package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_REMARK_LENGTH_X10;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.pojo.Attachment;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintUpdateDto {

  @NotNull
  @Schema(description = "Sprint id", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  //@Schema(description = "Services id", example = "1")
  //private Long projectId;

  @Schema(description = "Sprint name", example = "Example sprint")
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  //@Schema(description = "Sprint status", example = "IN_PROGRESS")
  //private FuncSprintStatus status;

  @Schema(description = "Sprint start date", example = "2023-06-10 00:00:00")
  private LocalDateTime startDate;

  @Schema(description = "Sprint deadline date", example = "2023-06-20 00:00:00")
  public LocalDateTime deadlineDate;

  @Schema(description = "Owner id", example = "1")
  private Long ownerId;

  //Not allowed modification
  //@Schema(description = "Task prefix", example = "EXAMPLE_")
  //@Length(max = MAX_KEY_LENGTH)
  //private String taskPrefix;

  @Schema(description = "Workload evaluation method", example = "STORY_POINT")
  private EvalWorkloadMethod evalWorkloadMethod;

  @Schema(description = "Sprint attachments")
  @Size(max = MAX_ATTACHMENT_NUM_X2)
  private List<Attachment> attachments;

  @Length(max = MAX_REMARK_LENGTH_X10)
  @Schema(description = "Acceptance criteria for sprint. Clearly define the specific conditions and standards for software product delivery. ")
  private String acceptanceCriteria;

  @Length(max = MAX_REMARK_LENGTH_X10)
  @Schema(description =
      "Other sprint information. This is the other description of the sprint. Additional details such as sprint strategies, "
          + "risk assessment, and management. ")
  private String otherInformation;

}
