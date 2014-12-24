package issue10;

import com.github.tonybaines.gestalt.Default;

public interface NewExecutorConfig {

    @Default.Integer(4)
    Integer getCoreThreads();

    @Default.Integer(16)
    Integer getMaxThreads();

    @Default.Integer(4096)
    Integer getQueueSize();
}


