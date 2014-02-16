package tonybaines.configuration;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class XmlConfigTest {
  @Test
  public void readsAnXmlConfigFileAndMakesThePropertiesAvailable() {
    Configurations<TestConfig> config = Configurations.definedBy(TestConfig.class).fromXmlFile("common.xml");
    TestConfig testConfig = config.load();

    assertThat(testConfig.intValue(), is(5));
    assertThat(testConfig.stringValue(), is("Five"));
    assertThat(testConfig.doubleValue(), is(5.0));
    assertThat(testConfig.booleanValue(), is(true));
    assertThat(testConfig.subConfig().intValue(), is(6));
    assertThat(testConfig.strings().get(0), is("A"));
    assertThat(testConfig.strings().get(1), is("B"));
    assertThat(testConfig.strings().get(2), is("C"));
    assertThat(testConfig.handedness(), is(Handed.left));
  }
}
