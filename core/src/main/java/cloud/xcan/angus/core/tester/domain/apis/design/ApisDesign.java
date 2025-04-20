package cloud.xcan.angus.core.tester.domain.apis.design;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "apis_design")
@Setter
@Getter
@Accessors(chain = true)
public class ApisDesign extends TenantAuditingEntity<Apis, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  private String name;

  @Column(name = "release")
  private Boolean release;

  @Column(name = "openapi_spec_version")
  private String openapiSpecVersion;

  private String openapi;

  @Enumerated(EnumType.STRING)
  @Column(name = "design_source")
  private ApisDesignSource designSource;

  @Column(name = "design_source_id")
  private Long designSourceId;

  @Transient
  private String latestOpenapi;
  @Transient
  private String designSourceName;

  public boolean hasLatestContent(){
    return release && designSource.isSynchronousService() && nonNull(designSourceId);
  }

  @Override
  public Long getParentId() {
    return projectId;
  }

  @Override
  public Long identity() {
    return id;
  }
}
