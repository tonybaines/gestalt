package issue10;

public interface BrokerConfig {
    ExecutorsConfig getExecutors();

    DirectChannelHousekeeperConfig getDirectChannelHousekeeper();

}
