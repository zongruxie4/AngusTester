package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PRIORITY;
import static cloud.xcan.angus.spec.experimental.BizConstant.MIN_PRIORITY;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.StartMode;
import cloud.xcan.angus.model.script.configuration.TestPlatform;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Setter
@Getter
@Accessors(chain = true)
public class ExecFindDto extends PageQuery {

  @NotNull
  @Schema(description = "Project identifier for execution query scope definition", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Execution identifier for precise query")
  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Execution name for fuzzy search and filtering")
  private String name;

  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Plugin name for execution filtering")
  private String plugin;

  @Schema(description = "Execution status for state-based filtering")
  private ExecStatus status;

  @Schema(description = "Type of platform for test execution")
  private TestPlatform platform;

  @Schema(description = "Script identifier for script-based filtering")
  private Long scriptId;

  @Schema(description = "Script type for type-based filtering")
  private ScriptType scriptType;

  @Schema(description = "Script source for source-based filtering")
  private ScriptSource scriptSource;

  @Schema(description = "Script source identifier for source-based filtering")
  private Long scriptSourceId;

  @Range(min = MIN_PRIORITY, max = MAX_PRIORITY)
  @Schema(description = "Execution priority for priority-based filtering")
  private Integer priority;

  @Schema(description = "Node identifier for node-based filtering")
  private Long nodeId;

  @Schema(description = "Start mode for execution mode filtering")
  private StartMode startMode;

  @Schema(description = "Scheduled start date for temporal filtering")
  private LocalDateTime startDate;

  @Schema(description = "Actual start date for temporal filtering")
  private LocalDateTime actualStartDate;

  @Schema(description = "End date for temporal filtering")
  private LocalDateTime endDate;

  @Schema(description = "Executor identifier for executor-based filtering")
  private Long execBy;

  @Schema(description = "Creator identifier for ownership filtering")
  private Long createdBy;

  @Schema(description = "Creation date for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Last modifier identifier for update tracking")
  private Long lastModifiedBy;

  @Schema(description = "Last modification date for temporal filtering")
  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
