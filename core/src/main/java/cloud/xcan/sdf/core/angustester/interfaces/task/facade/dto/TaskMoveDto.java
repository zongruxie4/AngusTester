package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TaskMoveDto {

  @NotNull
  @Size(min = 1)
  @ApiModelProperty(value = "Source task ids", required = true)
  private List<Long> taskIds;

  //@NotNull
  @ApiModelProperty(example = "1", value = "Target sprint id. When empty, it means moving from sprint to product backlog"/*, required = true*/)
  private Long targetSprintId;

}
