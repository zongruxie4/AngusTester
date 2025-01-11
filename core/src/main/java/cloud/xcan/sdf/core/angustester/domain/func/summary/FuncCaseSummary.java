package cloud.xcan.sdf.core.angustester.domain.func.summary;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseSummary extends FuncCaseEfficiencySummary {

  private Long id;

  private Long projectId;

  private Long planId;

  @NameJoinField(id = "planId", repository = "funcPlanRepo")
  private String planName;

  private Long moduleId;

  @NameJoinField(id = "moduleId", repository = "moduleRepo")
  private String moduleName;

  private String name;

  private String code;

  public Priority priority;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  public LocalDateTime deadlineDate;

  private Boolean overdueFlag;

  private EvalWorkloadMethod evalWorkloadMethod;

  private BigDecimal evalWorkload;

  private BigDecimal actualWorkload;

  private String description;

  private Boolean reviewFlag;

  private Long reviewerId;

  @NameJoinField(id = "reviewerId", repository = "commonUserBaseRepo")
  private String reviewerName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
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

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime testResultHandleDate;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime lastModifiedDate;

}
