package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.api.pojo.CorsData;
import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.sdf.model.remoting.MockServiceSetting;
import cloud.xcan.sdf.web.validator.annotations.Port;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockServiceServicesAssocDto {

  //  @NotNull
  //  @ApiModelProperty(required = true, value = "Import mock service associated services or sample type, fixed value: PROJECT, SAMPLE")
  //  @EnumPart(enumClass = MockServiceSource.class, allowableValues = {"PROJECT"})
  //  private MockServiceSource source;

  @NotNull
  @ApiModelProperty(value = "Services id", example = "1", required = true)
  private Long serviceId;

  @NotBlank
  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "The name of an existing projectProject(service)",
      example = "XCAN-ANGUSAPI.BOOT", required = true)
  private String name;

  @NotNull
  @ApiModelProperty(value = "Specify the mock service running node ID", example = "1", required = true)
  private Long nodeId;

  @NotNull
  @Port
  @ApiModelProperty(value = "Specify the port on which to run the service on the node", example = "80", required = true)
  private Integer servicePort;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(value = "The cloud service edition is fixed at xxx.angusmock.cloud, and the privatized version allows users to freely enter the domain name", example = "service1.angusmock.cloud")
  private String serviceDomain;

  //@NotEmpty
  @Size(min = 1)
  @ApiModelProperty(value = "Import a services's apis collection"/*, required = true*/)
  private HashSet<Long> apiIds;

  @Valid
  @ApiModelProperty(value = "Mock request authentication configuration")
  private List<SimpleHttpAuth> apisSecurity;

  @Valid
  @ApiModelProperty(value = "Mock request CORS configuration")
  private CorsData apisCors;

  @Valid
  //@NotNull
  @ApiModelProperty(value = "Mock service configuration")
  private MockServiceSetting setting;

}
