package cloud.xcan.angus.core.tester.interfaces.func.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
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

@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseFindDto extends PageQuery {

  @Schema(description = "Functional test case identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for test case scope filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Functional test plan identifier for case filtering")
  private Long planId;

  @Schema(description = "Functional test case module identifier for categorization filtering")
  private Long moduleId;

  @Schema(description = "API identifier for test case association filtering")
  private Long apisId;

  @Schema(description = "Tag identifier for test case categorization filtering")
  private Long tagId;

  @Schema(description = "Test case name for fuzzy search and filtering")
  private String name;

  @Schema(description = "Test case code for precise identification")
  private String code;

  @Schema(description = "Software version for test case context filtering")
  private String softwareVersion;

  @Schema(description = "Tester identifier for case assignment filtering")
  private Long testerId;

  @Schema(description = "Developer identifier for case responsibility filtering")
  private Long developerId;

  @Schema(description = "Unplanned case flag for test case status filtering")
  private Boolean unplanned;

  @Schema(description = "Test case priority level for execution scheduling filtering")
  private Priority priority;

  @Schema(description = "Overdue case flag for deadline management filtering")
  private Boolean overdue;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Test case completion deadline for timeline filtering")
  private LocalDateTime deadlineDate;

  @Schema(description = "Reviewer identifier for case review filtering")
  private Long reviewerId;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Case review completion timestamp for review status filtering")
  private LocalDateTime reviewDate;

  @Schema(description = "Case review status for review workflow filtering")
  private ReviewStatus reviewStatus;

  @Schema(description = "Review iteration count for review progress tracking")
  private Integer reviewNum;

  @Schema(description = "Test execution count for test progress tracking")
  private Integer testNum;

  @Schema(description = "Failed test count for test result analysis")
  private Integer testFailNum;

  @Schema(description = "Test case execution result for result-based filtering")
  private CaseTestResult testResult;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Test result processing timestamp for result management")
  private LocalDateTime testResultHandleDate;

  @Schema(description = "API protocol for test case protocol filtering")
  private ApisProtocol protocol;

  @Schema(description = "HTTP method for test case method filtering")
  private HttpMethod method;

  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "API endpoint for test case endpoint filtering")
  private String endpoint;

  @Schema(description = "Test case creator identifier for ownership filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Test case creation timestamp for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Test case last modifier identifier for modification tracking")
  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Test case last modification timestamp for change tracking")
  private LocalDateTime lastModifiedDate;

  @Schema(description = "Favorite user identifier for favorite case filtering")
  private Long favouriteBy;

  @Schema(description = "Following user identifier for followed case filtering")
  private Long followBy;

  @Schema(description = "Commenting user identifier for commented case filtering")
  private Long commentBy;

}



