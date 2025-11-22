package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X2;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisDataSource;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisTimeRange;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
@Schema(description = "Data transfer object for updating existing analysis task configuration with partial modifications")
public class AnalysisUpdateDto {

  @NotNull
  @Schema(description = "Analysis task ID of the existing analysis to be updated", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Name of the analysis task for identification and reference")
  private String name;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Detailed description of the analysis purpose and scope")
  private String description;

  @Schema(description = "Type of object to be analyzed (e.g., TESTER_ORG, ASSIGNEE_ORG, PLAN, etc.)")
  private String object;

  @Schema(description = "Sprint or plan ID required when analyzing plan-based objects")
  private Long planId;

  @Schema(description = "Organization type required when analyzing organization-based objects")
  private AuthObjectType orgType;

  @Schema(description = "Organization ID required when analyzing organization-based objects")
  private Long orgId;

  @Schema(description = "Flag to include tester analysis in the results, defaults to true")
  private Boolean containsUserAnalysis;

  @Schema(description = "Flag to include detailed data analysis in the results, defaults to true")
  private Boolean containsDataDetail;

  @Schema(description = "Time range configuration for data analysis (e.g., LAST_WEEK, LAST_MONTH, CUSTOM_TIME)")
  private AnalysisTimeRange timeRange;

  @Schema(description = "Custom start time for analysis data, required when time range is CUSTOM_TIME")
  private LocalDateTime startTime;

  @Schema(description = "Custom end time for analysis data, required when time range is CUSTOM_TIME")
  private LocalDateTime endTime;

  @Schema(description = "Data source configuration for the analysis (e.g., database, API, file system)")
  private AnalysisDataSource datasource;

  @Length(max = MAX_URL_LENGTH_X2)
  @Schema(description = "Custom filter criteria in URL format for advanced data filtering")
  private String filterCriteria;

}
