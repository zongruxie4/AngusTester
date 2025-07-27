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

@Getter
@Setter
@Accessors(chain = true)
public class MockDataScriptGenDto {

  @Schema(description = "Script identifier for reuse or new script generation")
  private Long scriptId;

  @Schema(description = "Project identifier required for script creation and management")
  private Long projectId;

  @NotEmpty
  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Script execution plugin with version-based selection for highest compatibility", requiredMode = RequiredMode.REQUIRED)
  private String plugin;

  @Valid
  @NotNull
  @Schema(description = "Common configuration parameters for script execution with comprehensive settings", requiredMode = RequiredMode.REQUIRED)
  private Configuration configuration;

  @Valid
  @NotNull
  @Schema(description = "Mock data generation task configuration for comprehensive data simulation", requiredMode = RequiredMode.REQUIRED)
  private MockData mockData;

}
