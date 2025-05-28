package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_REMARK_LENGTH_X4;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class FuncPlanAddDto {

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotBlank
  @Schema(description = "Plan name, Brief Overview of the Plan, supporting up to 200 characters.", example = "Example plan", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  @Schema(description = "Whether to enable authorization control, default disabled")
  public Boolean auth;

  @NotNull
  @Schema(description = "Plan start date, Determine the start times of the testing activities to ensure completion within the project cycle.", example = "2023-06-10 00:00:00", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime startDate;

  @NotNull
  @Schema(description = "Plan deadline date, Determine the end times of the testing activities to ensure completion within the project cycle.", example = "2029-06-20 00:00:00", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime deadlineDate;

  @NotNull
  @Schema(description =
      "Owner id. By assigning a responsible person to the plan, clarify the accountability and authority for testing, "
          + "facilitating task completion and progress control. Responsibilities include problem resolution, progress promotion, team collaboration, risk identification, and management.", requiredMode = RequiredMode.REQUIRED)
  private Long ownerId;

  @NotEmpty
  @Schema(description =
      "Specify the testers involved in this test plan; only authorized testers are allowed to participate. "
          + "Define the roles of testers, outlining their responsibilities within the testing scope to avoid ambiguity and task omission.", requiredMode = RequiredMode.REQUIRED)
  private LinkedHashMap<Long, @Length(max = MAX_REMARK_LENGTH_X4) String> testerResponsibilities;

  @NotEmpty
  @EditorContentLength
  @Schema(description =
      "Testing scope for testing plan. Define the specific content and extent covered by the testing activities, "
          + "including which functional modules, platforms, versions, etc. ", requiredMode = RequiredMode.REQUIRED)
  private String testingScope;

  @NotEmpty
  @EditorContentLength
  @Schema(description =
      "Testing objectives for testing plan. Specify the specific purposes and expected outcomes of the testing activities, "
          + "helping the testing team focus on core testing objectives.", requiredMode = RequiredMode.REQUIRED)
  private String testingObjectives;

  @EditorContentLength
  @Schema(description = "Acceptance criteria for testing plan. Clearly define the specific conditions and standards for software product delivery. ")
  private String acceptanceCriteria;

  @EditorContentLength
  @Schema(description =
      "Other plan information. This is the other description of the testing plan. Additional details such as testing strategies, "
          + "risk assessment, and management. ")
  private String otherInformation;

  @Schema(description =
      "Plan attachments. Additional documents and information, such as requirement specifications, reference materials and standards, "
          + "system architecture diagrams, testing specifications, technical documents, etc.")
  @Size(max = MAX_ATTACHMENT_NUM_X2)
  private List<Attachment> attachments;

  ///////////// Other setting /////////////

  @Schema(description = "Use case prefix", example = "EXAMPLE_")
  @Length(max = MAX_KEY_LENGTH)
  private String casePrefix;

  @NotNull
  @Schema(description = "Enable use case review flag", example = "true", requiredMode = RequiredMode.REQUIRED)
  private Boolean review;

  @NotNull
  @Schema(description = "Workload evaluation method", example = "STORY_POINT", requiredMode = RequiredMode.REQUIRED)
  private EvalWorkloadMethod evalWorkloadMethod;
}
