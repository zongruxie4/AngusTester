package cloud.xcan.sdf.core.angustester.domain.func.cases;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X10;

import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class CaseTestStep {

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value = "Case testing step")
  private String step;

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value = "Case expected testing result")
  private String expectedResult;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CaseTestStep)) {
      return false;
    }
    CaseTestStep that = (CaseTestStep) o;
    return Objects.equals(step, that.step) &&
        Objects.equals(expectedResult, that.expectedResult);
  }

  @Override
  public int hashCode() {
    return Objects.hash(step, expectedResult);
  }
}
