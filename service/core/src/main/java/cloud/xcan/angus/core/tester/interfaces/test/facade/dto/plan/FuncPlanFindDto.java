package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanStatus;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanFindDto extends PageQuery {

  @Schema(description = "Functional test plan identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for test plan scope filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Test plan name for fuzzy search and filtering")
  private String name;

  //Note: All project members are visible
  //@Schema(description = "Required when app administrators query all task")
  //private Boolean admin;

  @Schema(description = "Test case review workflow enablement flag for workflow filtering")
  private Boolean review;

  @Schema(description = "Test plan status for state-based filtering")
  private FuncPlanStatus status;

  @Schema(description = "Test plan start date for timeline filtering")
  private LocalDateTime startDate;

  @Schema(description = "Test plan completion deadline for timeline filtering")
  private LocalDateTime deadlineDate;

  @Schema(description = "Test plan owner identifier for responsibility filtering")
  private Long ownerId;

  @Schema(description = "Test plan creator identifier for ownership filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Test plan creation timestamp for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Test plan last modifier identifier for modification tracking")
  private Long lastModifiedBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}



