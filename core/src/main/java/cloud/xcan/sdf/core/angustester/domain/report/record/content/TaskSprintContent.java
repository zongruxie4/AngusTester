package cloud.xcan.sdf.core.angustester.domain.report.record.content;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskSprintSummary;
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
