package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ScriptStatisticsDto extends PageQuery {

  @Schema(description = "Script identifier")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for script statistics", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Script name for search and filtering")
  private String name;

  @Schema(description = "Script type for categorization")
  private ScriptType type;

  @Schema(description = "Script source for origin tracking")
  private ScriptSource source;

  @Schema(description = "Service identifier associated with the script")
  private Long serviceId;

  @Schema(description = "Source identifier for external script reference")
  private String sourceId;

  @Schema(description = "Scenario identifier associated with the script")
  private Long scenarioId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Script tag for categorization")
  private String tag;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Script plugin type")
  private String plugin;

  @Schema(description = "Administrator access flag for cross-project script queries")
  private Boolean admin;

  @Schema(description = "Permission level for script access control")
  private ScriptPermission hasPermission;

}
