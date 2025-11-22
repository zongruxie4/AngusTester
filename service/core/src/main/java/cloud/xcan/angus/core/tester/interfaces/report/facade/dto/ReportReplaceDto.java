package cloud.xcan.angus.core.tester.interfaces.report.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ID_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.core.tester.domain.report.setting.BasicInfoSetting;
import cloud.xcan.angus.core.tester.domain.setting.ContentSetting;
import cloud.xcan.angus.core.tester.domain.setting.TimeSetting;
import cloud.xcan.angus.validator.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
@Schema(description = "Data transfer object for completely replacing report configuration or creating new reports")
public class ReportReplaceDto {

  @Schema(description = "Report ID to modify existing report, or null to create a new report")
  private Long id;

  @Schema(description = "Project ID required when creating a new report, optional for existing report updates")
  private Long projectId;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Report name providing a brief overview of the report content, supporting up to 200 characters", example = "Q4 2024 Project Progress Report", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Schema(description = "Flag to enable or disable authorization control for the report, defaults to disabled")
  public Boolean auth;

  @Version
  @NotEmpty
  @Length(max = MAX_ID_LENGTH)
  @Schema(description = "Report version identifier for tracking changes and updates", example = "1.0.0", requiredMode = RequiredMode.REQUIRED)
  private String version;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Detailed description of the report purpose and content")
  private String description;

  @NotNull
  @Schema(description = "Report category that determines the type and structure of the report", requiredMode = RequiredMode.REQUIRED)
  private ReportCategory category;

  @NotNull
  @Schema(description = "Report template that defines the layout and formatting of the report", requiredMode = RequiredMode.REQUIRED)
  private ReportTemplate template;

  @Valid
  @NotNull
  @Schema(description = "Configuration for when and how often the report should be generated", requiredMode = RequiredMode.REQUIRED)
  private TimeSetting createTimeSetting;

  @Valid
  @NotNull
  @Schema(description = "Basic information settings including title, subtitle, and metadata", requiredMode = RequiredMode.REQUIRED)
  private BasicInfoSetting basicInfoSetting;

  @Valid
  @NotNull
  @Schema(description = "Content configuration defining what data and sections to include in the report", requiredMode = RequiredMode.REQUIRED)
  private ContentSetting contentSetting;

}
