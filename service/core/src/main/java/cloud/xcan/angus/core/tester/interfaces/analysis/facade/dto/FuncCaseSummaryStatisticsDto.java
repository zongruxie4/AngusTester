package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_CODE_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import cloud.xcan.angus.remote.PageQuery;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseSummaryStatisticsDto extends PageQuery {

  @Schema(description = "Functional case identifier")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for case statistics", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Test plan identifier for case filtering")
  private Long planId;

  @Schema(description = "Module identifier for case categorization")
  private Long moduleId;

  @Schema(description = "Tag identifier for case filtering")
  private Long tagId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Case name for search and filtering")
  private String name;

  @Length(max = MAX_CODE_LENGTH)
  @Schema(description = "Case code for identification and search")
  private String code;

  @Schema(description = "Tester user identifier assigned to the case")
  private Long testerId;

  @Schema(description = "Developer user identifier assigned to the case")
  private Long developerId;

  @Schema(description = "Flag indicating if case is unplanned")
  private Boolean unplanned;

  @Schema(description = "Case priority level")
  private Priority priority;

  @Schema(description = "Flag indicating if case is overdue")
  private Boolean overdue;

  @Schema(description = "Case deadline timestamp")
  private LocalDateTime deadlineDate;

  @Schema(description = "Reviewer user identifier")
  private Long reviewerId;

  @Schema(description = "Case review completion timestamp")
  private LocalDateTime reviewDate;

  @Schema(description = "Case review status")
  private ReviewStatus reviewStatus;

  @Schema(description = "Number of review attempts")
  private Integer reviewNum;

  @Schema(description = "Number of test executions")
  private Integer testNum;

  @Schema(description = "Number of failed test executions")
  private Integer testFailNum;

  @Schema(description = "Current test result status")
  private CaseTestResult testResult;

  @Schema(description = "Test result processing timestamp")
  private LocalDateTime testResultHandleDate;

  @Schema(description = "API protocol type for the case")
  private ApisProtocol protocol;

  @Schema(description = "HTTP method for API case")
  private HttpMethod method;

  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "API endpoint URL for the case")
  private String endpoint;

  @Schema(description = "Case creator user identifier")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Case creation timestamp")
  private LocalDateTime createdDate;

  @Schema(description = "Last modifier user identifier")
  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Last modification timestamp")
  private LocalDateTime lastModifiedDate;

}
