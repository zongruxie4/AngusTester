package cloud.xcan.angus.core.tester.domain.report.setting;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class BasicInfoSetting {

  //  @Schema(hidden = true)
  //  private final boolean showReportName = true;
  //
  //  @Schema(hidden = true)
  //  private final boolean showReportedBy = true;
  //
  //  @Schema(hidden = true)
  //  private final boolean showReportDate = true;
  //
  //  @Schema(hidden = true)
  //  private final boolean showReportVersion = true;
  //
  //  @Schema(hidden = true)
  //  private final boolean showReportDescription = true;

  @Length(max = MAX_DESC_LENGTH)
  private String reportContacts;

  @Length(max = MAX_DESC_LENGTH)
  private String reportCopyright;

  @Length(max = MAX_DESC_LENGTH)
  private String otherInformation;

  @Length(max = MAX_NAME_LENGTH)
  private String watermark;

}
