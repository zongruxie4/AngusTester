package cloud.xcan.sdf.core.angustester.domain.func.baseline;


import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "func_baseline")
@Setter
@Getter
@Accessors(chain = true)
public class FuncBaselineInfo extends TenantAuditingEntity<FuncBaselineInfo, Long>
    implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "plan_id")
  private Long planId;

  @Column(name = "description")
  private String description;

  @Column(name = "established_flag")
  private Boolean establishedFlag;

  @Transient
  private String createdByName;
  @Transient
  private String createdByAvatar;

  @Override
  public Long getParentId() {
    return projectId;
  }

  @Override
  public Long identity() {
    return this.id;
  }
}
