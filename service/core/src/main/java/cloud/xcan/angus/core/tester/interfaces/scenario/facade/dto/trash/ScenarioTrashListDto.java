package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.trash;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter

public class ScenarioTrashListDto extends PageQuery {

  @NotNull
  @Schema(description = "Project identifier for trash scenario filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Scenario name for fuzzy search in trash")
  private String targetName;

  @Schema(description = "Scenario deletion date for temporal filtering")
  private LocalDateTime deletedDate;

}



