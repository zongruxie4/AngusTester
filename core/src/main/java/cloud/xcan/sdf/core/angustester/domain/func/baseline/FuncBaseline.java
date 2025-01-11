package cloud.xcan.sdf.core.angustester.domain.func.baseline;


import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.util.LinkedHashSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "func_baseline")
@Setter
@Getter
@Accessors(chain = true)
public class FuncBaseline extends TenantAuditingEntity<FuncBaseline, Long> implements
    ActivityResource, IdAndCreatedDateBase<FuncBaseline> {

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

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "case_ids")
  private LinkedHashSet<Long> caseIds;

  public LinkedHashSet<Long> getCaseIds() {
    if (isNull(caseIds)) {
      caseIds = new LinkedHashSet<>();
    }
     return caseIds;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }

  @Override
  public Long identity() {
    return this.id;
  }
}
