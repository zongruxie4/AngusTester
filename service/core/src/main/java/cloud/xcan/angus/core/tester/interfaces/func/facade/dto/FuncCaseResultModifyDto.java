package cloud.xcan.angus.core.tester.interfaces.func.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import cloud.xcan.angus.validator.EnumPart;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseResultModifyDto {

  @NotNull
  @Schema(description = "Functional testing case id", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @NotNull
  @EnumPart(enumClass = CaseTestResult.class, allowableValues = {
      "PASSED", "NOT_PASSED", "BLOCKED", "CANCELED"})
  @Schema(description = "The result of functional testing case", requiredMode = RequiredMode.REQUIRED)
  private CaseTestResult testResult;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Eval usage workload")
  private BigDecimal evalWorkload;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Actual usage workload")
  private BigDecimal actualWorkload;

  @Length(max = MAX_DESC_LENGTH_X2)
  @Schema(description = "The remark of case test")
  private String testRemark;

}
