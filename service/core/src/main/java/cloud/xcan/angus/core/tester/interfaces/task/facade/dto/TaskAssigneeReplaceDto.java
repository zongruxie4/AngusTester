package cloud.xcan.angus.core.tester.interfaces.task.facade.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TaskAssigneeReplaceDto {

  @Schema(description = "Primary assignee identifier for task responsibility assignment. Empty value clears the current assignee")
  private Long assigneeId;

}
