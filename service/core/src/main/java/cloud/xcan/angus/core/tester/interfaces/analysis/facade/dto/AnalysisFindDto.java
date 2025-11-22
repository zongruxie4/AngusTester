package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisDataSource;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisResource;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
@Accessors(chain = true)
@Schema(description = "Data transfer object for finding and filtering analysis tasks with comprehensive search criteria")
public class AnalysisFindDto extends PageQuery {

  @Schema(description = "Analysis task ID to filter by specific analysis")
  private Long id;

  @NotNull
  @Schema(description = "Project ID to filter analysis tasks for a specific project", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Analysis resource type to filter by resource category")
  private AnalysisResource resource;

  @Schema(description = "Analysis template identifier to filter by template type")
  private String template;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Analysis name to filter by partial or exact name match")
  private String name;

  @Schema(description = "Type of object being analyzed to filter by analysis target")
  private String object;

  @Schema(description = "Sprint or plan ID to filter analysis tasks for specific plans")
  private Long planId;

  @Schema(description = "Organization type to filter analysis tasks by organization category")
  private AuthObjectType orgType;

  @Schema(description = "Organization ID to filter analysis tasks for specific organizations")
  private Long orgId;

  @Schema(description = "Data source configuration to filter by data source type")
  private AnalysisDataSource datasource;

  @Schema(description = "User ID who created the analysis task")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Creation date to filter analysis tasks created on a specific date")
  private LocalDateTime createdDate;

  @Schema(description = "User ID who last modified the analysis task")
  private Long lastModifiedBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
