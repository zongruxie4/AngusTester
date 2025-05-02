package cloud.xcan.angus.core.tester.interfaces.task.facade.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Valid
@Getter
@Setter
@Accessors(chain = true)
public class TaskAssigneeReplaceDto {

  @Schema(description = "Assignee id, allow clear assignee by empty value")
  private Long assigneeId;

}
