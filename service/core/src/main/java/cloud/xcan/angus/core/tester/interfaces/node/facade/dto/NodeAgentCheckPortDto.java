package cloud.xcan.angus.core.tester.interfaces.node.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.agent.message.CheckPortCmdParam;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class NodeAgentCheckPortDto {

  /**
   * Whether to notify other controllers to handle
   */
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private boolean broadcast;

  @Valid
  @NotEmpty
  @Size(min = 1, max = MAX_BATCH_SIZE)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private List<CheckPortCmdParam> cmdParams;

}
