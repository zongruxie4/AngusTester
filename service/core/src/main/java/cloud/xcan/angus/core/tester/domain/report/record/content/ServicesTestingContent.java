package cloud.xcan.angus.core.tester.domain.report.record.content;

import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.core.tester.domain.apis.summary.ApisInfoSummary;
import cloud.xcan.angus.core.tester.domain.services.summary.ServicesSummary;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServicesTestingContent implements ReportContent {

  private final String template = SERVICES_TESTING_RESULT;

  private ServicesSummary services;

  private ExecApisResultInfo testResult;

  // Not used in report
  private List<ApisInfoSummary> apis;

}
