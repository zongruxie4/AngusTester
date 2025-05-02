package cloud.xcan.angus.core.tester.interfaces.node.facade.dto;

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

  @Schema(requiredMode = RequiredMode.REQUIRED)
  public Long deviceId;

}
