package cloud.xcan.angus.core.tester.domain.report.record.content;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncPlanSummary;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanContent implements ReportContent {

  private final String template = TASK_SPRINT;

  private FuncPlanSummary plan;

  private Progress progress;

  private List<UserInfo> members;

  private EfficiencyCaseOverview cases;

}
