package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review;

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
public class FuncReviewFindDto extends PageQuery {

  @Schema(description = "Functional test review identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for review scope filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Functional test plan identifier for review filtering")
  private Long planId;

  @Schema(description = "Review name for fuzzy search and filtering")
  private String name;

  @Schema(description = "Review status for state-based filtering")
  private FuncPlanStatus status;

  @Schema(description = "Review owner identifier for responsibility filtering")
  private Long ownerId;

  @Schema(description = "Review creator identifier for ownership filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Review creation timestamp for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Review last modifier identifier for modification tracking")
  private Long lastModifiedBy;

}



