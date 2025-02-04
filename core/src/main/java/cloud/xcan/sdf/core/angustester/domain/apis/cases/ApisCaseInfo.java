package cloud.xcan.sdf.core.angustester.domain.apis.cases;


import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.CaseTestMethod;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.HttpAssertionConverter;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.RequestBodyConverter;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.SecuritySchemeConverter;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.ServerConverter;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "apis_case")
@Where(clause = "apis_deleted_flag =0")
@Setter
@Getter
@Accessors(chain = true)
public class ApisCaseInfo extends TenantAuditingEntity<ApisCaseInfo, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "services_id")
  private Long servicesId;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "apis_id")
  private Long apisId;

  @Column(name = "enabled")
  private Boolean enabled;

  @Enumerated(EnumType.STRING)
  private ApisCaseType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "test_method")
  private CaseTestMethod testMethod;

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

  @Convert(converter = ServerConverter.class)
  @Column(name = "current_server")
  private Server currentServer;

  /**
   * @see Operation#getParameters()
   */
  @Type(type = "json")
  @Column(columnDefinition = "json", name = "parameters")
  private List<Parameter> parameters;

  /**
   * @see Operation#getRequestBody()
   */
  @Convert(converter = RequestBodyConverter.class)
  @Column(name = "request_body")
  private RequestBody requestBody;

  @Convert(converter = SecuritySchemeConverter.class)
  @Column(name = "authentication")
  private SecurityScheme authentication;

  @Convert(converter = HttpAssertionConverter.class)
  @Column(name = "assertions")
  private List<Assertion<HttpExtraction>> assertions;

  @Column(name = "apis_deleted_flag")
  private Boolean apisDeletedFlag;

  @Column(name = "exec_result")
  @Enumerated(EnumType.STRING)
  private Result execResult;

  @Column(name = "exec_failure_message")
  private String execFailureMessage;

  @Column(name = "exec_test_num")
  private Integer execTestNum;

  @Column(name = "exec_test_failure_num")
  private Integer execTestFailureNum;

  @Column(name = "exec_id")
  private Long execId;

  @Column(name = "exec_name")
  private String execName;

  @Column(name = "exec_by")
  private Long execBy;

  @Column(name = "exec_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime execDate;

  @Transient
  private String apisSummary;
  @Transient
  private Long apisServiceId;
  @Transient
  private int commentNum;
  @Transient
  private String createdByName;
  @Transient
  private String avatar;

  @Override
  public Long getParentId() {
    return apisId;
  }

  @Override
  public Long identity() {
    return this.id;
  }

}
