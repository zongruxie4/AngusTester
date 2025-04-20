package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.enums.ReviewStatus;
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
public class FuncReviewCaseSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @Schema(description = "Review id", requiredMode = RequiredMode.REQUIRED)
  private Long reviewId;

  private Long planId;

  private Long caseId;

  private String caseName;

  private String caseCode;

  private Long reviewerId;

  private LocalDateTime reviewDate;

  private ReviewStatus reviewStatus;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}



