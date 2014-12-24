package issue10;

import com.github.tonybaines.gestalt.Default;

public interface Eventing {

    @Default.String("broker")
    String getProcessName();

    StackConfig getStack();

    BrokerConfig getBroker();

    FrontendConfig getFrontend();

    MetricsConfig getMetrics();
}
