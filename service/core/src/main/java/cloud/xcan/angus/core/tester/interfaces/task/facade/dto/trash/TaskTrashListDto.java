package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.trash;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.api.commonlink.TaskTargetType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
public class TaskTrashListDto extends PageQuery {

  @Schema(description = "Project identifier for trash item filtering and organization")
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Target item name for partial matching search in trash")
  private String targetName;

  @NotNull
  @Schema(description = "Target item type for task or sprint classification", requiredMode = RequiredMode.REQUIRED)
  private TaskTargetType targetType;

  @Schema(description = "Item deletion timestamp for timeline filtering")
  private LocalDateTime deletedDate;

}



