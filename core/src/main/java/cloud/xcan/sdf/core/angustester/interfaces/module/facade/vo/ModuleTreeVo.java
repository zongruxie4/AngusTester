package cloud.xcan.sdf.core.angustester.interfaces.module.facade.vo;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.spec.utils.TreeUtils.TreeNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ModuleTreeVo implements TreeNode<ModuleTreeVo> {

  private Long id;

  private String name;

  private Long projectId;

  private Long pid;

  private Integer sequence;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private List<ModuleTreeVo> children;

  private boolean hasEditPermission = false;

  private Boolean hitFlag = false;

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
