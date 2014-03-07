package com.github.tonybaines.gestalt;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

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
    public void supplyingAClassInstanceMakesItEasierToLocateResourcesWithARelativePath() throws Exception {
        Configurations.definedBy(SimpleConfig.class).fromXmlResource("com/github/tonybaines/gestalt/config/simple-config.xml").done();
        Configurations.definedBy(SimpleConfig.class).fromXmlResource("config/simple-config.xml", this.getClass()).done();
    }
}
