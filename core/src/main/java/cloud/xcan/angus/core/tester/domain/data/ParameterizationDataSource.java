package cloud.xcan.angus.core.tester.domain.data;

import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.angus.model.element.extraction.ExtractionSource;
import cloud.xcan.angus.model.element.extraction.FileExtraction;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.model.element.extraction.HttpSamplingExtraction;
import cloud.xcan.angus.model.element.extraction.JdbcExtraction;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ParameterizationDataSource implements EnumMessage<String> {
  STATIC_VALUE,
  EXTRACT_FILE,
  EXTRACT_HTTP,
  EXTRACT_HTTP_SAMPLING,
  EXTRACT_JDBC;

  public static ParameterizationDataSource of(DefaultExtraction extraction) {
    if (extraction == null) {
      return STATIC_VALUE;
    } else if (extraction instanceof FileExtraction
        || extraction.getSource().equals(ExtractionSource.FILE.getValue())) {
      return EXTRACT_FILE;
    } else if (extraction instanceof HttpSamplingExtraction
        || extraction.getSource().equals(ExtractionSource.HTTP_SAMPLING.getValue())) {
      return EXTRACT_HTTP_SAMPLING;
    } else if (extraction instanceof HttpExtraction
        || extraction.getSource().equals(ExtractionSource.HTTP.getValue())) {
      return EXTRACT_HTTP;
    } else if (extraction instanceof JdbcExtraction
        || extraction.getSource().equals(ExtractionSource.JDBC.getValue())) {
      return EXTRACT_JDBC;
    }
    return EXTRACT_HTTP;
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
