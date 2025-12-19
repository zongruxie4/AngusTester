package cloud.xcan.angus.core.tester.domain.project.module;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_ROOT_PID;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.remote.NameJoinField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "module")
@Setter
@Getter
@Accessors(chain = true)
public class Module extends TenantAuditingEntity<Module, Long> implements ActivityResource {

  @Id
  private Long id;

  private String name;

  @Column(name = "project_id")
  private Long projectId;

  private Long pid;

  private Integer sequence;

  @Transient
  private Boolean hasEditPermission = false;
  @Transient
  private Boolean hit = false;
  @Transient
  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String creator;

  public boolean hasParent() {
    return nonNull(pid) && !pid.equals(DEFAULT_ROOT_PID);
  }

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }
}
