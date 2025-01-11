package cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.trash;


import cloud.xcan.sdf.api.commonlink.TaskTargetType;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class TaskTrashDetailVo {

  private Long id;

  @NotNull
  @ApiModelProperty(required = true)
  private Long projectId;

  private String targetName;

  private TaskTargetType targetType;

  private Long targetId;

  private TaskType taskType;

  private Long createdBy;

  private String createdByName;

  private String createdByAvatar;

  private Long deletedBy;

  private String deletedByName;

  private String deletedByAvatar;

  private LocalDateTime deletedDate;

}



