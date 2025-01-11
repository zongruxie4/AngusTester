package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_CODE_LENGTH_X5;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_OPENAPI_DOC_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PARAM_SIZE;

import cloud.xcan.angus.model.element.ActionOnEOF;
import cloud.xcan.angus.model.element.SharingMode;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.http.HttpMethod;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.extension.ExtensionKey;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
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
public class ApisArchiveDto {

  @ApiModelProperty(example = "1", value = "Unarchived api id")
  private Long unarchiveId;

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long serviceId;

  @NotNull
  @ApiModelProperty(example = "http", required = true)
  private ApisProtocol protocol;

  @NotNull
  @ApiModelProperty(example = "GET", required = true)
  private HttpMethod method;

  /**
   * Note:: Cannot include query parameters when saving.
   *
   * @see io.swagger.v3.oas.models.Paths#keySet()
   */
  @Length(max = DEFAULT_URL_LENGTH_X4)
  @ApiModelProperty(example = "/comm/api/v1/country/{id}")
  private String endpoint;

  /////////////////////////OpenAPI Document//////////////////////////
  // private List<String> tags;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  private String summary;

  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  private String description;

  // private ExternalDocumentation externalDocs;

  @Length(max = DEFAULT_CODE_LENGTH_X5)
  private String operationId;

  @Size(max = MAX_PARAM_SIZE)
  private List<Parameter> parameters;

  private RequestBody requestBody;

  private Map<String, ApiResponse> responses;

  //  private Boolean deprecated;

  //  private List<SecurityRequirement> security;

  private Server currentServer;

  /**
   * Include Extension:
   * <p>
   * - RequestSetting: Extension key in {@link Operation#getExtensions()} is
   * {@link ExtensionKey#REQUEST_SETTING_KEY}
   */
  @JsonAnyGetter
  private Map<String, Object> extensions = new HashMap<>();
  /////////////////////////OpenAPI Document//////////////////////////

  private SecurityScheme authentication;

  @Valid
  @Size(max = MAX_PARAM_SIZE)
  private List<Assertion<HttpExtraction>> assertions;

  @ApiModelProperty(value = "Whether to enable authorization control, default disabled")
  private Boolean authFlag;

  @ApiModelProperty(value = "Process actions when the dataset reaches the end of reading, default `RECYCLE`.")
  private ActionOnEOF datasetActionOnEOF;

  @ApiModelProperty(value = "Dataset sharing mode when multi threads, default `ALL_THREAD`.")
  private SharingMode datasetSharingMode;

  @ApiModelProperty(value = "Whether to enable functional testing, default enabled")
  private Boolean testFuncFlag;

  @ApiModelProperty(value = "Whether to enable performance testing, default enabled")
  private Boolean testPerfFlag;

  @ApiModelProperty(value = "Whether to enable stability testing, default enabled")
  private Boolean testStabilityFlag;

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long ownerId;

  @NotNull
  @ApiModelProperty(example = "DEV_COMPLETED", required = true)
  private ApiStatus status;
}
