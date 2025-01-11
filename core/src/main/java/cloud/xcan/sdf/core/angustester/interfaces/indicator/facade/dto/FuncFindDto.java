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
public class FuncFindDto extends PageQuery {

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"SERVICE", "API"})
  @ApiModelProperty(allowableValues = "SERVICE,API", required = true)
  private CombinedTargetType targetType;

  @ApiModelProperty(value = "Required when app administrators query all Functionality indicator")
  private Boolean adminFlag;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Target name")
  private String targetName;

  @ApiModelProperty(value = "Functionality indicator creator id")
  private Long createdBy;

  @ApiModelProperty(value = "Functionality indicator creation time")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}



