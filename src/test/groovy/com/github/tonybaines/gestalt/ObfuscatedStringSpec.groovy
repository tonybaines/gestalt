package com.github.tonybaines.gestalt

import spock.lang.Specification

class ObfuscatedStringSpec extends Specification {
  def "Obfuscates a plaintext string"() {
    given:
    ObfuscatedString obfuscatedFromPlain = ObfuscatedString.fromString("plain")
    ObfuscatedString obfuscatedFromObfuscated = ObfuscatedString.fromString("{rot13}cynva")

    expect:
    obfuscatedFromPlain.toString() == '{rot13}cynva'
    obfuscatedFromPlain.toPlainTextString() == 'plain'
    obfuscatedFromObfuscated.toString() == '{rot13}cynva'
    obfuscatedFromObfuscated.toPlainTextString() == 'plain'
  }
}
