package cloud.xcan.angus.core.tester.domain.apis.unarchived;

import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.apis.converter.ApiResponseConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.HttpAssertionConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.ServerConverter;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;


/**
 * @author XiaoLong Liu
 */@Entity
@Table(name = "apis_unarchived")
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
  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "parameters")
  private List<Parameter> parameters;

  /**
   * @see Operation#getRequestBody()
   */
  @Type(JsonType.class)
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
  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "security")
  private List<SecurityRequirement> security;

  @Convert(converter = ServerConverter.class)
  @Column(name = "current_server")
  private Server currentServer;

  //  /**
  //   * @see Operation#getServers()
  //   */
  //  @Type(JsonType.class)
  //  @Column(columnDefinition = "json", name = "servers")
  //  private List<Server> servers;

  /**
   * Include Extension:
   * <p>
   * - RequestSetting: Extension key in {@link Operation#getExtensions()} is
   * {@link ExtensionKey#REQUEST_SETTING_KEY}
   */
  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "extensions")
  private Map<String, Object> extensions;
  /////////////////////////OpenAPI Document//////////////////////////

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "authentication")
  private SecurityScheme authentication;

  @Convert(converter = HttpAssertionConverter.class)
  @Column(name = "assertions")
  private List<Assertion<HttpExtraction>> assertions;

  private Boolean secured;

  @Override
  public Long identity() {
    return this.id;
  }

}
