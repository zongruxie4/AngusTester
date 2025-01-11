package cloud.xcan.sdf.core.angustester.domain.module;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_ROOT_PID;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.NameJoinField;
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
  private Boolean hitFlag = false;
  @Transient
  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

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