package com.github.tonybaines.gestalt

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test as test

class GeckoKotlinTest {
  interface KotlinConfig {
    @get:[Default.String("baz")]
    val foo: String
  }

  @test fun `should work with a Kotlin interface of properties`() {
    val kConfig = Configurations.definedBy(KotlinConfig::class.java)
        .fromProperties(mapOf("foo" to "bar").toProperties())
        .done()

    assertThat(kConfig.foo, equalTo("bar"))
  }

  @test fun `should work with a Kotlin interface of properties with default value`() {
    val kConfig = Configurations.definedBy(KotlinConfig::class.java)
        .fromProperties(emptyMap<String,String>().toProperties())
        .done()

    assertThat(kConfig.foo, equalTo("baz"))
  }
}