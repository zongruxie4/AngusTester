package cloud.xcan.angus.core.tester.interfaces.func.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseWorkloadReplaceDto {

  @DecimalMin(value = "0.01")
  @DecimalMax(value = "" + MAX_WORKLOAD_NUM)
  @Schema(name = "workload", description = "Test case story points or work hours with clear functionality")
  private BigDecimal workload;
}
