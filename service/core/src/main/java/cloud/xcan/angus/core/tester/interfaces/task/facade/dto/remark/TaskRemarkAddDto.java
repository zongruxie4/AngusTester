package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.remark;

import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TaskRemarkAddDto {

  @NotNull
  @Schema(description = "Task identifier for remark association and context", requiredMode = RequiredMode.REQUIRED)
  private Long taskId;

  @EditorContentLength(max = 6000)
  @Schema(description = "Task remark content for collaboration and documentation")
  private String content;
}
