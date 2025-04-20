package cloud.xcan.angus.core.tester.domain.apis;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.converter.ApiResponseConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.HttpAssertionConverter;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.responses.ApiResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "apis")
@SQLRestriction("deleted = 0 ")
@SQLDelete(sql = "update apis set deleted = 1 where id = ?")
@Setter
@Getter
@Accessors(chain = true)
public class ApisBasicAndConfigInfo extends TenantAuditingEntity<ApisBasicAndConfigInfo, Long>
    implements ActivityResource {

  @Id
  public Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "service_id")
  public Long serviceId;

  @Enumerated(EnumType.STRING)
  public ApiSource source;

  @Enumerated(EnumType.STRING)
  @Column(name = "import_source")
  public ApiImportSource importSource;

  public String name;

  public String code;

  @Enumerated(EnumType.STRING)
  public HttpMethod method;

  public String host;

  @Enumerated(EnumType.STRING)
  private ApisProtocol protocol;

  public String uri;

  private String endpoint;

  public String description;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "authentication")
  private SecurityScheme authentication;

  @Convert(converter = HttpAssertionConverter.class)
  @Column(name = "assertions")
  private List<Assertion<HttpExtraction>> assertions;

  /**
   * @see Operation#getResponses()
   */
  @Convert(converter = ApiResponseConverter.class)
  @Column(name = "responses")
  private Map<String, ApiResponse> responses;

  @Column(name = "owner_id")
  public Long ownerId;

  @Enumerated(EnumType.STRING)
  public ApiStatus status;

  @Column(name = "auth")
  public Boolean auth;

  @Column(name = "security")
  public Boolean security;

  @Column(name = "deleted_by")
  public Long deletedBy;

  @Column(name = "deleted")
  public Boolean deleted;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "deleted_date", columnDefinition = "TIMESTAMP")
  public LocalDateTime deletedDate;

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    return serviceId;
  }

  /**
   * Note: Generated based on uri, modification not allowed.
   */
  private void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getEndpoint() {
    return /*nonNull(endpoint) ? endpoint : */ nonNull(uri) ? uri.split("\\?")[0] : "";
  }
}
