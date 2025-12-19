package cloud.xcan.angus.core.tester.interfaces.report.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
import cloud.xcan.angus.core.tester.domain.report.ReportStatus;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
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
@Schema(description = "Data transfer object for full-text search and advanced filtering of reports")
public class ReportSearchDto extends PageQuery {

  @Schema(description = "Report ID to filter by specific report")
  private Long id;

  @NotNull
  @Schema(description = "Project ID to filter reports for a specific project", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Report name to search by partial or exact name match")
  private String name;

  @Schema(description = "Report status to filter by current state (DRAFT, ACTIVE, INACTIVE, etc.)")
  private ReportStatus status;

  @Schema(description = "Report category to filter by report type and structure")
  private ReportCategory category;

  @Schema(description = "Report template to filter by layout and formatting type")
  private ReportTemplate template;

  @Schema(description = "Target object ID to filter reports associated with specific entities")
  private Long targetId;

  @Schema(description = "Target object type to filter reports by associated entity type")
  private CombinedTargetType targetType;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Next generation date to filter reports scheduled for generation on a specific date")
  private LocalDateTime nextGenerationDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
