package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationScope;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
public class EvaluationFindDto extends PageQuery {

  @Schema(description = "Evaluation identifier for precise query filtering")
  private Long id;

  @Schema(description = "Evaluation name for fuzzy search or filtering")
  private String name;

  @Schema(description = "Project identifier for project-based filtering")
  private Long projectId;

  @Schema(description = "Evaluation scope for filtering (PROJECT, PLAN, MODULE)")
  private EvaluationScope scope;

  @Schema(description = "Resource identifier for filtering based on scope type")
  private Long resourceId;

  @Schema(description = "Creator identifier for creation-based filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Evaluation creation date for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Last modifier identifier for modification-based filtering")
  private Long modifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Evaluation last modification date for temporal filtering")
  private LocalDateTime modifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}

