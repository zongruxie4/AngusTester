package cloud.xcan.sdf.core.angustester.interfaces.activity.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
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
public class ActivityFindDto extends PageQuery {

  @ApiModelProperty(value = "Activity id")
  private Long id;

  @ApiModelProperty(value = "Project id")
  private Long projectId;

  @ApiModelProperty(value = "Target id")
  private Long targetId;

  @ApiModelProperty(value = "Target parent id")
  private Long parentTargetId;

  @ApiModelProperty(value = "Target type")
  private CombinedTargetType targetType;

  @ApiModelProperty(value = "Operation user id")
  private Long userId;

  @ApiModelProperty(value = "Main target id")
  private Long mainTargetId;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(value = "For fulltext search")
  private String detail;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime optDate;

  @Override
  public String getDefaultOrderBy() {
    return "optDate";
  }
}
