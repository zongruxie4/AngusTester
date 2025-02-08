package cloud.xcan.sdf.core.angustester.domain.setting;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.info.IdAndName;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashSet;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NoticeSetting {

  @NotNull
  @ApiModelProperty(required = true)
  private Boolean enabled = false;

  @ApiModelProperty(value = "Query organization type, it is required when enabled is true")
  private AuthObjectType orgType;

  @ApiModelProperty(value = "Query organization info, , it is required when enabled is true")
  private LinkedHashSet<IdAndName> orgs;

}
