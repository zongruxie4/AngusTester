package cloud.xcan.sdf.core.angustester.domain.apis.summary;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import cloud.xcan.sdf.spec.http.HttpMethod;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ApisDetailSummary {

  private Long id;

  private ApiSource source;

  private ApiImportSource importSource;

  private Long projectId;

  @NameJoinField(id = "projectId", repository = "projectRepo")
  private String projectName;

  private Long serviceId;

  @NameJoinField(id = "serviceId", repository = "servicesRepo")
  private String serviceName;

  @ApiModelProperty(example = "http")
  private ApisProtocol protocol;

  @ApiModelProperty(example = "GET")
  private HttpMethod method;

  @DoInFuture("Add endpoint field")
  @ApiModelProperty(example = "/comm/api/v1/country/{id}")
  private String endpoint;

  /////////////////////////OpenAPI Document//////////////////////////
  private List<String> tags;

  private String summary;

  private String description;

  private ExternalDocumentation externalDocs;

  private String operationId;

  private List<Parameter> parameters;

  private RequestBody requestBody;

  private Map<String, ApiResponse> responses;

  private Boolean deprecated;

  private List<SecurityRequirement> security;

  @ApiModelProperty(notes = "Available server urls. The data source includes the current request server, api servers, and parent services servers.")
  private List<Server> availableServers;

  private Map<String, Object> extensions;
  /////////////////////////OpenAPI Document//////////////////////////

  private SecurityScheme authentication;

  private List<Assertion<HttpExtraction>> assertions;

  private ApiStatus status;

  private Map<String, Tag> tagSchemas;

  private Long ownerId;

  @NameJoinField(id = "ownerId", repository = "commonUserBaseRepo")
  private String ownerName;

  private Boolean favouriteFlag;

  private Boolean followFlag;

  private Boolean authFlag;

  private Boolean serviceAuthFlag;

  private Boolean securityFlag;

  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testFuncFlag;

  private Boolean testFuncPassedFlag;

  private String testFuncFailureMessage;

  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testPerfFlag;

  private Boolean testPerfPassedFlag;

  private String testPerfFailureMessage;

  /**
   * Whether to enable stability testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testStabilityFlag;

  private Boolean testStabilityPassedFlag;

  private String testStabilityFailureMessage;

  private String syncName;

  private Map<String, String> resolvedRefModels;

  private Long tenantId;

  @JsonAnyGetter
  public Map<String, Object> getExtensions() {
    return extensions;
  }

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime lastModifiedDate;

}
