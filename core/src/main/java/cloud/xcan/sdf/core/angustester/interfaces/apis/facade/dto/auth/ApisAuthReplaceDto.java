package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth;

import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import cloud.xcan.sdf.web.validator.annotations.CollectionValueNotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisAuthReplaceDto {

  @NotNull
  @Size(min = 1)
  @CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @ApiModelProperty(value = "Authorization permissions(Operation permission)", required = true)
  private Set<ApiPermission> permissions;

}




