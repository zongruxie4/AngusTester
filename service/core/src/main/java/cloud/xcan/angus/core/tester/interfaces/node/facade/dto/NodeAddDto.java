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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class NodeAddDto {

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(example = "node1", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotBlank
  @Length(max = IPV4_LENGTH)
  @Schema(example = "192.168.10.11", requiredMode = RequiredMode.REQUIRED)
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
  private Integer sshPort;

  @NotEmpty
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Set<NodeRole> roles;

  @Schema(example = "1")
  private Long tenantId;

  @Schema(example = "1")
  private Boolean free;

}
