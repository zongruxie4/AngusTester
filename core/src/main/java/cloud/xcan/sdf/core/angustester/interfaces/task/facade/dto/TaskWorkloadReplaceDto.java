package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import io.swagger.annotations.ApiParam;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TaskWorkloadReplaceDto {

  @DecimalMin(value = "0.01")
  @DecimalMax(value = "" + MAX_WORKLOAD_NUM)
  @ApiParam(name = "workload", value = "Task story point or work hours, allow clear story point or work hours by empty value")
  private BigDecimal workload;
}
