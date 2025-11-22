package cloud.xcan.angus.core.tester.domain.report.record.content;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskSprintSummary;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskOverview;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintContent implements ReportContent {

  private final String template = TASK_SPRINT;

  private TaskSprintSummary sprint;

  private Progress progress;

  private List<UserInfo> members;

  private EfficiencyTaskOverview tasks;

}
