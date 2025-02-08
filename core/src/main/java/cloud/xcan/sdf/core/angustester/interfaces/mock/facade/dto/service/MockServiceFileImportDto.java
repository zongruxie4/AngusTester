package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.api.pojo.CorsData;
import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.model.remoting.MockServiceSetting;
import cloud.xcan.sdf.web.validator.annotations.EnumPart;
import cloud.xcan.sdf.web.validator.annotations.Port;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockServiceFileImportDto {

  @NotNull
  @ApiModelProperty(value = "Project ID, required when creating a service", required = true)
  private Long projectId;

  @NotNull
  @EnumPart(enumClass = ApiImportSource.class, allowableValues = {"OPENAPI", "POSTMAN", "ANGUS"})
  @ApiModelProperty(value = "Import the source of the mock service", example = "OPENAPI", required = true)
  private ApiImportSource importType;

  @ApiModelProperty(value = "Apis document file, the apis documents file and text must specify one, and only import file when both exist")
  private MultipartFile file;

  @ApiModelProperty(value = "Apis document text, the apis documents file and text must specify one, and only import file when both exist")
  private String text;

  @NotBlank
  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Mock service name", example = "MockApis", required = true)
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
