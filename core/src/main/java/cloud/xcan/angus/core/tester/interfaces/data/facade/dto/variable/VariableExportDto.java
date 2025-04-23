package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.domain.script.ScriptFormat;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class VariableExportDto extends PageQuery {

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Variable ids. Note: All the variables wil be exported if ids are empty.")
  @Size(max = MAX_BATCH_SIZE)
  private LinkedHashSet<Long> ids;

  @Schema(name = "format", description = "Script format, default yml")
  private ScriptFormat format;

}



