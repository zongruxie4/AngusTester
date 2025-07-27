package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.domain.script.ScriptFormat;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class DatasetExportDto extends PageQuery {

  @NotNull
  @Schema(description = "Project identifier for dataset export scope definition", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Specific dataset identifiers for selective export. All datasets will be exported if empty")
  @Size(max = MAX_BATCH_SIZE)
  private LinkedHashSet<Long> ids;

  @Parameter(description = "Export format specification with YAML as default")
  private ScriptFormat format;

}



