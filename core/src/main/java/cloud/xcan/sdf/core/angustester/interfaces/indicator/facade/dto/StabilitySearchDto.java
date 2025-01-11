package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.web.validator.annotations.EnumPart;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class StabilitySearchDto extends PageQuery {

  @ApiModelProperty(value = "Perf indicator id")
  private Long id;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API", "SCENARIO"})
  @ApiModelProperty(allowableValues = "API,SCENARIO", required = true)
  private CombinedTargetType targetType;

  @ApiModelProperty(value = "Required when app administrators query all stability indicator")
  private Boolean adminFlag;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Target name")
  private String targetName;

  @ApiModelProperty(value = "Stability indicator creator id")
  private Long createdBy;

  @ApiModelProperty(value = "Stability indicator creation time ")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}



