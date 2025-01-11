package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ServicesAuthFindDto extends PageQuery {

  //@NotNull -> Transferring values in filters
  @ApiModelProperty(value = "Services id", required = true)
  private Long serviceId;

  @NotNull
  @ApiModelProperty(example = "USER", value = "Authorization object type", required = true)
  private AuthObjectType authObjectType;

  @ApiModelProperty(value = "Authorization object id")
  private Long authObjectId;

  private Date createdDate;

}



