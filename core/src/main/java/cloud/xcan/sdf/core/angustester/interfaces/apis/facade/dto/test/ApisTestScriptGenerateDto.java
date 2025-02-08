package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test;

import static cloud.xcan.angus.model.AngusConstant.MAX_EXEC_DURATION_IN_MS;
import static cloud.xcan.angus.model.AngusConstant.MAX_FUNCTIONAL_ITERATIONS;

import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.spec.unit.TimeValue;
import cloud.xcan.sdf.web.validator.annotations.TimeValueRange;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisTestScriptGenerateDto {

  @NotNull
  @ApiModelProperty(value = "Test type", example = "PERFORMANCE", required = true)
  private TestType testType;

  @ApiModelProperty(value = "Test execution priority, default MEDIUM", example = "MEDIUM")
  private Priority priority = Priority.DEFAULT;

  @Range(min = 1, max = MAX_FUNCTIONAL_ITERATIONS)
  @ApiModelProperty(value = "Functional testing iterations, default 1, max 10", example = "MEDIUM")
  private Long iterations = 1L;

  @Min(1)
  @ApiModelProperty(value = "Maximum number of threads. When there are multiple nodes, they will be evenly distributed to each node.", required = true)
  private int threads = 1;

  //@NotNull
  @TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  @ApiModelProperty(value = "Duration of task execution, when iterations and duration are not configured, they will automatically execute for 30 seconds."/*, required = true*/)
  private TimeValue duration;

  @Min(0L)
  @ApiModelProperty("Adjust ramp up thread number, specify during performance testing and the value does not exceed the threads.")
  private Integer rampUpThreads;

  @ApiModelProperty(value = "Adjust ramp up thread interval, specify during performance testing the value does not exceed the duration.")
  private TimeValue rampUpInterval;

  @ApiModelProperty(value = "Whether to ignore assertions. If not ignored, handle as sampling error when assertion fails.")
  private Boolean ignoreAssertions;

  /**
   * Important: A null value indicates that there is no need to update the test results.
   */
  @ApiModelProperty(value =
      "When there are associated resources, will the test results be updated to the associated resources, "
          + "such as apis, use cases, and scenarios. The default is false.")
  private Boolean updateTestResult;

  @ApiModelProperty(value = "Is the generated script subject to permission control? "
      + "When it is subject to permission control, by default, it is only visible to and accessible by the owner with full permissions, "
      + "Other users need to be manually authorized through \"Scripts\" -> \"Authorization\". "
      + "When it is not subject to permission control, it is visible and accessible to all users.")
  private Boolean authFlag;

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




