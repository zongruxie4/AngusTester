package cloud.xcan.angus.core.tester.domain.project.evaluation;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.api.commonlink.tag.OrgTargetInfo;
import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "test_evaluation")
@DynamicInsert
@Setter
@Getter
@Accessors(chain = true)
public class TestEvaluation extends TenantAuditingEntity<TestEvaluation, Long> implements ActivityResource {

  @Id
  private Long id;

  private String name;

  @Column(name = "project_id")
  private Long projectId;

  @Enumerated(EnumType.STRING)
  @Column(name = "scope")
  private EvaluationScope scope;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "purposes")
  private List<EvaluationPurpose> purposes;

  @Column(name = "resource_id")
  private Long resourceId;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "deadline_date")
  private LocalDateTime deadlineDate;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "result")
  private TestEvaluationResult result;

  @Transient
  private String resourceName;

  @Override
  public Long getParentId() {
    return projectId;
  }

  @Override
  public Long getProjectId() {
    return projectId;
  }

  @Override
  public Long identity() {
    return this.id;
  }

}
