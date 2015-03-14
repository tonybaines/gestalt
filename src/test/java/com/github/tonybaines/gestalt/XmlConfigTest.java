package com.github.tonybaines.gestalt;

import com.github.tonybaines.gestalt.validation.ValidationResult;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class XmlConfigTest {
    @Test
    public void readsAnXmlConfigFileAndMakesThePropertiesAvailable() {
        TestConfig testConfig = Configurations.definedBy(TestConfig.class).fromXmlResource("common.xml").done();

        assertThat(testConfig.getIntValue(), is(5));
        assertThat(testConfig.getStringValue(), is("Five"));
        assertThat(testConfig.getDoubleValue(), is(5.0));
        assertThat(testConfig.isBooleanValue(), is(true));
        assertThat(testConfig.getSubConfig().getIntValue(), is(6));
        assertThat(testConfig.getStrings().get(0), is("A"));
        assertThat(testConfig.getStrings().get(1), is("B"));
        assertThat(testConfig.getStrings().get(2), is("C"));
        assertThat(testConfig.getHandedness(), is(Handed.left));
        assertThat(testConfig.getNonExistentDoubleWithDefault(), is(42.5));
        FluentIterable.from(testConfig.getThings()).anyMatch(new Predicate<TestConfig.Thing>() {
            public boolean apply(TestConfig.Thing thing) {
                return thing.getId().equals("alpha");
            }
        });
    }

    @Test
    public void canCallToStringOnAConfigInstance_Issue16() throws Exception {
        TestConfig testConfig = Configurations.definedBy(TestConfig.class).fromXmlResource("common.xml").done();
        //noinspection ResultOfMethodCallIgnored
        testConfig.toString();
    }

    @Test
    public void supplyingAClassInstanceMakesItEasierToLocateResourcesWithARelativePath() throws Exception {
        Configurations.definedBy(SimpleConfig.class).fromXmlResource("com/github/tonybaines/gestalt/config/simple-config.xml").done();
        Configurations.definedBy(SimpleConfig.class).fromXmlResource("config/simple-config.xml", this.getClass()).done();
    }

    @Test
    public void constantsCanBeInjectedFromAPropertiesObject() throws Exception {
        EnclosingInterface config = Configurations.definedBy(EnclosingInterface.class).fromGroovyConfigResource("simple-config-with-constant-refs.grc").withConstantsFromResource("constants.properties").done();

        assertThat(config.getSimpleConfig().getName(), is("bar"));
        assertThat(config.getSimpleConfig().getLevel(), is(11));
      assertThat(config.getSimpleConfig().getEnabled(), is(true));
    }

    @Test
    public void anInstanceLoadedFromAResourceCanBeValidated() throws Exception {
        TestConfig testConfig = Configurations.definedBy(TestConfig.class).fromXmlResource("common.xml").done();
        final ValidationResult validationResult = Configurations.validate(testConfig, TestConfig.class);
        assertTrue(validationResult.hasFailures());
    }

    @Test
    public void anInstanceCreatedAnonymouslyCanBeValidated() throws Exception {
        SimpleConfig simpleConfig = new SimpleConfig() {
            public String getName() {
                return null;
            }

          public Integer getLevel() {
                return 0;
            }

          public Boolean getEnabled() {
                return false;
            }

            public String getDefaultOnly() {
                return null;
            }
        };

        final ValidationResult validationResult = Configurations.validate(simpleConfig, SimpleConfig.class);

        assertFalse("Validation Result:\n" + validationResult, validationResult.hasFailures());
    }

    @Test
    public void anUndefinedChildConfigValueRevertsToDefaultValue() throws Exception {
        EnclosingInterface config = Configurations.definedBy(EnclosingInterface.class).fromXmlResource("simple-config-with-constant-refs.xml").done();
        assertThat(config.getSimpleConfig().getDefaultOnly(), is("defaulted"));
    }

}
