package cloud.xcan.angus.core.tester.interfaces.node.facade.dto;

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
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private boolean broadcast = true;

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long nodeId;

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Integer pid;

}
