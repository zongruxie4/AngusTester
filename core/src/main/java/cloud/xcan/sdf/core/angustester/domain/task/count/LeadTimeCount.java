package cloud.xcan.sdf.core.angustester.domain.task.count;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LeadTimeCount implements LeadTimeCountBase {

  @ApiModelProperty(value = "The number of task assignees or case testers")
  protected long userNum;
  @ApiModelProperty(value = "Assignee or tester average processing time")
  protected double userAvgProcessingTime = 0d;

  // Lead time
  @ApiModelProperty(value = "Tasks or cases average processing time")
  protected double totalProcessingTime = 0d;
  @ApiModelProperty(value = "Tasks or cases average processing time")
  protected double avgProcessingTime = 0d;
  @ApiModelProperty(value = "Tasks or cases minimum processing time")
  protected double minProcessingTime = 0d;
  @ApiModelProperty(value = "Tasks or cases maximum processing time")
  protected double maxProcessingTime = 0d;
  @ApiModelProperty(value = "50th percentile tasks or cases processing time")
  protected double p50ProcessingTime = 0d;
  @ApiModelProperty(value = "75h percentile tasks or cases processing time")
  protected double p75ProcessingTime = 0d;
  @ApiModelProperty(value = "90th percentile tasks or cases processing time")
  protected double p90ProcessingTime = 0d;
  @ApiModelProperty(value = "95th percentile tasks or cases processing time")
  protected double p95ProcessingTime = 0d;
  @ApiModelProperty(value = "99th percentile tasks or cases processing time")
  protected double p99ProcessingTime = 0d;

}
