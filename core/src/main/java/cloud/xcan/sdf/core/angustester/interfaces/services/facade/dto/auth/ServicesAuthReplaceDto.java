package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth;

import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ServicesAuthReplaceDto {

  @NotEmpty
  //@CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @ApiModelProperty(value = "Authorization permissions(Operation permission)", required = true)
  private Set<ServicesPermission> permissions;

}




