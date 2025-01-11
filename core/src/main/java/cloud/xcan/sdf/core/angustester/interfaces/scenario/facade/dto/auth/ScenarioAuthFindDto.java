package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@ApiModel
public class ScenarioAuthFindDto extends PageQuery {

  @ApiModelProperty(value = "Authorization id")
  private Long id;

  //@NotNull -> Transferring values in filters
  @ApiModelProperty(value = "Scenario id", required = true)
  private Long scenarioId;

  @NotNull
  @ApiModelProperty(example = "USER", required = true)
  private AuthObjectType authObjectType;

  @ApiModelProperty(value = "Authorization object id")
  private Long authObjectId;

  @ApiModelProperty(value = "Authorization date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
