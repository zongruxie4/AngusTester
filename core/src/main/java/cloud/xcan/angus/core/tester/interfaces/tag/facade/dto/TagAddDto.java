package cloud.xcan.angus.core.tester.interfaces.tag.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TagAddDto {

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotEmpty
  @Size(max = MAX_BATCH_SIZE)
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private LinkedHashSet<String> names;
}
