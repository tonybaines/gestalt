package tonybaines.configuration;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class XmlConfigTest {
  @Test
  public void readsAnXmlConfigFileAndMakesThePropertiesAvailable() {
    Configuration<TestConfig> config = DefaultConfiguration.definedBy(TestConfig.class).fromXmlFile("common.xml");
    TestConfig testConfig = config.load();

    assertThat(testConfig.getIntValue(), is(5));
    assertThat(testConfig.getStringValue(), is("Five"));
    assertThat(testConfig.getDoubleValue(), is(5.0));
    assertThat(testConfig.getBooleanValue(), is(true));
    assertThat(testConfig.getSubConfig().getIntValue(), is(6));
    assertThat(testConfig.getStrings().get(0), is("A"));
    assertThat(testConfig.getStrings().get(1), is("B"));
    assertThat(testConfig.getStrings().get(2), is("C"));
    assertThat(testConfig.getHandedness(), is(Handed.left));
  }
}