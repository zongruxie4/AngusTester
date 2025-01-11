package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_CODE_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X4;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseSummaryStatisticsDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(required = true)
  private Long projectId;

  private Long planId;

  private Long moduleId;

  private Long tagId;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String name;

  @Length(max = DEFAULT_CODE_LENGTH)
  private String code;

  private Long testerId;

  private Long developerId;

  private Boolean unplannedFlag;

  private Priority priority;

  private Boolean overdueFlag;

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

  @Length(max = DEFAULT_URL_LENGTH_X4)
  private String endpoint;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

}
