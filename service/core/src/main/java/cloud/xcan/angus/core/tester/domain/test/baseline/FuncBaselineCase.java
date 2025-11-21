package cloud.xcan.angus.core.tester.domain.test.baseline;


import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.ResourceTagAssoc;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCaseAssoc;
import cloud.xcan.angus.core.tester.domain.project.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestStep;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
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
import org.hibernate.annotations.Type;

@Entity
@Table(name = "func_baseline_case")
@Setter
@Getter
@Accessors(chain = true)
public class FuncBaselineCase extends TenantAuditingEntity<FuncBaselineCase, Long> implements
    ActivityResource, TaskFuncCaseAssoc<FuncBaselineCase, Long>,
    ResourceTagAssoc<FuncBaselineCase, Long> {

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

  //@Column(name = "plan_auth")
  //private Boolean planAuth;

  @Column(name = "module_id")
  private Long moduleId;

  @Column(name = "priority")
  @Enumerated(EnumType.STRING)
  public Priority priority;

  @Column(name = "deadline_date", columnDefinition = "TIMESTAMP")
  public LocalDateTime deadlineDate;

  @Column(name = "overdue")
  private Boolean overdue;

  @Column(name = "eval_workload_method")
  @Enumerated(EnumType.STRING)
  private EvalWorkloadMethod evalWorkloadMethod;

  @Column(name = "eval_workload")
  private BigDecimal evalWorkload;

  @Column(name = "actual_workload")
  private BigDecimal actualWorkload;

  @Column(name = "precondition")
  private String precondition;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "steps")
  private List<CaseTestStep> steps;

  @Column(name = "description")
  private String description;

  @Column(name = "review")
  private Boolean review;

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

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "attachments")
  public List<Attachment> attachments;

  @Column(name = "created_by")
  //@CreatedBy // Fix: The imported value does not take effect
  private Long createdBy;

  @Column(name = "created_date")
  //@CreatedDate // Fix: The imported value does not take effect
  private LocalDateTime createdDate;

  @Transient
  private int commentNum;
  @Transient
  private Boolean favourite;
  @Transient
  private Boolean follow;
  @Transient
  private String createdByName;
  @Transient
  private String avatar;
  @Transient
  public List<TagTarget> tagTargets;
  @Transient
  public LinkedHashSet<Long> tagIds;
  @Transient// Only used by export
  private List<TaskInfo> assocTasks;
  @Transient// Only used by export
  private List<FuncCaseInfo> assocCases;

  public boolean canReview() {
    return isNull(reviewStatus) || reviewStatus.isFailed() || reviewStatus.isPending();
  }

  public boolean isReviewPending() {
    return nonNull(reviewStatus) && reviewStatus.isPending();
  }

  public boolean isReviewed() {
    return nonNull(reviewStatus) && reviewStatus.isPassed();
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
