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
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Schema(example = "node1", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_NAME_LENGTH)
  private String name;

  @Schema(example = "192.168.10.11", requiredMode = RequiredMode.REQUIRED)
  @Length(max = IPV4_LENGTH)
  private String ip;

  @Length(max = IPV4_LENGTH)
  @Schema(example = "192.168.10.11")
  private String publicIp;

  @Length(max = MAX_DOMAIN_LENGTH)
  private String domain;

  @Schema(example = "root")
  @Length(max = MAX_NAME_LENGTH_X2)
  private String username;

  @Schema(example = "abc@123")
  @Length(max = MAX_CODE_LENGTH_X5)
  private String password;

  @Port
  @Schema(example = "22")
  private Integer sshPort;

  private Set<NodeRole> roles;

}




