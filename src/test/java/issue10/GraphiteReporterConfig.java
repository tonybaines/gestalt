package issue10;

/**
 * @author 702161900
 */
public interface GraphiteReporterConfig {
    String getHost();
    Integer getPort();
    String getPrefix();
    Integer getUploadPeriodSec();
    boolean isEnabled();
}
