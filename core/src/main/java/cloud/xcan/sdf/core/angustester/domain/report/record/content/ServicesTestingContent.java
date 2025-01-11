package cloud.xcan.sdf.core.angustester.domain.report.record.content;

import cloud.xcan.sdf.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.summary.ApisInfoSummary;
import cloud.xcan.sdf.core.angustester.domain.services.summary.ServicesSummary;
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
