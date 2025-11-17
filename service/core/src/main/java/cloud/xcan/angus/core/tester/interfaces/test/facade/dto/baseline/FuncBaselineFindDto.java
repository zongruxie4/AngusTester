package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.baseline;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

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
public class FuncBaselineFindDto extends PageQuery {

  @Schema(description = "Functional test baseline identifier for precise query")
  private Long id;

  @Schema(description = "Functional test baseline name for fuzzy search and filtering")
  private String name;

  @NotNull
  @Schema(description = "Project identifier for baseline scope filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Functional test plan identifier for baseline filtering")
  private Long planId;

  @Schema(description = "Baseline establishment status for state-based filtering")
  private Boolean established;

  @Schema(description = "Baseline creator identifier for ownership filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Baseline creation timestamp for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Baseline last modifier identifier for modification tracking")
  private Long lastModifiedBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
