package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.favorite;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter

public class TaskFavouriteFindDto extends PageQuery {

  @NotNull
  @Schema(description = "Project identifier for favorite task filtering and organization", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Task display name for partial matching search in favorites")
  private String taskName;

}



