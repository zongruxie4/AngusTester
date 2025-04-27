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

  private Long id;

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  private Long planId;

  private Long moduleId;

  private Long tagId;

  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  @Length(max = MAX_CODE_LENGTH)
  private String code;

  private Long testerId;

  private Long developerId;

  private Boolean unplanned;

  private Priority priority;

  private Boolean overdue;

  private LocalDateTime deadlineDate;

  private Long reviewerId;

  private LocalDateTime reviewDate;

  private ReviewStatus reviewStatus;

  private Integer reviewNum;

  private Integer testNum;

  private Integer testFailNum;

  private CaseTestResult testResult;

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

}
