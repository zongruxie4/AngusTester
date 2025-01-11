package cloud.xcan.sdf.core.angustester.domain.report.record.content;

import cloud.xcan.sdf.api.angusctrl.exec.vo.result.ExecTestResultVo;
import cloud.xcan.sdf.core.angustester.domain.apis.summary.ApisDetailSummary;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ApisTestingContent implements ReportContent {

  private final String template = APIS_TESTING_RESULT;

  private ApisDetailSummary apis;

  private ExecTestResultVo testResult;

}
