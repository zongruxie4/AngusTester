package cloud.xcan.angus.core.tester.interfaces.task.facade.dto;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_TAGS_NUM;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TaskTagReplaceDto {

  @Size(max = MAX_TAGS_NUM)
  @Schema(description = "Tag identifiers for task categorization and filtering. Empty value removes all current tags")
  private LinkedHashSet<Long> tagIds;

}
