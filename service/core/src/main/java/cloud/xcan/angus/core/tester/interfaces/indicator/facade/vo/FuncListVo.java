package cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.gm.indicator.SecurityCheckSetting;
import cloud.xcan.angus.api.gm.indicator.SmokeCheckSetting;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class FuncListVo {

  private Long id;

  private Long targetId;

  private CombinedTargetType targetType;

  private String targetName;

  @Schema(description = "Enabled or disabled smoke testing, default Enabled")
  private boolean smoke;

  @Schema(description = "Enabled or disabled smoke testing")
  private SmokeCheckSetting smokeCheckSetting;

  @Schema(description = "User-defined check smoke testing assertion")
  private Assertion<HttpExtraction> userDefinedSmokeAssertion;

  @Schema(description = "Enabled or disabled security testing")
  private boolean security;

  @Schema(description = "Enabled or disabled security testing, default Enabled")
  private SecurityCheckSetting securityCheckSetting;

  @Schema(description = "User-defined check security testing assertion")
  private Assertion<HttpExtraction> userDefinedSecurityAssertion;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;


}



