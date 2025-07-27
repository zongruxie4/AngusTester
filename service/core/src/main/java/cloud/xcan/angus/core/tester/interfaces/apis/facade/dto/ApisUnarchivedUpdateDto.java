package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_DOC_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.spec.http.HttpMethod;
import com.fasterxml.jackson.annotation.JsonAnySetter;
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
public class ApisUnarchivedUpdateDto {

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Unarchived API identifier for update operation")
  private Long id;

  @Schema(description = "API protocol specification for communication type definition", example = "http")
  private ApisProtocol protocol;

  @Schema(description = "HTTP method for API request specification", example = "GET")
  private HttpMethod method;

  /**
   * Note:: Cannot include query parameters when saving.
   *
   * @see io.swagger.v3.oas.models.Paths#keySet()
   */
  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(example = "/comm/api/v1/country/{id}", description = "API endpoint path without query parameters for resource identification")
  private String endpoint;

  /////////////////////////OpenAPI Document//////////////////////////
  //  private List<String> tags;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @Schema(description = "API summary or name for identification and documentation", example = "Add user api")
  private String summary;

  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  @Schema(description = "API description with CommonMark syntax support for rich text representation")
  private String description;

  //  private ExternalDocumentation externalDocs;

  // private String operationId;

  @Size(max = MAX_PARAM_SIZE)
  @Schema(description = "API request parameters following OpenAPI Parameter Object specification")
  private List<Parameter> parameters;

  @Schema(description = "API request body following OpenAPI Request Body Object specification")
  private RequestBody requestBody;

  @Schema(description = "API responses following OpenAPI Response Object specification")
  private Map<String, ApiResponse> responses;

  //  private Boolean deprecated;

  //  private List<SecurityRequirement> security;

  @Schema(description = "Current server configuration for API connection and deployment information")
  private Server currentServer;

  /**
   * Include Extension:
   * <p>
   * - RequestSetting: Extension key in {@link Operation#getExtensions()} is
   * {@link ExtensionKey#REQUEST_SETTING_KEY}
   */
  @JsonAnySetter
  @Schema(description = "Extension fields for custom business attributes and configuration")
  private Map<String, Object> extensions = new HashMap<>();
  /////////////////////////OpenAPI Document//////////////////////////

  @Schema(description = "Security scheme definition following OpenAPI Security Scheme Object specification")
  private SecurityScheme authentication;

  @Valid
  @Size(max = MAX_PARAM_SIZE)
  @Schema(description = "HTTP assertion configuration for interface execution result validation")
  private List<Assertion<HttpExtraction>> assertions;

}




