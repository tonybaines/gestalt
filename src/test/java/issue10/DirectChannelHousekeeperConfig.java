package issue10;

import com.github.tonybaines.gestalt.Default;

public interface DirectChannelHousekeeperConfig {
    @Default.Long(900000)
    Long getMessageTimeToLiveMillis();

    @Default.Long(60000)
    Long getCheckPeriodMillis();
}
