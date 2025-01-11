package cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_CODE_LENGTH_X5;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.IPV4_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_DOMAIN_LENGTH;

import cloud.xcan.sdf.api.enums.NodeRole;
import cloud.xcan.sdf.web.validator.annotations.Port;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.Valid;
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
public class NodeUpdateDto {

  @NotNull
  @ApiModelProperty(required = true)
  private Long id;

  @ApiModelProperty(example = "node1", required = true)
  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  @ApiModelProperty(example = "192.168.10.11", required = true)
  @Length(max = IPV4_LENGTH)
  private String ip;

  @Length(max = IPV4_LENGTH)
  @ApiModelProperty(example = "192.168.10.11")
  private String publicIp;

  @Length(max = MAX_DOMAIN_LENGTH)
  private String domain;

  @ApiModelProperty(example = "root")
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String username;

  @ApiModelProperty(example = "abc@123")
  @Length(max = DEFAULT_CODE_LENGTH_X5)
  private String passd;

  @Port
  @ApiModelProperty(example = "22")
  private Integer sshPort;

  private Set<NodeRole> roles;

}




