package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth;

import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ServicesAddAuthDto {

  @NotNull
  @ApiModelProperty(example = "USER", value = "Authorization object type", required = true)
  private AuthObjectType authObjectType;

  @NotNull
  @ApiModelProperty(example = "1", value = "Authorization object id", required = true)
  private Long authObjectId;

  //@CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @ApiModelProperty(value = "Authorization permissions(Operation permission), default view")
  private Set<ServicesPermission> permissions;

}
