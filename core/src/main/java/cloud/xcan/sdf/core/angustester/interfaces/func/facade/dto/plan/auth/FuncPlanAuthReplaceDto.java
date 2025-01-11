package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth;

import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanPermission;
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
public class FuncPlanAuthReplaceDto {

  @NotNull
  @Size(min = 1)
  @CollectionValueNotNull
  @ApiModelProperty(value = "Authorization permissions(Operation permission)", required = true)
  private Set<FuncPlanPermission> permissions;

}




