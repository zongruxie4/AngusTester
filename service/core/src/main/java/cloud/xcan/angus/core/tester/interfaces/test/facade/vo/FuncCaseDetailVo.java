package cloud.xcan.angus.core.tester.interfaces.test.facade.vo;


import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.tester.domain.test.TestLayer;
import cloud.xcan.angus.core.tester.domain.test.TestPurpose;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseStepView;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestStep;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.TaskInfoVo;
import cloud.xcan.angus.remote.NameJoinField;
import cloud.xcan.angus.remote.vo.IdAndNameVo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseDetailVo {

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

  private Boolean planAuth;

  private Long moduleId;

  @NameJoinField(id = "moduleId", repository = "moduleRepo")
  private String moduleName;

  private String softwareVersion;

  private Priority priority;

  public LocalDateTime deadlineDate;

  private Boolean overdue;

  private EvalWorkloadMethod evalWorkloadMethod;

  private BigDecimal evalWorkload;

  private BigDecimal actualWorkload;

  private TestLayer testLayer;

  private TestPurpose testPurpose;

  private String precondition;

  private CaseStepView stepView;

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

  private Boolean unplanned;

  private Integer testNum;

  private Integer testFailNum;

  private CaseTestResult testResult;

  private String testRemark;

  @Schema(description = "Test case score")
  private Integer testScore;

  private LocalDateTime testResultHandleDate;

  private List<Attachment> attachments;

  private List<IdAndNameVo> tags;

  private List<TaskInfoVo> refTaskInfos;

  private List<FuncCaseInfoVo> refCaseInfos;

  private Map<Integer, FuncCaseDetailVo> allVersionCaseVos;

  private Progress progress;

  private Boolean favourite;

  private Boolean follow;

  private int commentNum;

  private int activityNum;

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
