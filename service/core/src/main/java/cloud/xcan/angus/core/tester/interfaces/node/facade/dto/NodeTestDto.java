package cloud.xcan.angus.core.tester.interfaces.node.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.IPV4_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_CODE_LENGTH_X5;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.validator.Port;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class NodeTestDto {

  @NotBlank
  @Schema(example = "192.168.10.11", requiredMode = RequiredMode.REQUIRED)
  @Length(max = IPV4_LENGTH)
  private String ip;

  @NotBlank
  @Schema(example = "root", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_NAME_LENGTH_X2)
  private String username;

  @NotBlank
  @Schema(example = "abc@123", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_CODE_LENGTH_X5)
  private String password;

  @NotNull
  @Port
  @Schema(example = "22", requiredMode = RequiredMode.REQUIRED)
  private Integer sshPort;

}




