package cloud.xcan.angus.core.tester.domain.issue;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.associate.AssociateUserType;
import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.angus.core.tester.domain.ResourceTagAssoc;
import cloud.xcan.angus.core.tester.domain.activity.MainTargetActivityResource;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCaseAssoc;
import cloud.xcan.angus.core.tester.domain.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.model.script.TestType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "task")
@SQLRestriction("deleted = 0 AND sprint_deleted =0")
@SQLDelete(sql = "update task set deleted = 1 where id = ?")
@Setter
@Getter
@Accessors(chain = true)
public class Task extends TenantAuditingEntity<Task, Long> implements MainTargetActivityResource,
    TaskAssociateUser, TaskFuncCaseAssoc<Task, Long>, ResourceFavouriteAndFollow<Task, Long>,
    ResourceTagAssoc<Task, Long> {

  @Id
  public Long id;

  public String name;

  public String code;

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
   * true: Product backlog, false: Sprint backlog or is general project management.
   */
  @Column(name = "backlog")
  private Boolean backlog;

  @Column(name = "task_type")
  @Enumerated(EnumType.STRING)
  private TaskType taskType;

  @Column(name = "bug_level")
  @Enumerated(EnumType.STRING)
  private BugLevel bugLevel;

  @Column(name = "priority")
  @Enumerated(EnumType.STRING)
  private Priority priority;

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

  @Column(name = "start_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime startDate;

  @Column(name = "deadline_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime deadlineDate;

  @Column(name = "overdue")
  private Boolean overdue;

  @Column(name = "canceled_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime canceledDate;

  @Column(name = "confirmed_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime confirmedDate;

  @Column(name = "completed_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime completedDate;

  @Column(name = "processed_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime processedDate;

  /**
   * The number of task processing failures, counting rules:
   *
   * <pre>
   * - Increment 1 when the task confirmation fails;
   * - Increment 1 when the test task execution test result fails.
   * </pre>
   */
  @Column(name = "fail_num")
  private Integer failNum;

  /**
   * The total number of tasks processed, counting rules:
   *
   * <pre>
   * Increment 1 when the task enters the start processing status.
   * </pre>
   */
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

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "attachments")
  public List<Attachment> attachments;

  public String description;

  @Column(name = "sprint_deleted")
  private Boolean sprintDeleted;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "deleted")
  private Boolean deleted;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "deleted_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime deletedDate;

  @Column(name = "created_by")
  //@CreatedBy // Fix: The imported value does not take effect
  private Long createdBy;

  @Column(name = "created_date")
  //@CreatedDate // Fix: The imported value does not take effect
  private LocalDateTime createdDate;

  @Transient
  private String assigneeName;
  @Transient
  private String assigneeAvatar;
  @Transient
  private List<TaskInfo> subTasks;
  @Transient
  private List<TaskInfo> assocTasks;
  @Transient
  private List<FuncCaseInfo> assocCases;
  @Transient
  private List<TagTarget> tagTargets;
  @Transient
  private String targetName;
  @Transient
  private String targetParentName;
  @Transient
  private List<AssociateUserType> currentAssociateType;
  @Transient
  private int commentNum;
  @Transient
  private int remarkNum;
  @Transient
  private int activityNum;
  @Transient
  private Boolean favourite;
  @Transient
  private Boolean follow;
  @Transient
  private LinkedHashSet<Long> refTaskIds;
  @Transient
  private LinkedHashSet<Long> refCaseIds;
  @Transient
  private Progress progress;

  public boolean isConfirmTask() {
    return nonNull(confirmerId);
  }

  public boolean hasTag() {
    return isNotEmpty(tagTargets);
  }

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getMainTargetId() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    return nullSafe(this.sprintId, this.projectId);
  }

  @Override
  public Long getTaskId() {
    return this.id;
  }
}
