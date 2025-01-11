package cloud.xcan.sdf.core.angustester.domain.report.setting;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class BasicInfoSetting {

  //  @ApiModelProperty(hidden = true)
  //  private final boolean showReportName = true;
  //
  //  @ApiModelProperty(hidden = true)
  //  private final boolean showReportedBy = true;
  //
  //  @ApiModelProperty(hidden = true)
  //  private final boolean showReportDate = true;
  //
  //  @ApiModelProperty(hidden = true)
  //  private final boolean showReportVersion = true;
  //
  //  @ApiModelProperty(hidden = true)
  //  private final boolean showReportDescription = true;

  @Length(max = DEFAULT_DESC_LENGTH)
  private String reportContacts;

  @Length(max = DEFAULT_DESC_LENGTH)
  private String reportCopyright;

  @Length(max = DEFAULT_DESC_LENGTH)
  private String otherInformation;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String watermark;

}
