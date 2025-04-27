package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_SIZE;

import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ApisUnarchivedDetailVo {

  private Long id;

  private Long projectId;

  @Schema(example = "http")
  private ApisProtocol protocol;

  @Schema(example = "GET")
  private String method;

  @Schema(example = "/comm/api/v1/country/{id}")
  private String endpoint;

  /////////////////////////OpenAPI Document//////////////////////////
  //  private List<String> tags;

  private String summary;

  private String description;

  //  private ExternalDocumentation externalDocs;

  private String operationId;

  private List<Parameter> parameters;

  private RequestBody requestBody;

  private Map<String, ApiResponse> responses;

  //  private Boolean deprecated;

  private List<SecurityRequirement> security;

  private Server currentServer;

  private Map<String, Object> extensions;
  /////////////////////////OpenAPI Document//////////////////////////

  private SecurityScheme authentication;

  @Size(max = MAX_PARAM_SIZE)
  private List<Assertion<HttpExtraction>> assertions;

  @Schema(description = "Whether to enable authorization control, default enabled")
  private Boolean auth;

  @Schema(description = "Authentication flag")
  private Boolean secured;

  private Long tenantId;

  //@NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  //private String tenantName;

  private Long createdBy;

  //@NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  //private String createdByName;

  private LocalDateTime createdDate;

  private LocalDateTime lastModifiedDate;

  @JsonAnyGetter
  public Map<String, Object> getExtensions() {
    return extensions;
  }
}



