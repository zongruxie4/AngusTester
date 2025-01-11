package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_KEY_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X10;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM_X2;

import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.pojo.Attachment;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
public class TaskSprintAddDto {

  @NotNull
  @ApiModelProperty(value = "Project id", example = "1", required = true)
  private Long projectId;

  @NotBlank
  @ApiModelProperty(value = "Sprint name", example = "Example sprint", required = true)
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String name;

  @ApiModelProperty(value = "Whether to enable authorization control, default disabled")
  public Boolean authFlag;

  @NotNull
  @ApiModelProperty(value = "Sprint start date", example = "2023-06-10 00:00:00", required = true)
  private LocalDateTime startDate;

  @NotNull
  @ApiModelProperty(value = "Sprint deadline date", example = "2023-06-20 00:00:00", required = true)
  private LocalDateTime deadlineDate;

  @NotNull
  @ApiModelProperty(value = "Owner id", example = "1", required = true)
  private Long ownerId;

  @ApiModelProperty(value = "Task prefix", example = "EXAMPLE_")
  @Length(max = DEFAULT_KEY_LENGTH)
  private String taskPrefix;

  @NotNull
  @ApiModelProperty(value = "Workload evaluation method", example = "STORY_POINT", required = true)
  private EvalWorkloadMethod evalWorkloadMethod;

  @ApiModelProperty(value = "Sprint attachments")
  @Size(max = MAX_ATTACHMENT_NUM_X2)
  private List<Attachment> attachments;

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value = "Acceptance criteria for sprint. Clearly define the specific conditions and standards for software product delivery. ")
  private String acceptanceCriteria;

  @Length(max = DEFAULT_REMARK_LENGTH_X10)
  @ApiModelProperty(value =
      "Other sprint information. This is the other description of the sprint. Additional details such as sprint strategies, "
          + "risk assessment, and management. ")
  private String otherInformation;

}
