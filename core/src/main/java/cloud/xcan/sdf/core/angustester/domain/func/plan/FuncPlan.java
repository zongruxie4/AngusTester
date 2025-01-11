package cloud.xcan.sdf.core.angustester.domain.func.plan;


import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;


@Entity
@Table(name = "func_plan")
@Where(clause = "deleted_flag = 0")
@SQLDelete(sql = "update func_plan set deleted_flag = 1 where id = ?")
@Setter
@Getter
@Accessors(chain = true)
public class FuncPlan extends TenantAuditingEntity<FuncPlan, Long> implements ActivityResource,
    IdAndCreatedDateBase<FuncPlan> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "name")
  private String name;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private FuncPlanStatus status;

  @Column(name = "auth_flag")
  private Boolean authFlag;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "deadline_date")
  private LocalDateTime deadlineDate;

  @Column(name = "owner_id")
  private Long ownerId;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "tester_responsibilities")
  private LinkedHashMap<Long, String> testerResponsibilities;

  @Column(name = "testing_scope")
  private String testingScope;

  @Column(name = "testing_objectives")
  private String testingObjectives;

  @Column(name = "acceptance_criteria")
  private String acceptanceCriteria;

  @Column(name = "other_information")
  private String otherInformation;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "attachments")
  public List<Attachment> attachments;

  @Column(name = "case_prefix")
  private String casePrefix;

  @Column(name = "review_flag")
  private Boolean reviewFlag;

  @Column(name = "eval_workload_method")
  @Enumerated(EnumType.STRING)
  private EvalWorkloadMethod evalWorkloadMethod;

  @Column(name = "deleted_flag")
  private Boolean deletedFlag;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "deleted_date")
  private LocalDateTime deletedDate;

  @Transient
  private long caseNum;
  @Transient
  private long validCaseNum;
  @Transient
  private Progress progress;
  @Transient
  private List<UserInfo> members;
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
    return this.id;
  }

}
