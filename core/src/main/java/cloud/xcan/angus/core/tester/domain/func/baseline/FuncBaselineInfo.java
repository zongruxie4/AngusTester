package cloud.xcan.angus.core.tester.domain.func.baseline;


import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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

  @Column(name = "established")
  private Boolean established;

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
