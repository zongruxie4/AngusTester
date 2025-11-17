package cloud.xcan.angus.core.tester.domain.test.cases;


import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.angus.core.tester.domain.ResourceTagAssoc;
import cloud.xcan.angus.core.tester.domain.activity.MainTargetActivityResource;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCaseAssoc;
import cloud.xcan.angus.core.tester.domain.tag.TagTarget;
import cloud.xcan.angus.core.tester.domain.test.TestLayer;
import cloud.xcan.angus.core.tester.domain.test.TestPurpose;
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

@Entity
@Table(name = "func_case")
@SQLRestriction("deleted = 0 AND plan_deleted =0")
@SQLDelete(sql = "update func_case set deleted = 1 where id = ?")
@Setter
@Getter
@Accessors(chain = true)
public class FuncCase extends TenantAuditingEntity<FuncCase, Long> implements
    MainTargetActivityResource, TaskFuncCaseAssoc<FuncCase, Long>,
    ResourceFavouriteAndFollow<FuncCase, Long>, ResourceTagAssoc<FuncCase, Long> {

  @Id
  private Long id;

  private String name;

  private String code;

  private Integer version;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "plan_id")
  private Long planId;

  @Column(name = "plan_auth")
  private Boolean planAuth;

  @Column(name = "module_id")
  private Long moduleId;

  @Column(name = "software_version")
  private String softwareVersion;

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

  @Enumerated(EnumType.STRING)
  @Column(name = "test_layer")
  private TestLayer testLayer = TestLayer.UI;

  @Enumerated(EnumType.STRING)
  @Column(name = "test_purpose")
  private TestPurpose testPurpose = TestPurpose.FUNCTIONAL;

  @Column(name = "precondition")
  private String precondition;

  @Enumerated(EnumType.STRING)
  @Column(name = "step_view")
  private CaseStepView stepView;

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

  @Column(name = "unplanned")
  private Boolean unplanned;

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

  @Column(name = "plan_deleted")
  private Boolean planDeleted;

  @Column(name = "deleted")
  private Boolean deleted;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "deleted_date")
  private LocalDateTime deletedDate;

  @Column(name = "created_by")
  //@CreatedBy // Fix: The imported value does not take effect
  private Long createdBy;

  @Column(name = "created_date")
  //@CreatedDate // Fix: The imported value does not take effect
  private LocalDateTime createdDate;

  @Transient
  private String testerName;
  @Transient
  private String testerAvatar;
  @Transient
  private int commentNum;
  @Transient
  private int activityNum;
  @Transient
  private Boolean favourite;
  @Transient
  private Boolean follow;
  @Transient
  private String createdByName;
  @Transient
  private String avatar;
  @Transient
  private List<TaskInfo> assocTasks;
  @Transient
  private List<FuncCaseInfo> assocCases;
  @Transient
  public List<TagTarget> tagTargets;
  @Transient
  public LinkedHashSet<Long> tagIds;
  @Transient
  private LinkedHashSet<Long> refTaskIds;
  @Transient
  private LinkedHashSet<Long> refCaseIds;
  @Transient
  private Progress progress;

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

  @Override
  public Long getMainTargetId() {
    return this.id;
  }
}
