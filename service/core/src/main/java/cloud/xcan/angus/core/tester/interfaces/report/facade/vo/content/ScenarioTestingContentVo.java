package cloud.xcan.angus.core.tester.interfaces.report.facade.vo.content;

import cloud.xcan.angus.core.tester.domain.report.record.content.ScenarioTestingContent;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.ReportDetailVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScenarioTestingContentVo {

  private ReportDetailVo report;

  private ScenarioTestingContent content;

}
