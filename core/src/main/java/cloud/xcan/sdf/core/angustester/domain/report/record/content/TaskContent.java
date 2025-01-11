package cloud.xcan.sdf.core.angustester.domain.report.record.content;

import cloud.xcan.sdf.api.angusctrl.exec.vo.result.ExecTestResultDetailVo;
import cloud.xcan.sdf.core.angustester.domain.activity.summary.ActivitySummary;
import cloud.xcan.sdf.core.angustester.domain.comment.summary.CommentTreeSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskDetailSummary;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskRemarkSummary;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskContent implements ReportContent {

  private final String template = TASK;

  private TaskDetailSummary task;

  private List<TaskRemarkSummary> remarks;

  private List<ActivitySummary> activities;

  private List<CommentTreeSummary> comments;

  private ExecTestResultDetailVo testResult;

}
