package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class ExecAddByContentDto {

  @NotNull
  @Schema(description = "Project identifier for execution association", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Execution name for identification and organization")
  private String name;

  @NotEmpty
  @Schema(description = "Script content for direct execution with inline script definition", requiredMode = RequiredMode.REQUIRED)
  private String scriptContent;

  @Schema(description = "Trial execution flag for testing and validation purposes")
  private Boolean trial;

}
