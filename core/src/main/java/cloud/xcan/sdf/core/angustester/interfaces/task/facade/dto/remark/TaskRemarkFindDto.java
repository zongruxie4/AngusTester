package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.remark;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author xiaolong.liu
 */
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class TaskRemarkFindDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(required = true)
  private Long taskId;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }


}
