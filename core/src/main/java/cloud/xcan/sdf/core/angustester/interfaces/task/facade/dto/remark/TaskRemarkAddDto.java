package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.remark;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X30;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TaskRemarkAddDto {

  @NotNull
  @ApiModelProperty(value = "Task id", example = "1", required = true)
  private Long taskId;

  @Length(max = DEFAULT_REMARK_LENGTH_X30)
  @ApiModelProperty(value = "Task quoteRemark content")
  private String content;
}
