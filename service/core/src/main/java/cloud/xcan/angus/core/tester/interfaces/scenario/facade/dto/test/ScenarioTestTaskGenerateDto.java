package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.test;

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
public class ScenarioTestTaskGenerateDto {

  @NotNull
  @Schema(description = "Test task assignee identifier for responsibility assignment", requiredMode = RequiredMode.REQUIRED)
  private Long assigneeId;

  @NotNull
  @Schema(description = "Test type defining the testing methodology and execution approach", example = "FUNCTIONAL", requiredMode = RequiredMode.REQUIRED)
  private TestType testType;

  @Schema(description = "Test task priority level for resource allocation and scheduling", example = "MEDIUM")
  private Priority priority = Priority.DEFAULT;

  @Future
  @NotNull
  @Schema(description = "Test task deadline date for delivery planning and resource allocation", example = "2022-12-01 12:00:00", requiredMode = RequiredMode.REQUIRED)
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ScenarioTestTaskGenerateDto that = (ScenarioTestTaskGenerateDto) o;
    return testType == that.testType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(testType);
  }
}




