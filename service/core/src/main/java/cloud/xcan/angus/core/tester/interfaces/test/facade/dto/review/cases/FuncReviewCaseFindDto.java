package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.cases;

import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class FuncReviewCaseFindDto extends PageQuery {

  @Schema(description = "Functional test review case identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Functional test review identifier for case filtering", requiredMode = RequiredMode.REQUIRED)
  private Long reviewId;

  @Schema(description = "Functional test plan identifier for case filtering")
  private Long planId;

  @Schema(description = "Test case identifier for precise case filtering")
  private Long caseId;

  @Schema(description = "Test case name for fuzzy search and filtering")
  private String caseName;

  @Schema(description = "Test case code for precise identification")
  private String caseCode;

  @Schema(description = "Reviewer identifier for responsibility filtering")
  private Long reviewerId;

  @Schema(description = "Case review completion timestamp for review status filtering")
  private LocalDateTime reviewDate;

  @Schema(description = "Test case review status for state-based filtering")
  private ReviewStatus reviewStatus;

}



