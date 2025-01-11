package cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth;

import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.web.validator.annotations.CollectionValueNotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
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
public class ScriptAuthAddDto {

  @NotNull
  @ApiModelProperty(example = "USER", required = true)
  private AuthObjectType authObjectType;

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long authObjectId;

  @Size(min = 1)
  @CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @ApiModelProperty(value = "Authorization permissions(Operation permission), default view")
  private List<ScriptPermission> permissions;

}




