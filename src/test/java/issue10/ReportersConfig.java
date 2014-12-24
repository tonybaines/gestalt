package issue10;

public interface ReportersConfig {

    CsvReporterConfig getCsvReporter();
    JmxReporterConfig getJmxReporter();
    GraphiteReporterConfig getGraphiteReporter();
    Slf4jReporterConfig getSlf4jReporter();
}
