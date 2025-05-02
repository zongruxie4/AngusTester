package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class ExecStartDto {

  @Schema(description = "Whether to notify other controllers to handle", requiredMode = RequiredMode.REQUIRED)
  private boolean broadcast;

  @NotNull
  @Schema(description = "Execution id", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Valid
  @Size(min = 1, max = MAX_BATCH_SIZE)
  private LinkedHashSet<Long> remoteNodeIds;

  private Long lastNodeId;

}
