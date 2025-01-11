package cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_CODE_LENGTH_X5;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.IPV4_LENGTH;

import cloud.xcan.sdf.web.validator.annotations.Port;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class NodeTestDto {

  @NotBlank
  @ApiModelProperty(example = "192.168.10.11", required = true)
  @Length(max = IPV4_LENGTH)
  private String ip;

  @NotBlank
  @ApiModelProperty(example = "root", required = true)
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String username;

  @NotBlank
  @ApiModelProperty(example = "abc@123", required = true)
  @Length(max = DEFAULT_CODE_LENGTH_X5)
  private String passd;

  @NotNull
  @Port
  @ApiModelProperty(example = "22", required = true)
  private Integer sshPort;

}




