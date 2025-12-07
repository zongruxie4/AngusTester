package cloud.xcan.angus.core.tester.interfaces.config.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class NodeAgentStatusQueryDto {

  /**
   * Whether to notify other controllers to handle
   */
  @NotNull
  @Schema(description = "Flag to enable broadcast mode for multi-controller coordination", requiredMode = RequiredMode.REQUIRED)
  private Boolean broadcast;

  @Valid
  @NotEmpty
  @Size(min = 1, max = MAX_BATCH_SIZE)
  @Schema(description = "List of node identifiers for batch agent status query", requiredMode = RequiredMode.REQUIRED)
  private List<Long> nodeIds;

}
