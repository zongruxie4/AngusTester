package cloud.xcan.angus.core.tester.interfaces.config.facade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class NodeRunnerKillDto {

  /**
   * Whether to notify other controllers to handle
   */
  @Schema(description = "Flag to enable broadcast mode for multi-controller coordination", requiredMode = RequiredMode.REQUIRED)
  private boolean broadcast = true;

  @NotNull
  @Schema(description = "Node identifier for runner process termination", requiredMode = RequiredMode.REQUIRED)
  private Long nodeId;

  @NotNull
  @Schema(description = "Process identifier for specific runner process termination", requiredMode = RequiredMode.REQUIRED)
  private Integer pid;

}
