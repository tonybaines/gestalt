package tonybaines.configuration;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface TestConfig {
  @NotNull()
  public abstract Integer getIntValue();

  public abstract String getStringValue();

  public abstract Double getDoubleValue();

  public abstract Boolean getBooleanValue();

  public abstract Handed getHandedness();

  public abstract SubConfigLevel1 getSubConfig();

  public abstract List<String> getStrings();

  public abstract List<Thing> getThings();

  public static interface SubConfigLevel1 {
    public abstract Integer getIntValue();
  }

  public static interface Thing {
    String getId();

    String getStringValue();
  }
}
