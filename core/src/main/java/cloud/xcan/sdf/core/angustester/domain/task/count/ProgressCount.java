package cloud.xcan.sdf.core.angustester.domain.task.count;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ProgressCount {

  // Total
  @ApiModelProperty(value = "Total number of work")
  protected long totalNum;
  @ApiModelProperty(value = "The number of completed work")
  protected long completedNum;
  @ApiModelProperty(value = "The rate of completed work, equal to the progress")
  protected double completedRate;

  // Workload
  @ApiModelProperty(value = "Evaluate workload")
  protected double evalWorkload = 0d;
  @ApiModelProperty(value = "Completed workload")
  protected double completedWorkload = 0d;
  @ApiModelProperty(value = "The rate of completed workload")
  protected double completedWorkloadRate = 0d;

}
