package cloud.xcan.angus.core.tester.interfaces.report.facade.dto.record;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
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

  @Schema(description = "User ID who created the report record")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Creation date to filter records created on a specific date")
  private LocalDateTime createdDate;

}
