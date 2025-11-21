package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_ROOT_PID;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ModuleUpdateDto {

  @NotNull
  @Schema(description = "Module identifier for update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Min(DEFAULT_ROOT_PID)
  @Schema(description = "Parent module identifier for hierarchical structure", example = "1")
  private Long pid;

  @Schema(description = "Sorting value; smaller values indicate higher priority", example = "10000")
  private Integer sequence;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Module name for identification and display", example = "Sprint-1")
  private String name;

}
