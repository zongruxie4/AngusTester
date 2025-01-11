package cloud.xcan.sdf.core.angustester.domain.script.count;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class ScriptCount {

  @ApiModelProperty(value = "Total number of script")
  private int totalScriptNum;

  @ApiModelProperty(value = "The number of performance script")
  private int perfScriptNum;
  @ApiModelProperty(value = "The number of function script")
  private int functionalScriptNum;
  @ApiModelProperty(value = "The number of stabilization script")
  private int stabilityScriptNum;
  @ApiModelProperty(value = "The number of customization script")
  private int customizationScriptNum;
  @ApiModelProperty(value = "The number of mock data script")
  private int mockDataScriptNum;
  @ApiModelProperty(value = "The number of mock apis script")
  private int mockApisScriptNum;

  @ApiModelProperty(value = "The number of created source script")
  private int createdSourceNum;
  @ApiModelProperty(value = "The number of imported source script")
  private int importedSourceNum;
  @ApiModelProperty(value = "The number of apis source script")
  private int apisSourceNum;
  @ApiModelProperty(value = "The number of case source script")
  private int caseSourceNum;
  @ApiModelProperty(value = "The number of scenario source script")
  private int scenarioSourceNum;

}
