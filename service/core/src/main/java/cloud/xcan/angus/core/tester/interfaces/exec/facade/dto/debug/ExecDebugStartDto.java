package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.pipeline.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class ExecDebugStartDto {

  @Schema(description = "Whether to notify other controllers to handle")
  private boolean broadcast;

  @Schema(description = "Debug id")
  private Long id;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Execution name", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotEmpty
  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Execution plugin name. Note: If there are multiple plugins with the same name, the highest version of the plugin will be automatically used", requiredMode = RequiredMode.REQUIRED)
  private String plugin;

  @Valid
  @NotNull
  @Schema(description = "Execution configuration", requiredMode = RequiredMode.REQUIRED)
  private Configuration configuration;

  @Valid
  @NotNull
  @Schema(description = "Execution task information", requiredMode = RequiredMode.REQUIRED)
  private Task task;

  public ExecDebugStartDto() {
  }

  private ExecDebugStartDto(Builder builder) {
    setBroadcast(builder.broadcast);
    setId(builder.id);
    setName(builder.name);
    setPlugin(builder.plugin);
    setConfiguration(builder.configuration);
    setTask(builder.task);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {

    private boolean broadcast;
    private Long id;
    private String name;
    private String plugin;
    private Configuration configuration;
    private Task task;

    private Builder() {
    }

    public Builder broadcast(boolean val) {
      broadcast = val;
      return this;
    }

    public Builder id(Long val) {
      id = val;
      return this;
    }

    public Builder name(String val) {
      name = val;
      return this;
    }

    public Builder plugin(String val) {
      plugin = val;
      return this;
    }

    public Builder configuration(Configuration val) {
      configuration = val;
      return this;
    }

    public Builder task(Task val) {
      task = val;
      return this;
    }

    public ExecDebugStartDto build() {
      return new ExecDebugStartDto(this);
    }
  }
}
