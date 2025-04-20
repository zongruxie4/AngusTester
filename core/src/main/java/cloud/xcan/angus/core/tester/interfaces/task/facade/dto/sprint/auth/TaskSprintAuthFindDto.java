package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Valid
@Setter
@Getter

public class TaskSprintAuthFindDto extends PageQuery {

  //@NotNull -> Transferring values in filters
  @Schema(description = "Task sprint id", requiredMode = RequiredMode.REQUIRED)
  private Long sprintId;

  @NotNull
  @Schema(example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object id")
  private Long authObjectId;

  private Date createdDate;

}



