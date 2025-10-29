package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan;

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
  @Schema(description = "Project identifier for test plan scope definition", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotBlank
  @Schema(description = "Functional test plan name for identification and organization", example = "Example plan", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  @Schema(description = "Authorization control enablement flag with default disabled state")
  public Boolean auth;

  @NotNull
  @Schema(description = "Test plan start date for timeline management", example = "2023-06-10 00:00:00", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime startDate;

  @NotNull
  @Schema(description = "Test plan completion deadline for timeline management", example = "2029-06-20 00:00:00", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime deadlineDate;

  @NotNull
  @Schema(description = "Test plan owner identifier for responsibility and authority assignment", requiredMode = RequiredMode.REQUIRED)
  private Long ownerId;

  @NotEmpty
  @Schema(description = "Tester responsibility mapping for role definition and task assignment", requiredMode = RequiredMode.REQUIRED)
  private LinkedHashMap<Long, @Length(max = MAX_REMARK_LENGTH_X4) String> testerResponsibilities;

  @EditorContentLength
  @Schema(description = "Comprehensive testing scope definition for activity coverage")
  private String testingScope;

  @EditorContentLength
  @Schema(description = "Testing objectives specification for outcome focus and validation")
  private String testingObjectives;

  @EditorContentLength
  @Schema(description = "Acceptance criteria definition for delivery standards and conditions")
  private String acceptanceCriteria;

  @EditorContentLength
  @Schema(description = "Additional test plan information for strategy and risk management")
  private String otherInformation;

  @Schema(description = "Test plan supporting documents and reference materials")
  @Size(max = MAX_ATTACHMENT_NUM_X2)
  private List<Attachment> attachments;

  ///////////// Other setting /////////////

  @Schema(description = "Test case prefix for naming convention with post-creation immutability", example = "EXAMPLE_")
  @Length(max = MAX_KEY_LENGTH)
  private String casePrefix;

  @NotNull
  @Schema(description = "Test case review workflow enablement flag", example = "true", requiredMode = RequiredMode.REQUIRED)
  private Boolean review;

  @NotNull
  @Schema(description = "Workload evaluation methodology for effort estimation", example = "STORY_POINT", requiredMode = RequiredMode.REQUIRED)
  private EvalWorkloadMethod evalWorkloadMethod;
}
