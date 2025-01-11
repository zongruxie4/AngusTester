package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Valid
@Setter
@Getter
@ApiModel
public class FuncPlanAuthFindDto extends PageQuery {

  //@NotNull -> Transferring values in filters
  @ApiModelProperty(value = "Plan id", required = true)
  private Long planId;

  @NotNull
  @ApiModelProperty(example = "USER", required = true)
  private AuthObjectType authObjectType;

  @ApiModelProperty(value = "Authorization object id")
  private Long authObjectId;

  private Date createdDate;

}
