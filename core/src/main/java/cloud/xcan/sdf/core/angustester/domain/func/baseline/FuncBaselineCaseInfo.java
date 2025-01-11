package cloud.xcan.sdf.core.angustester.domain.func.baseline;


import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.ResourceTagAssoc;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestStep;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.tag.TagTarget;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.cases.TaskFuncCaseAssoc;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

@Entity
@Table(name = "func_baseline_case")
@Setter
@Getter
@Accessors(chain = true)
public class FuncBaselineCaseInfo extends TenantAuditingEntity<FuncBaselineCaseInfo, Long>
    implements ActivityResource, TaskFuncCaseAssoc<FuncBaselineCaseInfo, Long>,
    ResourceTagAssoc<FuncBaselineCaseInfo, Long> {

  @Id
  private Long id;

  @Column(name = "baseline_id")
  private Long baselineId;

  @Column(name = "case_id")
  private Long caseId;

  @Column(name = "name")
  private String name;

  @Column(name = "code")
  private String code;

  @Column(name = "version")
  private Integer version;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "plan_id")
  private Long planId;

  //@Column(name = "plan_auth_flag")
  //private Boolean planAuthFlag;

  @Column(name = "module_id")
  private Long moduleId;

  @Column(name = "priority")
  @Enumerated(EnumType.STRING)
  public Priority priority;

  @Column(name = "deadline_date", columnDefinition = "TIMESTAMP")
  public LocalDateTime deadlineDate;

  @Column(name = "overdue_flag")
  private Boolean overdueFlag;

  @Column(name = "eval_workload_method")
  @Enumerated(EnumType.STRING)
  private EvalWorkloadMethod evalWorkloadMethod;

  @Column(name = "eval_workload")
  private BigDecimal evalWorkload;

  @Column(name = "actual_workload")
  private BigDecimal actualWorkload;

  //  @Column(name = "precondition")
  //  private String precondition;

  @Column(name = "description")
  private String description;

  @Column(name = "review_flag")
  private Boolean reviewFlag;

  @Column(name = "reviewer_id")
  private Long reviewerId;

  @Column(name = "review_date")
  private LocalDateTime reviewDate;

  @Column(name = "review_status")
  @Enumerated(EnumType.STRING)
  private ReviewStatus reviewStatus;

  @Column(name = "review_remark")
  private String reviewRemark;

  @Column(name = "review_num")
  private Integer reviewNum;

  @Column(name = "review_fail_num")
  private Integer reviewFailNum;

  @Column(name = "tester_id")
  private Long testerId;

  @Column(name = "developer_id")
  private Long developerId;

  @Column(name = "test_num")
  private Integer testNum;

  @Column(name = "test_fail_num")
  private Integer testFailNum;

  @Column(name = "test_result")
  @Enumerated(EnumType.STRING)
  private CaseTestResult testResult;

  @Column(name = "test_remark")
  private String testRemark;

  @Column(name = "test_result_handle_date")
  private LocalDateTime testResultHandleDate;

  @Transient
  private String testerName;
  @Transient
  private String testerAvatar;
  @Transient
  private int commentNum;
  @Transient
  private Boolean favouriteFlag;
  @Transient
  private Boolean followFlag;
  @Transient
  private String createdByName;
  @Transient
  private String avatar;
  @Transient
  public List<TagTarget> tagTargets;
  @Transient
  private LinkedHashSet<Long> tagIds;
  @Transient// Only used by export
  private String precondition;
  @Transient// Only used by export
  private LinkedHashSet<CaseTestStep> steps;
  @Transient// Only used by export
  private List<TaskInfo> assocTasks;
  @Transient// Only used by export
  private List<FuncCaseInfo> assocCases;

  public boolean isReviewed() {
    return nonNull(reviewStatus) && reviewStatus.isPassed();
  }

  public boolean canReview() {
    return isNull(reviewStatus) || reviewStatus.isFailed() || reviewStatus.isPending();
  }

  @Override
  public Long getParentId() {
    return planId;
  }

  @Override
  public Long identity() {
    return this.id;
  }
}
