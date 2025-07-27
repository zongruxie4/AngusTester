package cloud.xcan.angus.core.tester.interfaces.func.facade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseTesterReplaceDto {

  @NotNull
  @Schema(description = "Tester identifier for case assignment and execution responsibility", requiredMode = RequiredMode.REQUIRED)
  private Long testerId;

}
