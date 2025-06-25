package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_CODE_LENGTH_X5;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_DOC_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.model.element.ActionOnEOF;
import cloud.xcan.angus.model.element.SharingMode;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.spec.http.HttpMethod;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.extension.ExtensionKey;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ApisArchiveDto {

  @Schema(description = "Unarchived api id")
  private Long unarchivedId;

  @NotNull
  @Schema(description = "Service id to which the api belongs", requiredMode = RequiredMode.REQUIRED)
  private Long serviceId;

  @NotNull
  @Schema(example = "http", requiredMode = RequiredMode.REQUIRED)
  private ApisProtocol protocol;

  @NotNull
  @Schema(example = "GET", requiredMode = RequiredMode.REQUIRED)
  private HttpMethod method;

  /**
   * @see io.swagger.v3.oas.models.Paths#keySet()
   */
  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "Api endpoint. Note: Cannot include query parameters when saving.", example = "/comm/api/v1/country/{id}")
  private String endpoint;

  /////////////////////////OpenAPI Document//////////////////////////
  // private List<String> tags;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @Schema(description = "Api summary or name", example = "Add user api")
  private String summary;

  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  @Schema(description = "A description of the link. CommonMark syntax MAY be used for rich text representation.")
  private String description;

  // private ExternalDocumentation externalDocs;

  @Length(max = MAX_CODE_LENGTH_X5)
  @Schema(description = "The name of an existing, resolvable OAS operation, as defined with a unique operationId.")
  private String operationId;

  @Size(max = MAX_PARAM_SIZE)
  @Schema(description = "Api request parameters. See [OpenAPI Parameter Object](https://swagger.io/specification/v3/#parameter-object)")
  private List<Parameter> parameters;

  @Schema(description = "Api request body. See [OpenAPI Request Body Object](https://swagger.io/specification/v3/#request-body-object)")
  private RequestBody requestBody;

  @Schema(description = "Api responses. See [OpenAPI Response Object](https://swagger.io/specification/v3/#response-object)")
  private Map<String, ApiResponse> responses;

  //  private Boolean deprecated;

  //  private List<SecurityRequirement> security;

  @Schema(description = "Provides connection information between the current API and the target server. See [OpenAPI Server Object](https://swagger.io/specification/v3/#server-object)")
  private Server currentServer;

  /**
   * Include Extension:
   * <p>
   * - RequestSetting: Extension key in {@link Operation#getExtensions()} is
   * {@link ExtensionKey#REQUEST_SETTING_KEY}
   */
  @JsonAnyGetter
  @Schema(description = "Extension fields allow users to customize business extension attributes.")
  private Map<String, Object> extensions = new HashMap<>();
  /////////////////////////OpenAPI Document//////////////////////////

  @Schema(description = "Defines a security scheme that can be used by the operations. See [OpenAPI Security Scheme Object](https://swagger.io/specification/v3/#security-scheme-object)")
  private SecurityScheme authentication;

  @Valid
  @Size(max = MAX_PARAM_SIZE)
  @Schema(description = "Configure interface execution result assertion. See [AngusTester Http Assertion](https://www.xcan.cloud/en/docs/tester/specification/content/task/elements/http)")
  private List<Assertion<HttpExtraction>> assertions;

  @Schema(description = "Whether to enable authorization control, default disabled")
  private Boolean auth;

  @Schema(description = "Process actions when the dataset reaches the end of reading, default `RECYCLE`.")
  private ActionOnEOF datasetActionOnEOF;

  @Schema(description = "Dataset sharing mode when multi threads, default `ALL_THREAD`.")
  private SharingMode datasetSharingMode;

  @Schema(description = "Whether to enable functional testing, default enabled")
  private Boolean testFunc;

  @Schema(description = "Whether to enable performance testing, default enabled")
  private Boolean testPerf;

  @Schema(description = "Whether to enable stability testing, default enabled")
  private Boolean testStability;

  @NotNull
  @Schema(description = "Api owner or developer id", requiredMode = RequiredMode.REQUIRED)
  private Long ownerId;

  @NotNull
  @Schema(description = "Api status.", example = "DEV_COMPLETED", requiredMode = RequiredMode.REQUIRED)
  private ApiStatus status;
}
