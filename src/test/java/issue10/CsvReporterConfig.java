package issue10;

/**
 * @author 702161900
 */
public interface CsvReporterConfig {
    boolean isEnabled();
    String getPath();
    Integer getMaxFileSize();
    Integer getMaxFileIndex();
}
