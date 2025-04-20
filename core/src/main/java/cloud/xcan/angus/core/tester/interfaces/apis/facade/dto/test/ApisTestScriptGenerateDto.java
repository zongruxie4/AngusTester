package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test;

import static cloud.xcan.angus.model.AngusConstant.MAX_EXEC_DURATION_IN_MS;
import static cloud.xcan.angus.model.AngusConstant.MAX_FUNCTIONAL_ITERATIONS;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.spec.unit.TimeValue;
import cloud.xcan.angus.validator.TimeValueRange;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

@Valid

@Setter
@Getter
@Accessors(chain = true)
public class ApisTestScriptGenerateDto {

  @NotNull
  @Schema(description = "Test type", example = "PERFORMANCE", requiredMode = RequiredMode.REQUIRED)
  private TestType testType;

  @Schema(description = "Test execution priority, default MEDIUM", example = "MEDIUM")
  private Priority priority = Priority.DEFAULT;

  @Range(min = 1, max = MAX_FUNCTIONAL_ITERATIONS)
  @Schema(description = "Functional testing iterations, default 1, max 10", example = "MEDIUM")
  private Long iterations = 1L;

  @Min(1)
  @Schema(description = "Maximum number of threads. When there are multiple nodes, they will be evenly distributed to each node.", requiredMode = RequiredMode.REQUIRED)
  private int threads = 1;

  //@NotNull
  @TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  @Schema(description = "Duration of task execution, when iterations and duration are not configured, they will automatically execute for 30 seconds."/*, requiredMode = RequiredMode.REQUIRED*/)
  private TimeValue duration;

  @Min(0L)
  @Schema(description="Adjust ramp up thread number, specify during performance testing and the value does not exceed the threads.")
  private Integer rampUpThreads;

  @Schema(description = "Adjust ramp up thread interval, specify during performance testing the value does not exceed the duration.")
  private TimeValue rampUpInterval;

  @Schema(description = "Whether to ignore assertions. If not ignored, handle as sampling error when assertion fails.")
  private Boolean ignoreAssertions;

  /**
   * Important: A null value indicates that there is no need to update the test results.
   */
  @Schema(description =
      "When there are associated resources, will the test results be updated to the associated resources, "
          + "such as apis, use cases, and scenarios. The default is false.")
  private Boolean updateTestResult;

  @Schema(description = "Is the generated script subject to permission control? "
      + "When it is subject to permission control, by default, it is only visible to and accessible by the owner with full permissions, "
      + "Other users need to be manually authorized through \"Scripts\" -> \"Authorization\". "
      + "When it is not subject to permission control, it is visible and accessible to all users.")
  private Boolean auth;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApisTestScriptGenerateDto that = (ApisTestScriptGenerateDto) o;
    return testType == that.testType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(testType);
  }
}




