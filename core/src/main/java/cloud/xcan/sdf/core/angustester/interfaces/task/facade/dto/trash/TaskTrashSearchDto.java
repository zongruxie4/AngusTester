package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.trash;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.commonlink.TaskTargetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@ApiModel
public class TaskTrashSearchDto extends PageQuery {

  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String targetName;

  @NotNull
  @ApiModelProperty(required = true)
  private TaskTargetType targetType;

  private LocalDateTime deletedDate;

}



