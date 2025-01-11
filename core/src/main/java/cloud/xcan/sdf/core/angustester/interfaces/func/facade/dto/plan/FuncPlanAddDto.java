package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_KEY_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X10;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM_X2;

import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.pojo.Attachment;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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
public class FuncPlanAddDto {

  @NotNull
  @ApiModelProperty(value = "Project id", example = "1", required = true)
  private Long projectId;

  @NotBlank
  @ApiModelProperty(value = "Plan name, Brief Overview of the Plan, supporting up to 200 characters.", example = "Example plan", required = true)
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String name;

  @ApiModelProperty(value = "Whether to enable authorization control, default disabled")
  public Boolean authFlag;

  @NotNull
  @ApiModelProperty(value = "Plan start date, Determine the start times of the testing activities to ensure completion within the project cycle.", example = "2023-06-10 00:00:00", required = true)
  private LocalDateTime startDate;

  @NotNull
  @ApiModelProperty(value = "Plan deadline date, Determine the end times of the testing activities to ensure completion within the project cycle.", example = "2029-06-20 00:00:00", required = true)
  private LocalDateTime deadlineDate;

  @NotNull
  @ApiModelProperty(value =
      "Owner id. By assigning a responsible person to the plan, clarify the accountability and authority for testing, "
          + "facilitating task completion and progress control. Responsibilities include problem resolution, progress promotion, team collaboration, risk identification, and management.", example = "1", required = true)
  private Long ownerId;

  @NotEmpty
  @ApiModelProperty(value =
      "Specify the testers involved in this test plan; only authorized testers are allowed to participate. "
          + "Define the roles of testers, outlining their responsibilities within the testing scope to avoid ambiguity and task omission.", required = true)
  private LinkedHashMap<Long, @Length(max = DEFAULT_REMARK_LENGTH_X4) String> testerResponsibilities;

  @NotEmpty
  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value =
      "Testing scope for testing plan. Define the specific content and extent covered by the testing activities, "
          + "including which functional modules, platforms, versions, etc. ", required = true)
  private String testingScope;

  @NotEmpty
  @ApiModelProperty(value =
      "Testing objectives for testing plan. Specify the specific purposes and expected outcomes of the testing activities, "
          + "helping the testing team focus on core testing objectives.", required = true)
  private String testingObjectives;

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value = "Acceptance criteria for testing plan. Clearly define the specific conditions and standards for software product delivery. ")
  private String acceptanceCriteria;

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value =
      "Other plan information. This is the other description of the testing plan. Additional details such as testing strategies, "
          + "risk assessment, and management. ")
  private String otherInformation;

  @ApiModelProperty(value =
      "Plan attachments. Additional documents and information, such as requirement specifications, reference materials and standards, "
          + "system architecture diagrams, testing specifications, technical documents, etc.")
  @Size(max = MAX_ATTACHMENT_NUM_X2)
  private List<Attachment> attachments;

  ///////////// Other setting /////////////

  @ApiModelProperty(value = "Use case prefix", example = "EXAMPLE_")
  @Length(max = DEFAULT_KEY_LENGTH)
  private String casePrefix;

  @NotNull
  @ApiModelProperty(value = "Enable use case review flag", example = "true", required = true)
  private Boolean reviewFlag;

  @NotNull
  @ApiModelProperty(value = "Workload evaluation method", example = "STORY_POINT", required = true)
  private EvalWorkloadMethod evalWorkloadMethod;
}
