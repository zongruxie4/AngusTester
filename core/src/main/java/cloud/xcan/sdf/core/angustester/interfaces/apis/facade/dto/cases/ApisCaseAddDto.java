package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.cases;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PARAM_SIZE;

import cloud.xcan.angus.model.element.ActionOnEOF;
import cloud.xcan.angus.model.element.SharingMode;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisCaseAddDto {

  @NotNull
  @ApiModelProperty(value = "Case apis id", required = true)
  private Long apisId;

  @ApiModelProperty(value = "Enable test cases? default is enabled")
  private Boolean enabled;

  @ApiModelProperty(value = "Apis test cases type, default is USER_DEFINED")
  private ApisCaseType type;

  @NotBlank
  @Length(max = DEFAULT_NAME_LENGTH_X4)
  @ApiModelProperty(value = "Case name", required = true)
  private String name;

  @Length(max = DEFAULT_DESC_LENGTH_X4)
  @ApiModelProperty(value = "Case description")
  private String description;

  @NotNull
  @ApiModelProperty(example = "http", required = true)
  private ApisProtocol protocol;

  @NotNull
  @ApiModelProperty(example = "GET", required = true)
  private HttpMethod method;

  @Length(max = DEFAULT_URL_LENGTH_X4)
  @ApiModelProperty(example = "/comm/api/v1/country/{id}")
  private String endpoint;

  @ApiModelProperty(value = "Current apis server")
  private Server currentServer;

  @ApiModelProperty(value = "Current apis request parameters")
  private List<Parameter> parameters;

  @ApiModelProperty(value = "Current apis request body")
  private RequestBody requestBody;

  @ApiModelProperty(value = "Current apis request authentication")
  private SecurityScheme authentication;

  @Valid
  @Size(max = MAX_PARAM_SIZE)
  @ApiModelProperty(value = "Current apis assertions")
  private List<Assertion<HttpExtraction>> assertions;

  @ApiModelProperty(value = "Process actions when the dataset reaches the end of reading, default `RECYCLE`.")
  private ActionOnEOF datasetActionOnEOF;

  @ApiModelProperty(value = "Dataset sharing mode when multi threads, default `ALL_THREAD`.")
  private SharingMode datasetSharingMode;

}