package cloud.xcan.angus.core.tester.interfaces.report.facade.dto.record;

import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@Schema(description = "Data transfer object for finding and filtering report generation records")
public class ReportRecordFindDto extends PageQuery {

  @Schema(description = "Report record ID to filter by specific record")
  private Long id;

  @NotNull
  @Schema(description = "Project ID to filter records for a specific project", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Report ID to filter records for a specific report")
  private Long reportId;

  @Schema(description = "Report category to filter records by report type")
  private ReportCategory category;

  @Schema(description = "Report template to filter records by template type")
  private ReportTemplate template;

}
