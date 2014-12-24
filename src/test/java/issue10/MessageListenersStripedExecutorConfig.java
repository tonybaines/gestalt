package issue10;

import com.github.tonybaines.gestalt.Default;

public interface MessageListenersStripedExecutorConfig {

    @Default.Integer(32)
    Integer getWorkerThreads();

    @Default.Integer(1000000)
    Integer getWorkersQueueSize();
}
