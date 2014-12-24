package issue10;


public interface ExecutorsConfig {
    MessageListenersStripedExecutorConfig getStripedExecutor();

    SchedulerExecutorConfig getSchedulerExecutor();

    NewExecutorConfig getNewExecutor();

}
