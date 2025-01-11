package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Valid
@Setter
@Getter
public class TaskAssigneeWorkStatisticsDto {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @ApiModelProperty(value = "Sprint id")
  private Long sprintId;

  @NotNull
  @ApiModelProperty(value = "Query work user ID", required = true)
  private Long userId;

}
