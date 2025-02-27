package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_TAGS_NUM;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginConstant.MAX_CASE_OR_TASK_REFS_NUM;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginConstant.MAX_CASE_STEPS_NUM;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_KEY_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X10;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseStepView;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestStep;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseReplaceDto {

  @ApiModelProperty(value = "Functional testing case id")
  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH_X4)
  @ApiModelProperty(value = "Case name")
  private String name;

  //@NotBlank
  //@Length(max = DEFAULT_CODE_LENGTH)
  //@ApiModelProperty(value = "Case code", required = true)
  //private String code;

  @ApiModelProperty(value = "Functional testing plan id, required when adding")
  private Long planId;

  @ApiModelProperty(value = "Function case module id")
  private Long moduleId;

  @Length(max = DEFAULT_KEY_LENGTH)
  @ApiModelProperty(value = "Version of software for the task")
  private String softwareVersion;

  @ApiModelProperty(value = "Case priority")
  private Priority priority;

  @NotNull
  @ApiModelProperty(value = "Case deadline date", required = true)
  public LocalDateTime deadlineDate;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @ApiModelProperty(value = "Case Workload")
  private BigDecimal evalWorkload;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @ApiModelProperty(value = "Actual usage workload")
  private BigDecimal actualWorkload;

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value = "Case preconditions")
  private String precondition;

  @ApiModelProperty(value = "Case steps view type, default is 'TABLE'")
  private CaseStepView stepView;

  @Size(max = MAX_CASE_STEPS_NUM)
  @ApiModelProperty(value = "Case steps")
  private List<CaseTestStep> steps;

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value = "Case description")
  private String description;

  @NotNull
  @ApiModelProperty(value = "Developer id", required = true)
  private Long developerId;

  @NotNull
  @ApiModelProperty(value = "Tester id", required = true)
  private Long testerId;

  //private CaseTestResult testResult;

  @Size(max = MAX_TAGS_NUM)
  @ApiModelProperty(value = "Report ids")
  private LinkedHashSet<Long> tagIds;

  @Size(max = MAX_ATTACHMENT_NUM)
  @ApiModelProperty(value = "Case attachments")
  private List<Attachment> attachments;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @ApiModelProperty(value = "Reference task ids")
  private LinkedHashSet<Long> refTaskIds;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @ApiModelProperty(value = "Reference case ids")
  private LinkedHashSet<Long> refCaseIds;

}
