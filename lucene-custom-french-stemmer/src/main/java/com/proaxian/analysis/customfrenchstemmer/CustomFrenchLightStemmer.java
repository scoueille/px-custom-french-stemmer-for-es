/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.proaxian.analysis.customfrenchstemmer;

import static org.apache.lucene.analysis.util.StemmerUtil.*;
/**
 *
 * @author Sebastien Coueille
 */
public class CustomFrenchLightStemmer {
    
  public int stem(char s[], int len) {
    for (int i = 0; i < len; i++)
      switch(s[i]) {
        case '.': 
        case '·':
        case '‧': 
        case '․': 
        case '﹒':
            len = delete(s, i--, len);
      }
        
    if (len > 5 && s[len-1] == 'x') {
      if (s[len-3] == 'a' && s[len-2] == 'u' && s[len-4] != 'e')
        s[len-2] = 'l';
      len--;
    }
    
    if (len > 3 && s[len-1] == 'x')
      len--;
    
    if (len > 3 && s[len-1] == 's')
      len--;
    
    if (len > 9 && endsWith(s, len, "issement")) {
      len -= 6;
      s[len-1] = 'r';
      return norm(s, len);
    }
    
    if (len > 8 && endsWith(s, len, "issant")) {
      len -= 4;
      s[len-1] = 'r';
      return norm(s, len);
    }
    
    if (len > 6 && endsWith(s, len, "ement")) {
      len -= 4;
      if (len > 3 && endsWith(s, len, "ive")) {
        len--;
        s[len-1] = 'f';
      }
      return norm(s, len);
    }
    
    if (len > 11 && endsWith(s, len, "ficatrice")) {
      len -= 5;
      s[len-2] = 'e';
      s[len-1] = 'r';
      return norm(s, len);
    }
    
    if (len > 10 && endsWith(s, len, "ficateur")) {
      len -= 4;
      s[len-2] = 'e';
      s[len-1] = 'r';
      return norm(s, len);
    }
    
    if (len > 9 && endsWith(s, len, "catrice")) {
      len -= 3;
      s[len-4] = 'q';
      s[len-3] = 'u';
      s[len-2] = 'e';
      //s[len-1] = 'r' <-- unnecessary, already 'r'.
      return norm(s, len);
    }
    
    if (len > 8 && endsWith(s, len, "cateur")) {
      len -= 2;
      s[len-4] = 'q';
      s[len-3] = 'u';
      s[len-2] = 'e';
      s[len-1] = 'r';
      return norm(s, len);
    }
    
    if (len > 8 && endsWith(s, len, "atrice")) {
      len -= 4;
      s[len-2] = 'e';
      s[len-1] = 'r';
      return norm(s, len);
    }
    
    if (len > 7 && endsWith(s, len, "ateur")) {
      len -= 3;
      s[len-2] = 'e';
      s[len-1] = 'r';
      return norm(s, len);
    }
    
    if (len > 6 && endsWith(s, len, "trice")) {
      len--;
      s[len-3] = 'e';
      s[len-2] = 'u';
      s[len-1] = 'r';
    }
    
    if (len > 5 && endsWith(s, len, "ième"))
      return norm(s, len-4);
    
    if (len > 5 && endsWith(s, len, "ieme"))
      return norm(s, len-4);
    
    if (len > 7 && endsWith(s, len, "teuse")) {
      len -= 2;
      s[len-1] = 'r';
      return norm(s, len);
    }
    
    if (len > 6 && endsWith(s, len, "teur")) {
      len--;
      s[len-1] = 'r';
      return norm(s, len);
    }
    
    if (len > 5 && endsWith(s, len, "euse"))
      return norm(s, len-2);
    
    if (len > 5 && endsWith(s, len, "eure"))
      return norm(s, len-2);
    
    if (len > 7 && endsWith(s, len, "logue"))
      return norm(s, len-2);
    
    if (len > 7 && endsWith(s, len, "ère")) {
      len--;
      s[len-2] = 'e';
      return norm(s, len);
    }
    
    if (len > 7 && endsWith(s, len, "ere")) {
      len--;
      return norm(s, len);
    }
    
    if (len > 7 && endsWith(s, len, "ive")) {
      len--;
      s[len-1] = 'f';
      return norm(s, len);
    }
    
    if (len > 4 && 
        (endsWith(s, len, "folle") ||
         endsWith(s, len, "molle"))) {
      len -= 2;
      s[len-1] = 'u';
      return norm(s, len);
    }
    
    if (len > 9 && endsWith(s, len, "nnelle"))
      return norm(s, len-5);
    
    if (len > 9 && endsWith(s, len, "nnel"))
      return norm(s, len-3);
    
    if (len > 4 && endsWith(s, len, "ète")) {
      len--;
      s[len-2] = 'e';
    }
    
    if (len > 4 && endsWith(s, len, "ete")) {
      len--;
    }
    
    if (len > 8 && endsWith(s, len, "isme"))
      len -= 4;
    
    if (len > 8 && endsWith(s, len, "ique"))
      len -= 4;
    
    if (len > 8 && endsWith(s, len, "able"))
      len -= 4;
    
    if (len > 8 && endsWith(s, len, "ance"))
      len -= 4;
    
    if (len > 8 && endsWith(s, len, "ence")){
      len--;
      s[len-1] = 't';
      return norm(s, len);
    }
    
    if (len > 8 && endsWith(s, len, "esse"))
      return norm(s, len-3);
    
    if (len > 7 && endsWith(s, len, "inage"))
      return norm(s, len-3);
    
    if (len > 9 && endsWith(s, len, "isation")) {
      len -= 7;
      if (len > 5 && endsWith(s, len, "ual"))
        s[len-2] = 'e';
      return norm(s, len);
    }
    
    if (len > 9 && endsWith(s, len, "isateur"))
      return norm(s, len-7);
    
    if (len > 8 && endsWith(s, len, "ation"))
      return norm(s, len-5);

    if (len > 8 && endsWith(s, len, "ition"))
      return norm(s, len-5);
    
    return norm(s, len);
  }

  private int norm(char s[], int len) {
    if (len > 4) {
      for (int i = 0; i < len; i++)
        switch(s[i]) {
          case 'à': 
          case 'á':
          case 'â': s[i] = 'a'; break;
          case 'ô': s[i] = 'o'; break;
          case 'è':
          case 'é':
          case 'ê': s[i] = 'e'; break;
          case 'ù':
          case 'û': s[i] = 'u'; break;
          case 'î': s[i] = 'i'; break;
          case 'ç': s[i] = 'c'; break;
        }
      
      char ch = s[0];
      for (int i = 1; i < len; i++) {
        if (s[i] == ch && Character.isLetter(ch))
          len = delete(s, i--, len);
        else
          ch = s[i];
      }
    }
    
    if (len > 4 && endsWith(s, len, "ie"))
      len -= 2;
    
    if (len > 4) {
        if (s[len-1] == 'r') len--;
        if (s[len-1] == 'e') len--;
        if (s[len-1] == 'e') len--;
        if (s[len-1] == s[len-2] && Character.isLetter(s[len-1])) len--;
    }
    return len;
  }
}
