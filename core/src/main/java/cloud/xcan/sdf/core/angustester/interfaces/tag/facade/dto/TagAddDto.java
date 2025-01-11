package cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashSet;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TagAddDto {

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long projectId;

  @NotEmpty
  @Size(max = DEFAULT_BATCH_SIZE)
  @ApiModelProperty(required = true)
  private LinkedHashSet<String> names;
}
