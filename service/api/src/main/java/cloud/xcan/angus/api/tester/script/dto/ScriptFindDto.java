package cloud.xcan.angus.api.tester.script.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.TestPlatform;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class ScriptFindDto extends PageQuery {

  @Schema(description = "Script identifier for specific script lookup")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for script filtering and organization", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Service identifier for service-specific script filtering")
  private Long serviceId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Script name for partial matching search")
  private String name;

  @Schema(description = "Type of platform for test execution")
  private TestPlatform platform;

  @Schema(description = "Script execution type for methodology filtering", example = "TEST_PERFORMANCE")
  private ScriptType type;

  @Schema(description = "Script source type for origin classification", example = "CREATED")
  private ScriptSource source;

  @Schema(description = "Source identifier for external script reference tracking")
  private String sourceId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Script tag for classification and organization")
  private String tag;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Script plugin for extension functionality filtering")
  private String plugin;

  @Schema(description = "Administrator access flag for cross-project script querying")
  private Boolean admin;

  @Schema(description = "Permission level filter for user access control validation")
  private ScriptPermission hasPermission;

  @Schema(description = "User identifier who created the script")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Script creation timestamp for timeline filtering")
  private LocalDateTime createdDate;

  @Schema(description = "User identifier who last modified the script")
  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Script last modification timestamp for version tracking")
  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
