<#
Teste la même archive sur plusieurs Elasticsearch isolés, sans port publié.
Prérequis : PowerShell 7 et Docker ; lancer mvn clean verify au préalable.
#>
param(
    [string[]] $ESVersions = @('9.0.0', '9.5.6'),
    [string] $PluginZip
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
if (-not $PluginZip) {
    $PluginZip = Join-Path $repo 'es-custom-french-stemmer/target/releases/px-custom-french-stemmer-0.1.0-es9.zip'
}
$PluginZip = (Resolve-Path -LiteralPath $PluginZip).Path
$initialHash = (Get-FileHash -LiteralPath $PluginZip -Algorithm SHA256).Hash
$artifacts = Join-Path $repo 'es-custom-french-stemmer/target/integration'
New-Item -ItemType Directory -Force -Path $artifacts | Out-Null

function Invoke-DockerChecked {
    <# Exécute Docker et propage immédiatement toute erreur de la commande. #>
    param([Parameter(ValueFromRemainingArguments = $true)][string[]] $DockerArguments)
    $output = & docker @DockerArguments
    if ($LASTEXITCODE -ne 0) { throw "Docker a échoué : $($DockerArguments -join ' ')" }
    return $output
}

# Les résultats attendus proviennent du corpus historique, pas du filtre installé.
$corpus = Join-Path $repo 'lucene-custom-french-stemmer/src/test/resources/stemmer-regression.tsv'
$words = [System.Collections.Generic.List[string]]::new()
$expected = [System.Collections.Generic.List[string]]::new()
foreach ($line in [System.IO.File]::ReadAllLines($corpus)) {
    if ($line.StartsWith('#')) { continue }
    $fields = $line.Split("`t", 2)
    $words.Add($fields[0])
    $expected.Add($fields[1])
}
$request = @{
    tokenizer = 'keyword'
    filter = @('custom_french_stemmer')
    text = $words.ToArray()
} | ConvertTo-Json -Depth 5 -Compress
$requestPath = Join-Path $artifacts 'analyze-request.json'
[System.IO.File]::WriteAllText($requestPath, $request, [System.Text.UTF8Encoding]::new($false))

foreach ($version in $ESVersions) {
    if ($version -notmatch '^9\.\d+\.\d+$') { throw "Version ES 9.x attendue : $version" }
    $name = "px-stemmer-test-$($version.Replace('.', '-'))-$PID"
    $created = $false
    try {
        Write-Host "Test Elasticsearch $version, archive SHA-256 $initialHash"
        # L'installation et le lancement ne touchent que ce nouveau conteneur.
        # Aucun réseau, port hôte, volume persistant ou service existant n'est utilisé.
        Invoke-DockerChecked @('create', '--name', $name, '--network', 'none',
            '--hostname', 'stable-stemmer', '--add-host', 'stable-stemmer:127.0.0.1',
            '--memory', '2g', '-e', 'discovery.type=single-node',
            '-e', 'xpack.security.enabled=false', '-e', 'ingest.geoip.downloader.enabled=false',
            '-e', 'ES_JAVA_OPTS=-Xms512m -Xmx512m', '--entrypoint', '/bin/bash',
            "docker.elastic.co/elasticsearch/elasticsearch:$version", '-c',
            'bin/elasticsearch-plugin install --batch file:///tmp/stemmer.zip && exec bin/elasticsearch') | Out-Null
        $created = $true
        Invoke-DockerChecked @('cp', $PluginZip, "${name}:/tmp/stemmer.zip") | Out-Null
        Invoke-DockerChecked @('cp', $requestPath, "${name}:/tmp/analyze-request.json") | Out-Null
        Invoke-DockerChecked @('start', $name) | Out-Null

        # Attendre la disponibilité HTTP ; un arrêt prématuré affiche les journaux.
        $ready = $false
        for ($attempt = 0; $attempt -lt 120; $attempt++) {
            & docker exec $name curl --silent --fail http://127.0.0.1:9200/ *> $null
            if ($LASTEXITCODE -eq 0) { $ready = $true; break }
            $running = Invoke-DockerChecked @('inspect', '--format', '{{.State.Running}}', $name)
            if ($running -ne 'true') { throw "Elasticsearch $version s'est arrêté au démarrage." }
            Start-Sleep -Seconds 2
        }
        if (-not $ready) { throw "Elasticsearch $version indisponible après 240 secondes." }
        $identity = (Invoke-DockerChecked @('exec', $name, 'curl', '--silent', '--fail',
            'http://127.0.0.1:9200/')) -join "`n" | ConvertFrom-Json
        if ($identity.version.number -ne $version) { throw 'Version du serveur inattendue.' }

        # Un seul appel analyse tout le corpus sans ICU ni synonymes : le radical brut.
        $response = (Invoke-DockerChecked @('exec', $name, 'curl', '--silent', '--show-error',
            '--fail', '-H', 'Content-Type: application/json', '--data-binary',
            '@/tmp/analyze-request.json', 'http://127.0.0.1:9200/_analyze')) -join "`n"
        [System.IO.File]::WriteAllText((Join-Path $artifacts "analyze-$version.json"), $response)
        $tokens = @(($response | ConvertFrom-Json).tokens)
        if ($tokens.Count -ne $expected.Count) { throw "Nombre de tokens inattendu : $($tokens.Count)." }
        for ($index = 0; $index -lt $expected.Count; $index++) {
            if ($tokens[$index].token -cne $expected[$index]) {
                throw "Radical différent pour '$($words[$index])' : '$($tokens[$index].token)' au lieu de '$($expected[$index])'."
            }
        }
        if ((Get-FileHash -LiteralPath $PluginZip -Algorithm SHA256).Hash -ne $initialHash) {
            throw "L'archive a changé pendant les tests."
        }
        Write-Host "OK : $version, $($expected.Count) radicaux identiques, même archive."
    }
    finally {
        if ($created) {
            # Conserver le diagnostic puis supprimer uniquement notre conteneur éphémère.
            & docker logs $name *> (Join-Path $artifacts "elasticsearch-$version.log")
            & docker rm --force $name | Out-Null
        }
    }
}
