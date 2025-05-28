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

  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Schema(example = "http")
  private ApisProtocol protocol;

  @Schema(example = "GET")
  private HttpMethod method;

  /**
   * Note:: Cannot include query parameters when saving.
   *
   * @see io.swagger.v3.oas.models.Paths#keySet()
   */
  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(example = "/comm/api/v1/country/{id}")
  private String endpoint;

  /////////////////////////OpenAPI Document//////////////////////////
  //  private List<String> tags;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  private String summary;

  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  private String description;

  //  private ExternalDocumentation externalDocs;

  // private String operationId;

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
  @JsonAnySetter
  private Map<String, Object> extensions = new HashMap<>();
  /////////////////////////OpenAPI Document//////////////////////////

  private SecurityScheme authentication;

  @Valid
  @Size(max = MAX_PARAM_SIZE)
  private List<Assertion<HttpExtraction>> assertions;

}




