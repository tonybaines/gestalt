package com.github.tonybaines.gestalt;

public class ObfuscatedString {
  private static final String PREFIX = "{rot13}";
  private final String obfuscated;

  private ObfuscatedString(String string) {
    if (string.startsWith(PREFIX)) {
      // Already obfuscated
      this.obfuscated = string.substring(PREFIX.length());
    }
    else {
      this.obfuscated = obfuscated(string);
    }
  }

  public static ObfuscatedString fromString(String string) {
    return new ObfuscatedString(string);
  }

  @Override
  public String toString() {
    return PREFIX +obfuscated;
  }

  public String toPlainTextString() {
    return deobfuscate(obfuscated);
  }

  private String deobfuscate(String obfuscated) {
    return rot13(obfuscated);
  }

  private String obfuscated(String string) {
    return rot13(string);
  }

  private String rot13(String s) {
    StringBuilder rot13 = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if       (c >= 'a' && c <= 'm') c += 13;
      else if  (c >= 'A' && c <= 'M') c += 13;
      else if  (c >= 'n' && c <= 'z') c -= 13;
      else if  (c >= 'N' && c <= 'Z') c -= 13;
      rot13.append(c);
    }
    return rot13.toString();
  }
}
