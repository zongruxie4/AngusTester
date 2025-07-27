package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_REMARK_LENGTH_X10;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.pojo.Attachment;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class TaskSprintReplaceDto {

  @Schema(description = "Sprint identifier for modification. Creates new sprint when null")
  private Long id;

  @Schema(description = "Project identifier for sprint organization and access control")
  private Long projectId;

  //@Schema(description = "Sprint status", example = "IN_PROGRESS")
  //private FuncSprintStatus status;

  @Schema(description = "Authorization control flag for sprint access management")
  public Boolean auth;

  @NotBlank
  @Schema(description = "Sprint display name for identification and iteration management", example = "Example sprint", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  @NotNull
  @Schema(description = "Sprint execution start date for timeline planning", example = "2023-06-10 00:00:00", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime startDate;

  @NotNull
  @Schema(description = "Sprint completion deadline for timeline management", example = "2023-06-20 00:00:00", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime deadlineDate;

  @NotNull
  @Schema(description = "Sprint owner identifier for responsibility assignment", requiredMode = RequiredMode.REQUIRED)
  private Long ownerId;

  @Schema(description = "Task prefix for sprint task identification and organization", example = "EXAMPLE_")
  @Length(max = MAX_KEY_LENGTH)
  private String taskPrefix;

  @NotNull
  @Schema(description = "Workload evaluation method for effort estimation and planning", example = "STORY_POINT", requiredMode = RequiredMode.REQUIRED)
  private EvalWorkloadMethod evalWorkloadMethod;

  @Schema(description = "Sprint-related file attachments for documentation and reference")
  @Size(max = MAX_ATTACHMENT_NUM_X2)
  private List<Attachment> attachments;

  @Length(max = MAX_REMARK_LENGTH_X10)
  @Schema(description = "Sprint acceptance criteria for delivery standards and quality definition")
  private String acceptanceCriteria;

  @Length(max = MAX_REMARK_LENGTH_X10)
  @Schema(description = "Additional sprint information for strategy, risk assessment, and management details")
  private String otherInformation;

}
