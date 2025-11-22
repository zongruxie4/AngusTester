package cloud.xcan.angus.core.tester.domain.report.record.content;

import cloud.xcan.angus.core.tester.domain.activity.summary.ActivitySummary;
import cloud.xcan.angus.core.tester.domain.comment.summary.CommentTreeSummary;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseDetailSummary;
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
