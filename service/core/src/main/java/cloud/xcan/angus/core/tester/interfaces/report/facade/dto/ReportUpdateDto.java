package cloud.xcan.angus.core.tester.interfaces.report.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ID_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.domain.report.setting.BasicInfoSetting;
import cloud.xcan.angus.core.tester.domain.setting.ContentSetting;
import cloud.xcan.angus.core.tester.domain.setting.TimeSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
@Schema(description = "Data transfer object for updating existing report configuration with partial modifications")
public class ReportUpdateDto {

  @NotNull
  @Schema(description = "Report ID of the existing report to be updated", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Schema(description = "Report name providing a brief overview of the report content, supporting up to 200 characters", example = "Q4 2024 Project Progress Report")
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  @Length(max = MAX_ID_LENGTH)
  @Schema(description = "Report version identifier for tracking changes and updates", example = "1.0.0")
  private String version;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Detailed description of the report purpose and content")
  private String description;

  @Valid
  @Schema(description = "Configuration for when and how often the report should be generated")
  private TimeSetting createTimeSetting;

  @Valid
  @Schema(description = "Basic information settings including title, subtitle, and metadata")
  private BasicInfoSetting basicInfoSetting;

  @Valid
  @Schema(description = "Content configuration defining what data and sections to include in the report")
  private ContentSetting contentSetting;

}
