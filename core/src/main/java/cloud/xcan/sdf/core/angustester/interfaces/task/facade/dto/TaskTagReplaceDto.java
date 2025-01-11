package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_TAGS_NUM;

import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashSet;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TaskTagReplaceDto {

  @Size(max = MAX_TAGS_NUM)
  @ApiModelProperty(value = "Report ids, allow clear tags by empty value")
  private LinkedHashSet<Long> tagIds;

}
