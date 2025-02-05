package cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.variable;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class VariableTargetVo {

  private Long targetId;

  private String targetName;

  private CombinedTargetType targetType;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;


}
