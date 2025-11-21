package cloud.xcan.angus.core.tester.domain.issue;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.angus.core.tester.domain.activity.MainTargetActivityResource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.format.annotation.DateTimeFormat;


@Entity
@Table(name = "task")
@SQLRestriction("deleted = 0 AND sprint_deleted =0")
@SQLDelete(sql = "update task set deleted = 1 where id = ?")
@Setter
@Getter
@Accessors(chain = true)
public class TaskInfo extends TenantAuditingEntity<TaskInfo, Long> implements
    MainTargetActivityResource, TaskAssociateUser, ResourceFavouriteAndFollow<TaskInfo, Long> {

  @Id
  private Long id;

  private String name;

  private String code;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "sprint_id")
  private Long sprintId;

  @Column(name = "sprint_auth")
  private Boolean sprintAuth;

  @Column(name = "module_id")
  private Long moduleId;

  @Column(name = "software_version")
  private String softwareVersion;

  /**
   * true: Product backlog, false: Sprint backlog.
   */
  @Column(name = "backlog")
  private Boolean backlog;

  @Column(name = "priority")
  @Enumerated(EnumType.STRING)
  private Priority priority;

  @Column(name = "task_type")
  @Enumerated(EnumType.STRING)
  private TaskType taskType;

  @Column(name = "bug_level")
  @Enumerated(EnumType.STRING)
  private BugLevel bugLevel;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private TaskStatus status;

  @Column(name = "assignee_id")
  private Long assigneeId;

  @Column(name = "confirmer_id")
  private Long confirmerId;

  @Column(name = "tester_id")
  private Long testerId;

  @Column(name = "missing_bug")
  private Boolean missingBug;

  @Column(name = "unplanned")
  private Boolean unplanned;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "start_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime startDate;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "deadline_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime deadlineDate;

  @Column(name = "overdue")
  private Boolean overdue;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "canceled_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime canceledDate;

  @Column(name = "confirmed_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime confirmedDate;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "completed_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime completedDate;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "processed_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime processedDate;

  @Column(name = "fail_num")
  private Integer failNum;

  @Column(name = "total_num")
  private Integer totalNum;

  @Column(name = "eval_workload_method")
  @Enumerated(EnumType.STRING)
  private EvalWorkloadMethod evalWorkloadMethod;

  @Column(name = "eval_workload")
  private BigDecimal evalWorkload;

  @Column(name = "actual_workload")
  private BigDecimal actualWorkload;

  @Column(name = "parent_task_id")
  private Long parentTaskId;

  //  @Type(JsonType.class)
  //  @Column(columnDefinition = "json", name = "ref_task_ids")
  //  private LinkedHashSet<Long> refTaskIds;

  //  @Type(JsonType.class)
  //  @Column(columnDefinition = "json", name = "attachments_data")
  //  private List<Attachment> attachmentsData;
  //
  //  @Column(name = "description")
  //  private String description;

  @Column(name = "sprint_deleted")
  private Boolean sprintDeleted;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "deleted")
  private Boolean deleted;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "deleted_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime deletedDate;

  @Transient
  private String assigneeName;
  @Transient
  private String assigneeAvatar;
  @Transient
  private transient List<TagTarget> tagTargets;
  @Transient
  private transient String targetName;
  @Transient
  private transient String targetParentName;
  @Transient
  private Boolean favourite;
  @Transient
  private Boolean follow;
  @Transient
  private Progress progress;

  public boolean isConfirmTask() {
    return nonNull(confirmerId);
  }

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    // return this.sprintId  <- Non-agile projects are not supported
    return nullSafe(this.sprintId, this.projectId);
  }

  @Override
  public Long getMainTargetId() {
    return this.id;
  }

  @Override
  public Long getTaskId() {
    return this.id;
  }
}
