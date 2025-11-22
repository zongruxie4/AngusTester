package cloud.xcan.angus.core.tester.domain.report.record.content;

import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import cloud.xcan.angus.core.tester.domain.scenario.summary.ScenarioDetailSummary;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScenarioTestingContent implements ReportContent {

  private final String template = SCENARIO_TESTING_RESULT;

  private ScenarioDetailSummary scenario;

  private ExecTestResultSummary testResult;

}
