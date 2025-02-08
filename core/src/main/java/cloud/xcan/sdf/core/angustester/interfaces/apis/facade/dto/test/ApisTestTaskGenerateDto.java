package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.model.script.TestType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisTestTaskGenerateDto {

  @NotNull
  @ApiModelProperty(value = "Assignee id", required = true)
  private Long assigneeId;

  @NotNull
  @ApiModelProperty(value = "Test type", example = "PERFORMANCE", required = true)
  private TestType testType;

  @ApiModelProperty(value = "Test task priority, Default MEDIUM", example = "MEDIUM")
  private Priority priority = Priority.DEFAULT;

  @Future
  @NotNull
  @ApiModelProperty(value = "Test task deadline", example = "2022-12-01 12:00:00", required = true)
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  //Generate based on apis metrics indicator
  //@Min(1)
  //@ApiModelProperty(value = "Maximum number of threads. When there are multiple nodes, they will be evenly distributed to each node.", required = true)
  //private int threads = 1;

  //Generate based on apis metrics indicator
  //@NotNull
  //@TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  //@ApiModelProperty(value = "Duration of iterations for performance and stability", required = true)
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




