package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_DOMAIN_LENGTH;

import cloud.xcan.sdf.api.pojo.CorsData;
import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.sdf.model.remoting.MockServiceSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
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
public class MockServiceUpdateDto {

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(example = "XCAN-ANGUSAPI.BOOT")
  private String name;

  //Modification of node is not allowed
  //@NotNull
  //@ApiModelProperty(value="Specify the mock service running node ID", example = "1"/*, required = true*/)
  //private Long nodeId;

  //Modification of node port is not allowed
  //@NotNull
  //@Port
  //@ApiModelProperty(value = "Specify the port on which to run the service on the node", example = "80", required = true)
  //private Integer servicePort;

  @Length(max = MAX_DOMAIN_LENGTH)
  @ApiModelProperty(value = "The cloud service edition is fixed at xxx.angusmock.cloud, and the privatized version allows users to freely enter the domain name", example = "service1.angusmock.cloud")
  private String serviceDomain;

  @Valid
  @ApiModelProperty(value = "Mock request authentication configuration")
  private List<SimpleHttpAuth> apisSecurity;

  @Valid
  @ApiModelProperty(value = "Mock request CORS configuration")
  private CorsData apisCors;

  @Valid
  @ApiModelProperty(value = "Mock service configuration")
  private MockServiceSetting setting;
}
