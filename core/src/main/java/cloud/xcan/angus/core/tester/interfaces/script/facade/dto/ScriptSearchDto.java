package cloud.xcan.angus.core.tester.interfaces.script.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ScriptSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  private Long serviceId;

  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  private ScriptType type;

  private ScriptSource source;

  private String sourceId;

  @Length(max = MAX_NAME_LENGTH)
  private String tag;

  @Length(max = MAX_NAME_LENGTH)
  private String plugin;

  @Schema(description = "Required when app administrators query all scripts")
  private Boolean admin;

  @Schema(description = "Required when the user query has the one permission script")
  private ScriptPermission hasPermission;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
