package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto;

import static cloud.xcan.angus.model.AngusConstant.MAX_EXEC_DURATION_IN_MS;
import static cloud.xcan.angus.model.AngusConstant.MAX_ITERATIONS;
import static cloud.xcan.angus.model.AngusConstant.MAX_REPORT_INTERVAL_MS;
import static cloud.xcan.angus.model.AngusConstant.MAX_THREAD_NUM;
import static cloud.xcan.angus.model.AngusConstant.MIN_REPORT_INTERVAL_MS;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.model.script.configuration.StartMode;
import cloud.xcan.angus.spec.unit.TimeValue;
import cloud.xcan.angus.validator.TimeValueRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Setter
@Getter
@Accessors(chain = true)
public class ExecConfigReplaceDto {

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Execution name for identification and organization")
  private String name;

  @Max(value = MAX_ITERATIONS)
  @Schema(description = "Number of sampling iterations for execution control with mock data and multi-node support")
  private Long iterations;

  @TimeValueRange(maxInMs = MAX_EXEC_DURATION_IN_MS) // 3 days
  @Schema(description = "Task execution duration with automatic 30-second fallback when iterations and duration are not configured")
  private TimeValue duration;

  @Schema(description = "Thread count for concurrent execution control")
  @Range(min = 1, max = MAX_THREAD_NUM)
  private Integer thread;

  @Schema(description = "Execution priority for task scheduling with higher numerical values being prioritized")
  private Integer priority /*= DEFAULT_PRIORITY*/;

  @Schema(description = "Assertion ignore flag for error handling configuration")
  private Boolean ignoreAssertions;

  /**
   * Important: A null value indicates that there is no need to update the test results.
   */
  @Schema(description = "Test result update flag for associated resources like APIs, use cases, and scenarios")
  private Boolean updateTestResult;

  @Schema(description = "Execution mode specification for immediate or scheduled task execution")
  private StartMode startMode;

  @Schema(description = "Scheduled execution time for timing mode with immediate execution fallback")
  private LocalDateTime startAtDate;

  @TimeValueRange(minInMs = MIN_REPORT_INTERVAL_MS, maxInMs = MAX_REPORT_INTERVAL_MS)
  @Schema(description = "Sampling result reporting interval for real-time monitoring")
  private TimeValue reportInterval/* = DEFAULT_REPORT_INTERVAL*/;

}
