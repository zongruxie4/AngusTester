package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.remark;

import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TaskRemarkAddDto {

  @NotNull
  @Schema(description = "Task id", requiredMode = RequiredMode.REQUIRED)
  private Long taskId;

  @EditorContentLength(max = 6000)
  @Schema(description = "Task quoteRemark content")
  private String content;
}
