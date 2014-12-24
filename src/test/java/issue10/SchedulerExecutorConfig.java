package issue10;

import com.github.tonybaines.gestalt.Default;

public interface SchedulerExecutorConfig {

    @Default.Integer(4)
    Integer getCorePoolSize();
}
