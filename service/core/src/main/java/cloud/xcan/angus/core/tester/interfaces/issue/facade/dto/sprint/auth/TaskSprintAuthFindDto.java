package cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class TaskSprintAuthFindDto extends PageQuery {

  //@NotNull -> Transferring values in filters
  @Schema(description = "Task sprint identifier for authorization filtering", requiredMode = RequiredMode.REQUIRED)
  private Long sprintId;

  @NotNull
  @Schema(example = "USER", description = "Authorization object type for permission classification", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object identifier for user or role filtering")
  private Long authObjectId;

  @Schema(description = "Authorization creation timestamp for timeline filtering")
  private Date createdDate;

}



