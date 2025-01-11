package cloud.xcan.sdf.core.angustester.domain.report.record.content;

import cloud.xcan.sdf.core.angustester.domain.activity.summary.ActivitySummary;
import cloud.xcan.sdf.core.angustester.domain.comment.summary.CommentTreeSummary;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncCaseDetailSummary;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseContent implements ReportContent {

  private final String template = TASK;

  private FuncCaseDetailSummary cases;

  private List<ActivitySummary> activities;

  private List<CommentTreeSummary> comments;

}
