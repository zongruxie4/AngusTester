package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test;

import static cloud.xcan.angus.model.AngusConstant.MAX_EXEC_DURATION_IN_MS;
import static cloud.xcan.angus.model.AngusConstant.MAX_FUNCTIONAL_ITERATIONS;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.spec.unit.TimeValue;
import cloud.xcan.angus.validator.TimeValueRange;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
@Accessors(chain = true)
public class ApisTestScriptGenerateDto {

  @NotNull
  @Schema(description = "Test type for script generation specification", example = "PERFORMANCE", requiredMode = RequiredMode.REQUIRED)
  private TestType testType;

  @Schema(description = "Test execution priority for resource allocation, defaults to MEDIUM", example = "MEDIUM")
  private Priority priority = Priority.DEFAULT;

  @Range(min = 1, max = MAX_FUNCTIONAL_ITERATIONS)
  @Schema(description = "Functional testing iteration count, defaults to 1, maximum 10", example = "1")
  private Long iterations = 1L;

  @Min(1)
  @Schema(description = "Maximum thread count for distributed execution across nodes", requiredMode = RequiredMode.REQUIRED)
  private int threads = 1;

  //@NotNull
  @TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  @Schema(description = "Task execution duration, defaults to 30 seconds when iterations and duration are not configured"/*, requiredMode = RequiredMode.REQUIRED*/)
  private TimeValue duration;

  @Min(0L)
  @Schema(description = "Ramp-up thread count for performance testing, must not exceed total threads")
  private Integer rampUpThreads;

  @Schema(description = "Ramp-up thread interval for performance testing, must not exceed total duration")
  private TimeValue rampUpInterval;

  @Schema(description = "Assertion ignore flag for error handling configuration")
  private Boolean ignoreAssertions;

  /**
   * Important: A null value indicates that there is no need to update the test results.
   */
  @Schema(description = "Test result update flag for associated resources like APIs, use cases, and scenarios, defaults to false")
  private Boolean updateTestResult;

  @Schema(description = "Permission control flag for generated script access management, defaults to owner-only visibility")
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




