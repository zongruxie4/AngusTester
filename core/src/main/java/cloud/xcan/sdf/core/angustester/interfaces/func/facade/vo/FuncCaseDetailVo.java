package cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo;


import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.api.vo.IdAndNameVo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestStep;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskInfoVo;
import io.swagger.annotations.ApiModel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
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

  private Boolean planAuthFlag;

  private Long moduleId;

  @NameJoinField(id = "moduleId", repository = "moduleRepo")
  private String moduleName;

  private Priority priority;

  public LocalDateTime deadlineDate;

  private Boolean overdueFlag;

  private EvalWorkloadMethod evalWorkloadMethod;

  private BigDecimal evalWorkload;

  private BigDecimal actualWorkload;

  private String precondition;

  private List<CaseTestStep> steps;

  private String description;

  private Boolean reviewFlag;

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

  private Boolean unplannedFlag;

  private Integer testNum;

  private Integer testFailNum;

  private CaseTestResult testResult;

  private String testRemark;

  private LocalDateTime testResultHandleDate;

  private List<Attachment> attachments;

  private List<IdAndNameVo> tags;

  private List<TaskInfoVo> refTaskInfos;

  private List<FuncCaseInfoVo> refCaseInfos;

  private Map<Integer, FuncCaseDetailVo> allVersionCaseVos;

  private Progress progress;

  private Boolean favouriteFlag;

  private Boolean followFlag;

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
