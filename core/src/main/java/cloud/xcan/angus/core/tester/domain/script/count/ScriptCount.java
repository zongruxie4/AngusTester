package cloud.xcan.angus.core.tester.domain.script.count;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class ScriptCount {

  @Schema(description = "Total number of script")
  private int totalScriptNum;

  @Schema(description = "The number of performance script")
  private int perfScriptNum;
  @Schema(description = "The number of function script")
  private int functionalScriptNum;
  @Schema(description = "The number of stabilization script")
  private int stabilityScriptNum;
  @Schema(description = "The number of customization script")
  private int customizationScriptNum;
  @Schema(description = "The number of mock data script")
  private int mockDataScriptNum;
  @Schema(description = "The number of mock apis script")
  private int mockApisScriptNum;

  @Schema(description = "The number of created source script")
  private int createdSourceNum;
  @Schema(description = "The number of imported source script")
  private int importedSourceNum;
  @Schema(description = "The number of apis source script")
  private int apisSourceNum;
  @Schema(description = "The number of case source script")
  private int caseSourceNum;
  @Schema(description = "The number of scenario source script")
  private int scenarioSourceNum;

}
