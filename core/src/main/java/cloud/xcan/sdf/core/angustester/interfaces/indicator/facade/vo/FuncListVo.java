package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.gm.indicator.SecurityCheckSetting;
import cloud.xcan.sdf.api.gm.indicator.SmokeCheckSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncListVo {

  private Long id;

  private Long targetId;

  private CombinedTargetType targetType;

  private String targetName;

  @ApiModelProperty(value = "Enabled or disabled smoke testing, default Enabled")
  private boolean smoke;

  @ApiModelProperty(value = "Enabled or disabled smoke testing")
  private SmokeCheckSetting smokeCheckSetting;

  @ApiModelProperty(value = "User-defined check smoke testing assertion")
  private Assertion<HttpExtraction> userDefinedSmokeAssertion;

  @ApiModelProperty(value = "Enabled or disabled security testing")
  private boolean security;

  @ApiModelProperty(value = "Enabled or disabled security testing, default Enabled")
  private SecurityCheckSetting securityCheckSetting;

  @ApiModelProperty(value = "User-defined check security testing assertion")
  private Assertion<HttpExtraction> userDefinedSecurityAssertion;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;


}



