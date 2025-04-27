package cloud.xcan.angus.core.tester.interfaces.module.facade.dto;

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
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Min(DEFAULT_ROOT_PID)
  @Schema(name = "Parent module ID", example = "1")
  private Long pid;

  @Schema(description = "Sorting value, the smaller the value, the higher", example = "10000")
  private Integer sequence;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(name = "Module name", example = "Sprint-1")
  private String name;

}
