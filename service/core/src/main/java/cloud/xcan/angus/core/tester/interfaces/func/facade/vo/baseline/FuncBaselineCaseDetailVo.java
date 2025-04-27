package cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestStep;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseInfoVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskInfoVo;
import cloud.xcan.angus.remote.NameJoinField;
import cloud.xcan.angus.remote.vo.IdAndNameVo;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class FuncBaselineCaseDetailVo extends FuncCaseDetailVo {

  private Long baselineId;

  /**
   * Note: case id
   */
  private Long id;

  private String name;

  private String code;

  private Integer version;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  private Long planId;

  @NameJoinField(id = "planId", repository = "funcPlanRepo")
  private String planName;

  private Long moduleId;

  @NameJoinField(id = "moduleId", repository = "moduleRepo")
  private String moduleName;

  private Priority priority;

  public LocalDateTime deadlineDate;

  private Boolean overdue;

  private EvalWorkloadMethod evalWorkloadMethod;

  private BigDecimal evalWorkload;

  private BigDecimal actualWorkload;

  private String precondition;

  private List<CaseTestStep> steps;

  private String description;

  private Boolean review;

  private Long reviewerId;

  @NameJoinField(id = "reviewerId", repository = "commonUserBaseRepo")
  private String reviewerName;

  private LocalDateTime reviewDate;

  private ReviewStatus reviewStatus;

  private String reviewRemark;

  private Integer reviewNum;

  private Integer reviewFailNum;

  private Long testerId;

  @NameJoinField(id = "testerId", repository = "commonUserBaseRepo")
  private String testerName;

  private Long developerId;

  @NameJoinField(id = "developerId", repository = "commonUserBaseRepo")
  private String developerName;

  private Integer testNum;

  private Integer testFailNum;

  private CaseTestResult testResult;

  private String testRemark;

  private LocalDateTime testResultHandleDate;

  private List<Attachment> attachments;

  private List<IdAndNameVo> tags;

  private List<TaskInfoVo> refTaskInfos;

  private List<FuncCaseInfoVo> refCaseInfos;

  private Boolean favourite;

  private Boolean follow;

  private int commentNum;

  private Long tenantId;

  //@NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  //private String tenantName;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private String avatar;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
