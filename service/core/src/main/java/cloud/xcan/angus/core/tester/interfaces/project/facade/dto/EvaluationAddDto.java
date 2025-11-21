package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationScope;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class EvaluationAddDto {

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Evaluation name for identification and management", example = "项目质量测评 - 2024年Q1", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(description = "Project identifier for evaluation association", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotNull
  @Schema(description = "Evaluation scope (PROJECT, PLAN, MODULE)", example = "PROJECT", requiredMode = RequiredMode.REQUIRED)
  private EvaluationScope scope;

  @NotEmpty
  @Schema(description = "List of evaluation purposes to be assessed", requiredMode = RequiredMode.REQUIRED)
  private List<EvaluationPurpose> purposes;

  @Schema(description = "Resource identifier based on scope type (plan ID, module ID, etc.)")
  private Long resourceId;

  @Schema(description = "Evaluation start date for timeline planning", example = "2024-01-01 00:00:00")
  private LocalDateTime startDate;

  @Schema(description = "Evaluation deadline date for completion tracking", example = "2024-03-31 23:59:59")
  private LocalDateTime deadlineDate;
}

