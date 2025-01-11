package cloud.xcan.sdf.core.angustester.domain.func.review;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanStatus;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.util.LinkedHashSet;
import java.util.List;
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

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "participant_ids")
  private LinkedHashSet<Long> participantIds;

  @Type(type = "json")
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
