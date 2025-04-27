package cloud.xcan.angus.core.tester.domain.tag;

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
@Table(name = "tag")
@Setter
@Getter
@Accessors(chain = true)
public class Tag extends TenantAuditingEntity<Tag, Long> implements ActivityResource {

  @Id
  private Long id;

  private String name;

  @Column(name = "project_id")
  private Long projectId;

  @Transient
  private Boolean hasEditPermission = false;

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }
}
