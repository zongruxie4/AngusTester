package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class TaskAssigneeSummaryStatisticsDto {

  @NotNull
  @ApiModelProperty(required = true)
  private Long projectId;

  private Long sprintId;

}
