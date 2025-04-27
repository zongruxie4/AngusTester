package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;

import cloud.xcan.angus.model.element.mock.data.MockData;
import cloud.xcan.angus.model.script.configuration.Configuration;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class MockDataScriptGenDto {

  @Schema(description = "Script id. Generate a new script when the script ID is empty")
  private Long scriptId;

  @Schema(description = "Project id. It is required to add scripts.")
  private Long projectId;

  @NotEmpty
  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Script execution plugin, when multiple plugins with the same name exist, use the plugin with the highest version number", requiredMode = RequiredMode.REQUIRED)
  private String plugin;

  @Valid
  @NotNull
  @Schema(description = "Common configuration parameters for script execution.", requiredMode = RequiredMode.REQUIRED)
  private Configuration configuration;

  @Valid
  @NotNull
  @Schema(description = "Mock data task", requiredMode = RequiredMode.REQUIRED)
  private MockData mockData;

}
