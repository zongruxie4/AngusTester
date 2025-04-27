package cloud.xcan.angus.core.tester.domain.func.baseline;


import static java.util.Objects.isNull;

import cloud.xcan.angus.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
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

  private String name;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "plan_id")
  private Long planId;

  private String description;

  private Boolean established;

  @Type(JsonType.class)
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
