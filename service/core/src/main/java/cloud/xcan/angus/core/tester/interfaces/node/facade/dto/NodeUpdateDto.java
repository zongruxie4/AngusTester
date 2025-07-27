package cloud.xcan.angus.core.tester.interfaces.node.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.IPV4_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_CODE_LENGTH_X5;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DOMAIN_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.validator.Port;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class NodeUpdateDto {

  @NotNull
  @Schema(description = "Node identifier for update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Schema(description = "Node display name for identification and management", example = "node1", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_NAME_LENGTH)
  private String name;

  @Schema(description = "Private IP address for internal network communication", example = "192.168.10.11", requiredMode = RequiredMode.REQUIRED)
  @Length(max = IPV4_LENGTH)
  private String ip;

  @Length(max = IPV4_LENGTH)
  @Schema(description = "Public IP address for external network access", example = "192.168.10.11")
  private String publicIp;

  @Length(max = MAX_DOMAIN_LENGTH)
  @Schema(description = "Domain name for node identification and DNS resolution")
  private String domain;

  @Schema(description = "SSH username for node authentication and access", example = "root")
  @Length(max = MAX_NAME_LENGTH_X2)
  private String username;

  @Schema(description = "SSH password for node authentication and access", example = "abc@123")
  @Length(max = MAX_CODE_LENGTH_X5)
  private String password;

  @Port
  @Schema(description = "SSH port for secure shell connection to the node", example = "22")
  private Integer sshPort;

  @Schema(description = "Node roles defining its capabilities and responsibilities in the testing infrastructure")
  private Set<NodeRole> roles;

}




