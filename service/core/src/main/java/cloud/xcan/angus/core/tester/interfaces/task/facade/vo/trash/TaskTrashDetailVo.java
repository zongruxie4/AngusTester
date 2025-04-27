package cloud.xcan.angus.core.tester.interfaces.task.facade.vo.trash;


import cloud.xcan.angus.api.commonlink.TaskTargetType;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class TaskTrashDetailVo {

  private Long id;

  @NotNull
  @Schema(required = true)
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



