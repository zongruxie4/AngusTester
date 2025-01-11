package cloud.xcan.sdf.core.angustester.interfaces.comment.facade.vo;


import cloud.xcan.sdf.spec.utils.TreeUtils.TreeNode;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
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



