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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  private Long planId;

  private Long moduleId;

  private Long apisId;

  private Long tagId;

  private String name;

  private String code;

  private String softwareVersion;

  private Long testerId;

  private Long developerId;

  private Boolean unplanned;

  @Schema(allowableValues = {"HIGHEST", "HIGH", "MEDIUM", "LOW", "LOWEST"})
  private String priority;

  private Boolean overdue;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  private Long reviewerId;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime reviewDate;

  private ReviewStatus reviewStatus;

  private Integer reviewNum;

  private Integer testNum;

  private Integer testFailNum;

  private CaseTestResult testResult;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime testResultHandleDate;

  private ApisProtocol protocol;

  private HttpMethod method;

  @Length(max = MAX_URL_LENGTH_X4)
  private String endpoint;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  private Long favouriteBy;

  private Long followBy;

  private Long commentBy;
}
