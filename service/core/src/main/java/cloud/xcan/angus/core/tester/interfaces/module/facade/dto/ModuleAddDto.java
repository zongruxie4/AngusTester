package cloud.xcan.angus.core.tester.interfaces.module.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_ROOT_PID;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ModuleAddDto {

  @NotNull
  @Schema(name = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Min(DEFAULT_ROOT_PID)
  @Schema(name = "Parent module ID, null and -1 indicate that it is a top-level node", example = "-1")
  private Long pid;

  @Schema(description = "Sorting value, the smaller the value, the higher", example = "10000", requiredMode = RequiredMode.REQUIRED)
  private Integer sequence;

  @NotEmpty
  @Schema(name = "Module name", requiredMode = RequiredMode.REQUIRED)
  private LinkedHashSet<String> names;

}
