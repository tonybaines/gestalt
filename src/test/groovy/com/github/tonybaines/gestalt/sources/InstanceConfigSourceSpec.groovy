package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.*
import spock.lang.Specification
import spock.lang.Unroll

class InstanceConfigSourceSpec extends Specification {
    @Unroll
    def "Delegates property lookup to the supplied instance: #method"() {
        given:
        def configInstance = new SimpleConfig() {

            @Override
            String getName() {
                return "foo"
            }

            @Override
            Integer getLevel() {
                return 42
            }

            @Override
            Boolean isEnabled() {
                return true
            }

            @Override
            String getDefaultOnly() {
                return null
            }
        }
        ConfigSource source = new InstanceConfigSource(configInstance)

        expect:
        source.lookup(["ignored"], SimpleConfig.getMethod(method)) == expected

        where:
        method           | expected
        "getName"        | "foo"
        "getLevel"       | 42
        "isEnabled"      | true
        "getDefaultOnly" | null
    }

    @Unroll
    def "Delegates to a previously loaded config instance: #method"() {
        given:
        def configInstance = Configurations.definedBy(TestConfig).fromPropertiesResource('common.properties').done()
        ConfigSource source = new InstanceConfigSource(configInstance)

        expect:
        source.lookup(["ignored"], TestConfig.getMethod(method)) == expected

        where:
        method            | expected
        "getIntValue"     | 5
        "getStringValue"  | "Five"
        "getDoubleValue"  | 5.0
        "isBooleanValue"  | true
        "getStrings"      | ['A', 'B', 'C']
        "getHandedness"   | Handed.left

    }
}
