package cloud.xcan.angus.core.tester.domain.report.record.content;

import cloud.xcan.angus.core.tester.domain.activity.summary.ActivitySummary;
import cloud.xcan.angus.core.tester.domain.comment.summary.CommentTreeSummary;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskDetailSummary;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskRemarkSummary;
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

}
