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
  @Schema(description = "Execution name")
  private String name;

  @Max(value = MAX_ITERATIONS)
  @Schema(description = "Number of sampling iterations executed. Note: "
      + "1. When mocking data, the current value will be forcefully set to the specified rows for mocking data; "
      + "2. Iterations after sharding during multi node testing")
  private Long iterations;

  @TimeValueRange(maxInMs = MAX_EXEC_DURATION_IN_MS) // 3 days
  @Schema(description = "Duration of task execution, when iterations and duration are not configured, they will automatically execute for 30 seconds")
  private TimeValue duration;

  @Schema(description = "Duration of task execution")
  @Range(min = 1, max = MAX_THREAD_NUM)
  private Integer thread;

  @Schema(description = "Execution priority of tasks, with higher numerical values being prioritized. Supported range: 1-2147483647, default 1000")
  private Integer priority /*= DEFAULT_PRIORITY*/;

  @Schema(description = "Whether to ignore assertions. If not ignored, handle as sampling error when assertion fails")
  private Boolean ignoreAssertions;

  /**
   * Important: A null value indicates that there is no need to update the test results.
   */
  @Schema(description = "When there are associated resources, will the test results be updated to the associated resources, "
      + "such as apis, use cases, and scenarios. The default is false")
  private Boolean updateTestResult;

  @Schema(description = "Set the execution mode, including two options: IMMEDIATELY and TIMING. "
      + "In IMMEDIATELY mode, the task will be scheduled and executed immediately. "
      + "In TIMING mode, the task will be scheduled and executed at the designated startDate", required = true)
  private StartMode startMode;

  @Schema(description = "The desired scheduled execution time for the task. "
      + "If not set or if the time is less than the current time, it will be immediately scheduled for execution")
  private LocalDateTime startAtDate;

  @TimeValueRange(minInMs = MIN_REPORT_INTERVAL_MS, maxInMs = MAX_REPORT_INTERVAL_MS)
  @Schema(description = "The interval for reporting sampling results. Supported range: 1-300 second, default 5 second")
  private TimeValue reportInterval/* = DEFAULT_REPORT_INTERVAL*/;

}
