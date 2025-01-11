package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ServiceAddAuthDto {

  @NotNull
  @ApiModelProperty(example = "USER", value = "Authorization object type", required = true)
  private AuthObjectType authObjectType;

  @NotNull
  @ApiModelProperty(example = "1", value = "Authorization object id", required = true)
  private Long authObjectId;

  @Size(min = 1)
  @ApiModelProperty(value = "Authorization permissions(Operation permission), default view")
  private Set<MockServicePermission> permissions;
}
