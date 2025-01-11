package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Valid
@Setter
@Getter
public class FuncTesterWorkStatisticsDto {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @ApiModelProperty(value = "Plan id")
  private Long planId;

  @NotNull
  @ApiModelProperty(value = "Query work user ID", required = true)
  private Long userId;

}
