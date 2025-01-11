package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import cloud.xcan.sdf.web.validator.annotations.EnumPart;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseResultModifyDto {

  @NotNull
  @ApiModelProperty(value = "Functional testing case id", required = true)
  private Long id;

  @NotNull
  @EnumPart(enumClass = CaseTestResult.class, allowableValues = {
      "PASSED", "NOT_PASSED", "BLOCKED", "CANCELED"})
  @ApiModelProperty(value = "The result of functional testing case", required = true)
  private CaseTestResult testResult;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @ApiModelProperty(value = "Eval usage workload")
  private BigDecimal evalWorkload;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @ApiModelProperty(value = "Actual usage workload")
  private BigDecimal actualWorkload;

  @Length(max = DEFAULT_DESC_LENGTH_X2)
  @ApiModelProperty(value = "The remark of case test")
  private String testRemark;

}
