package cloud.xcan.angus.core.tester.interfaces.config.facade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class NodeAgentInstallDto {

  @Schema(description = "Device identifier for agent installation targeting", requiredMode = RequiredMode.REQUIRED)
  public Long deviceId;

}
