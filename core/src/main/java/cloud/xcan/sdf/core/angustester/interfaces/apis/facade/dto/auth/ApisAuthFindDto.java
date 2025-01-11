package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisAuthFindDto extends PageQuery {

  @ApiModelProperty(value = "Authorization id")
  private Long id;

  //@NotNull -> Transferring values in filters
  @ApiModelProperty(value = "Apis id", required = true)
  private Long apisId;

  @NotNull
  @ApiModelProperty(example = "USER", required = true)
  private AuthObjectType authObjectType;

  @ApiModelProperty(value = "Authorization object id")
  private Long authObjectId;

  @ApiModelProperty(value = "Authorization date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}



