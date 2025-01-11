package cloud.xcan.sdf.core.angustester.domain.apis.unarchived;

import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.HttpAssertionConverter;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.ServerConverter;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.extension.ExtensionKey;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

/**
 * @author xiaolong.liu
 */
@Entity
@Table(name = "apis_unarchived")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Setter
@Getter
@Accessors(chain = true)
public class ApisUnarchived extends TenantAuditingEntity<ApisUnarchived, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  /**
   * @see Operation#getServers()
   */
  @Enumerated(EnumType.STRING)
  private ApisProtocol protocol;

  /**
   * @see PathItem
   */
  @Enumerated(EnumType.STRING)
  private HttpMethod method;

  /**
   * @see io.swagger.v3.oas.models.Paths#keySet()
   */
  private String endpoint;

  /////////////////////////OpenAPI Document//////////////////////////

  /**
   * @see Operation#getSummary()
   */
  private String summary;

  /**
   * @see Operation#getDescription()
   */
  private String description;

  /**
   * @see Operation#getParameters()
   */
  @Type(type = "json")
  @Column(columnDefinition = "json", name = "parameters")
  private List<Parameter> parameters;

  /**
   * @see Operation#getRequestBody()
   */
  @Type(type = "json")
  @Column(columnDefinition = "json", name = "request_body")
  private RequestBody requestBody;

  /**
   * @see Operation#getResponses()
   */
  @Convert(converter = ApiResponseConverter.class)
  @Column(name = "responses")
  private Map<String, ApiResponse> responses;

  /**
   * @see Operation#getSecurity()
   */
  @Type(type = "json")
  @Column(columnDefinition = "json", name = "security")
  private List<SecurityRequirement> security;

  @Convert(converter = ServerConverter.class)
  @Column(name = "current_server")
  private Server currentServer;

  //  /**
  //   * @see Operation#getServers()
  //   */
  //  @Type(type = "json")
  //  @Column(columnDefinition = "json", name = "servers")
  //  private List<Server> servers;

  /**
   * Include Extension:
   * <p>
   * - RequestSetting: Extension key in {@link Operation#getExtensions()} is
   * {@link ExtensionKey#REQUEST_SETTING_KEY}
   */
  @Type(type = "json")
  @Column(columnDefinition = "json", name = "extensions")
  private Map<String, Object> extensions;
  /////////////////////////OpenAPI Document//////////////////////////

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "authentication")
  private SecurityScheme authentication;

  @Convert(converter = HttpAssertionConverter.class)
  @Column(name = "assertions")
  private List<Assertion<HttpExtraction>> assertions;

  @Column(name = "security_flag")
  private Boolean securityFlag;

  @Override
  public Long identity() {
    return this.id;
  }

}
