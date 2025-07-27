package cloud.xcan.angus.core.tester.interfaces.task.facade.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TaskConfirmorReplaceDto {

  @Schema(description = "Reviewer identifier for task validation and approval. Empty value clears the current reviewer")
  private Long confirmorId;

}
