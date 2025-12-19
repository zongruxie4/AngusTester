package cloud.xcan.angus.core.tester.interfaces.project.facade.vo;

import cloud.xcan.angus.remote.NameJoinField;
import cloud.xcan.angus.spec.utils.TreeUtils.TreeNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ModuleTreeVo implements TreeNode<ModuleTreeVo> {

  private Long id;

  private String name;

  private Long projectId;

  private Long pid;

  private Integer sequence;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String creator;

  private LocalDateTime createdDate;

  private List<ModuleTreeVo> children;

  private boolean hasEditPermission = false;

  private Boolean hit = false;

  @Override
  public void setChildren(List<ModuleTreeVo> children) {
    this.children = children;
  }

  @JsonIgnore
  @Override
  public Long getSequence0() {
    return Long.valueOf(sequence);
  }
}
