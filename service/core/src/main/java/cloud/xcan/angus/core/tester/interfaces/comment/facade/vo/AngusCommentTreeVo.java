package cloud.xcan.angus.core.tester.interfaces.comment.facade.vo;


import cloud.xcan.angus.spec.utils.TreeUtils.TreeNode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class AngusCommentTreeVo implements TreeNode<AngusCommentTreeVo> {

  private Long id;

  private Long pid;

  private String content;

  private Long userId;

  private String userName;

  private String avatar;

  private LocalDateTime createdDate;

  private int totalCommentNum;

  private List<AngusCommentTreeVo> children;

  @Override
  public void setChildren(List<AngusCommentTreeVo> children) {
    this.children = children;
  }
}



