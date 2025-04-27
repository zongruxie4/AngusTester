package cloud.xcan.angus.core.tester.domain.comment.summary;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.angus.spec.utils.TreeUtils.TreeNode;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class CommentTreeSummary implements TreeNode<CommentTreeSummary> {

  private Long id;

  private Long pid;

  private String content;

  private Long userId;

  private String userName;

  private String avatar;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  private int totalCommentNum;

  private List<CommentTreeSummary> children;

  @Override
  public void setChildren(List<CommentTreeSummary> children) {
    this.children = children;
  }
}



