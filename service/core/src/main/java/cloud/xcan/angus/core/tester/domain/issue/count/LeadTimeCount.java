package cloud.xcan.angus.core.tester.domain.issue.count;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LeadTimeCount implements LeadTimeCountBase {

  @Schema(description = "The number of task assignees or case testers")
  protected long userNum;
  @Schema(description = "Assignee or tester average processing time")
  protected double userAvgProcessingTime = 0d;

  // Lead time
  @Schema(description = "Tasks or cases average processing time")
  protected double totalProcessingTime = 0d;
  @Schema(description = "Tasks or cases average processing time")
  protected double avgProcessingTime = 0d;
  @Schema(description = "Tasks or cases minimum processing time")
  protected double minProcessingTime = 0d;
  @Schema(description = "Tasks or cases maximum processing time")
  protected double maxProcessingTime = 0d;
  @Schema(description = "50th percentile tasks or cases processing time")
  protected double p50ProcessingTime = 0d;
  @Schema(description = "75h percentile tasks or cases processing time")
  protected double p75ProcessingTime = 0d;
  @Schema(description = "90th percentile tasks or cases processing time")
  protected double p90ProcessingTime = 0d;
  @Schema(description = "95th percentile tasks or cases processing time")
  protected double p95ProcessingTime = 0d;
  @Schema(description = "99th percentile tasks or cases processing time")
  protected double p99ProcessingTime = 0d;

}
