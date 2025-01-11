package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.http.HttpMethod;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
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

@Getter
@Setter
@Accessors(chain = true)
public class ApisShareApisDetailVo {

  private Long id;

  private ApiSource source;

  private ApiImportSource importSource;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  @ApiModelProperty(example = "http")
  private ApisProtocol protocol;

  @ApiModelProperty(example = "GET")
  private HttpMethod method;

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

  private ApiStatus status;

  private Map<String, String> resolvedRefModels;

  private Map<String, Tag> tagSchemas;

  private Long ownerId;

  @NameJoinField(id = "ownerId", repository = "commonUserBaseRepo")
  private String ownerName;

  private Boolean favouriteFlag;

  private Boolean followFlag;

  private Long tenantId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private LocalDateTime lastModifiedDate;

  @JsonAnyGetter
  public Map<String, Object> getExtensions() {
    return extensions;
  }

}
