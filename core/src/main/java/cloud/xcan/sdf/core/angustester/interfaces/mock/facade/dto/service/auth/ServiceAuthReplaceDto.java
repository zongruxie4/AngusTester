package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth;

import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ServiceAuthReplaceDto {

  @NotEmpty
  @Size(min = 1)
  @ApiModelProperty(value = "Authorization permissions(Operation permission), default view")
  private Set<MockServicePermission> permissions;

}




