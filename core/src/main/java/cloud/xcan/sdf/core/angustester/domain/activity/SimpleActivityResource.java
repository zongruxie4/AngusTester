package cloud.xcan.sdf.core.angustester.domain.activity;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class SimpleActivityResource implements ActivityResource {

  private CombinedTargetType targetType;

  private Long id;

  private String name;

  private Long parentId;

  private Long projectId;

}
