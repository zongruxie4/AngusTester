package cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_ID_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.core.angustester.domain.report.setting.BasicInfoSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.ContentSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.TimeSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ReportUpdateDto {

  @NotNull
  @ApiModelProperty(value = "Report id", required = true)
  private Long id;

  //@NotNull
  //@ApiModelProperty(value = "Project id", example = "1"/*, required = true*/)
  //private Long projectId;

  @ApiModelProperty(value = "Report name, Brief overview of the report, supporting up to 200 characters.", example = "Example report")
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String name;

  @Length(max = DEFAULT_ID_LENGTH)
  @ApiModelProperty(value = "Report version", example = "1.0")
  private String version;

  @Length(max = DEFAULT_DESC_LENGTH)
  @ApiModelProperty(value = "Report description")
  private String description;

  //@ApiModelProperty(value = "Report category")
  //private ReportCategory category;

  //@ApiModelProperty(value = "Report template")
  //private ReportTemplate template;

  @Valid
  @ApiModelProperty(value = "Report create time setting")
  private TimeSetting createTimeSetting;

  @Valid
  @ApiModelProperty(value = "Report basic info setting")
  private BasicInfoSetting basicInfoSetting;

  @Valid
  @ApiModelProperty(value = "Report content setting")
  private ContentSetting contentSetting;

}
