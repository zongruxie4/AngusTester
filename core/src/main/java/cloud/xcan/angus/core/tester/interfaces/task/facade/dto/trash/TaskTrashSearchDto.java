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

public class TaskTrashSearchDto extends PageQuery {

  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  private String targetName;

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private TaskTargetType targetType;

  private LocalDateTime deletedDate;

}



