package cloud.xcan.angus.core.tester.domain.report.record.content;

import cloud.xcan.angus.core.tester.domain.apis.summary.ApisDetailSummary;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ApisTestingContent implements ReportContent {

  private final String template = APIS_TESTING_RESULT;

  private ApisDetailSummary apis;

  private ExecTestResultSummary testResult;

}
