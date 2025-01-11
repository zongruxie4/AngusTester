package cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.baseline;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.api.vo.IdAndNameVo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestStep;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseInfoVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskInfoVo;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 */
@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncBaselineCaseListVo extends FuncCaseListVo {

  private Long baselineId;

  /**
   * Note: case id
   */
  private Long id;

  private String name;

  private String code;

  private Integer version;

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

  @JsonIgnore // Only used by export
  private String precondition;

  @JsonIgnore // Only used by export
  private LinkedHashSet<CaseTestStep> steps;

  @JsonIgnore // Only used by export
  private List<TaskInfoVo> refTaskInfos;

  @JsonIgnore // Only used by export
  private List<FuncCaseInfoVo> refCaseInfos;

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

  //@NameJoinField(id = "testerId", repository = "commonUserBaseRepo")
  private String testerName;

  private String testerAvatar;

  private Long developerId;

  @NameJoinField(id = "developerId", repository = "commonUserBaseRepo")
  private String developerName;

  private Integer testNum;

  private Integer testFailNum;

  private CaseTestResult testResult;

  private String testRemark;

  private LocalDateTime testResultHandleDate;

  private List<IdAndNameVo> tags;

  private Boolean favouriteFlag;

  private Boolean followFlag;

  // private int commentNum;

  private Long tenantId;

  //@NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  //private String tenantName;

  private Long createdBy;

  private String createdByName;

  private String avatar;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime lastModifiedDate;

}
