package cloud.xcan.angus.core.tester.domain.func.review;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "func_review")
@Setter
@Getter
@Accessors(chain = true)
public class FuncReview extends TenantAuditingEntity<FuncReview, Long> implements ActivityResource,
    IdAndCreatedDateBase<FuncReview> {

  @Id
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "plan_id")
  private Long planId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private FuncPlanStatus status;

  @Column(name = "owner_id")
  private Long ownerId;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "participant_ids")
  private LinkedHashSet<Long> participantIds;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "attachments")
  public List<Attachment> attachments;

  @Column(name = "description")
  private String description;

  @Transient
  private LinkedHashSet<Long> caseIds;
  @Transient
  private long caseNum;
  @Transient
  private Progress progress;
  @Transient
  private List<UserInfo> participants;
  @Transient
  private String ownerName;
  @Transient
  private String ownerAvatar;

  @Override
  public Long getParentId() {
    return projectId;
  }

  @Override
  public Long identity() {
    return id;
  }
}
