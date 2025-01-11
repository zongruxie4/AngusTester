package cloud.xcan.sdf.core.angustester.domain.report.record.content;

import cloud.xcan.sdf.api.angusctrl.exec.vo.result.ExecTestResultVo;
import cloud.xcan.sdf.core.angustester.domain.scenario.summary.ScenarioDetailSummary;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScenarioTestingContent implements ReportContent {

  private final String template = SCENARIO_TESTING_RESULT;

  private ScenarioDetailSummary scenario;

  private ExecTestResultVo testResult;

}
