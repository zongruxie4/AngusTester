package cloud.xcan.sdf.core.angustester.domain.activity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class SimpleTaskActivityResource implements MainTargetActivityResource {

  private Long id;
  private String name;
  private Long parentId;
  private Long taskId;
  private Long projectId;

  @Override
  public Long getMainTargetId() {
    return taskId;
  }
}
