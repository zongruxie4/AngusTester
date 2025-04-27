package cloud.xcan.angus.core.tester.interfaces.task.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_REMARK_LENGTH_X30;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class TaskDescriptionReplaceDto {

  @Length(max = MAX_REMARK_LENGTH_X30)
  @Schema(description = "Task description, allow clear description by empty value")
  private String description;

}
