package cloud.xcan.sdf.core.angustester.domain.apis.design;

import static java.util.Objects.nonNull;

import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
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

  @Column(name = "release_flag")
  private Boolean releaseFlag;

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
    return releaseFlag && designSource.isSynchronousService() && nonNull(designSourceId);
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
