package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X2;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisCaseObject;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisDataSource;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisResource;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisTaskObject;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisTimeRange;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
@Schema(description = "Data transfer object for completely replacing analysis task configuration or creating new analysis tasks")
public class AnalysisReplaceDto {

  @Schema(description = "Analysis task ID to modify existing analysis, or null to create a new analysis task")
  private Long id;

  @Schema(description = "Project ID required when creating a new analysis task, optional for existing analysis updates")
  private Long projectId;

  @NotNull
  @Schema(description = "Type of resource to be analyzed (e.g., TEST_CASE, TASK, etc.)", requiredMode = RequiredMode.REQUIRED)
  private AnalysisResource resource;

  @NotEmpty
  @Schema(description = "Analysis template identifier that defines the analysis structure and methodology", requiredMode = RequiredMode.REQUIRED)
  private String template;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Name of the analysis task for identification and reference", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Detailed description of the analysis purpose and scope")
  private String description;

  @NotEmpty
  @Schema(description = "Type of object to be analyzed (e.g., TESTER_ORG, ASSIGNEE_ORG, PLAN, etc.)", requiredMode = RequiredMode.REQUIRED)
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

  @NotNull
  @Schema(description = "Time range configuration for data analysis (e.g., LAST_WEEK, LAST_MONTH, CUSTOM_TIME)", requiredMode = RequiredMode.REQUIRED)
  private AnalysisTimeRange timeRange;

  @Schema(description = "Custom start time for analysis data, required when time range is CUSTOM_TIME")
  private LocalDateTime startTime;

  @Schema(description = "Custom end time for analysis data, required when time range is CUSTOM_TIME")
  private LocalDateTime endTime;

  @NotNull
  @Schema(description = "Data source configuration for the analysis (e.g., database, API, file system)", requiredMode = RequiredMode.REQUIRED)
  private AnalysisDataSource datasource;

  @Length(max = MAX_URL_LENGTH_X2)
  @Schema(description = "Custom filter criteria in URL format for advanced data filtering")
  private String filterCriteria;

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isAnalysisOrg() {
    return AnalysisCaseObject.TESTER_ORG.getValue().equalsIgnoreCase(object)
        || AnalysisTaskObject.ASSIGNEE_ORG.getValue().equalsIgnoreCase(object);
  }

  @JsonIgnore
  @Schema(hidden = true)
  public boolean isAnalysisOrgTargetMissing() {
    return isAnalysisOrg() && (isNull(orgType) || isNull(orgId));
  }
}
