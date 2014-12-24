package issue10;

public interface MetricsConfig {
    boolean isJvmGaugesEnabled();
    Integer getSamplingPeriodSec();
    ReportersConfig getReporters();
}
