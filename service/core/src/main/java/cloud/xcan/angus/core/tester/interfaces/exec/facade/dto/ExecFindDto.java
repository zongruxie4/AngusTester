package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PRIORITY;
import static cloud.xcan.angus.spec.experimental.BizConstant.MIN_PRIORITY;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.StartMode;
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
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  private String name;

  @Length(max = MAX_KEY_LENGTH)
  private String plugin;

  private ExecStatus status;

  private Long scriptId;

  private ScriptType scriptType;

  private ScriptSource scriptSource;

  private Long scriptSourceId;

  @Range(min = MIN_PRIORITY, max = MAX_PRIORITY)
  private Integer priority;

  private Long nodeId;

  private StartMode startMode;

  private LocalDateTime startDate;

  private LocalDateTime actualStartDate;

  private LocalDateTime endDate;

  private Long execBy;

  private Long createdBy;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
