package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.model.script.TestType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Accessors(chain = true)
public class ApisTestTaskGenerateDto {

  @NotNull
  @Schema(description = "Assignee identifier for task assignment", requiredMode = RequiredMode.REQUIRED)
  private Long assigneeId;

  @NotNull
  @Schema(description = "Test type for task generation specification", example = "PERFORMANCE", requiredMode = RequiredMode.REQUIRED)
  private TestType testType;

  @Schema(description = "Test task priority for resource allocation, defaults to MEDIUM", example = "MEDIUM")
  private Priority priority = Priority.DEFAULT;

  @Future
  @NotNull
  @Schema(description = "Test task deadline for completion tracking", example = "2022-12-01 12:00:00", requiredMode = RequiredMode.REQUIRED)
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  //Generate based on apis metrics indicator
  //@Min(1)
  //@Schema(description = "Maximum number of threads. When there are multiple nodes, they will be evenly distributed to each node", requiredMode = RequiredMode.REQUIRED)
  //private int threads = 1;

  //Generate based on apis metrics indicator
  //@NotNull
  //@TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  //@Schema(description = "Duration of iterations for performance and stability", requiredMode = RequiredMode.REQUIRED)
  //private TimeValue duration;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApisTestTaskGenerateDto that = (ApisTestTaskGenerateDto) o;
    return testType == that.testType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(testType);
  }
}




