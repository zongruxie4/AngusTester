package cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_ID_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.core.angustester.domain.report.ReportCategory;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.angustester.domain.report.setting.BasicInfoSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.ContentSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.TimeSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ReportAddDto {

  @NotNull
  @ApiModelProperty(value = "Project id", example = "1", required = true)
  private Long projectId;

  @NotBlank
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(value = "Report name, Brief overview of the report, supporting up to 200 characters.", example = "Example report", required = true)
  private String name;

  @ApiModelProperty(value = "Whether to enable authorization control, default disabled")
  public Boolean authFlag;

  @NotEmpty
  @Length(max = DEFAULT_ID_LENGTH)
  @ApiModelProperty(value = "Report version", example = "1.0", required = true)
  private String version;

  @Length(max = DEFAULT_DESC_LENGTH)
  @ApiModelProperty(value ="Report description")
  private String description;

  @NotNull
  @ApiModelProperty(value = "Report category", required = true)
  private ReportCategory category;

  @NotNull
  @ApiModelProperty(value = "Report template", required = true)
  private ReportTemplate template;

  @Valid
  @NotNull
  @ApiModelProperty(value = "Report create time setting", required = true)
  private TimeSetting createTimeSetting;

  @Valid
  @NotNull
  @ApiModelProperty(value = "Report basic info setting", required = true)
  private BasicInfoSetting basicInfoSetting;

  @Valid
  @NotNull
  @ApiModelProperty(value = "Report content setting", required = true)
  private ContentSetting contentSetting;

}
