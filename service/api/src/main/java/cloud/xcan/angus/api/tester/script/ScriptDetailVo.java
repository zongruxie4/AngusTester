package cloud.xcan.angus.api.tester.script;


import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.TestPlatform;
import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScriptDetailVo {

  private Long id;

  private Long projectId;

  private Long serviceId;

  private String name;

  private TestPlatform platform;

  private ScriptType type;

  private ScriptSource source;

  private Long sourceId;

  private String sourceName;

  private Boolean auth;

  private List<String> tags;

  // Note: For ctrl check permission
  @Schema(description = "Authorization permissions(Operation permission)")
  private List<ScriptPermission> permissions;

  private String plugin;

  private String description;

  private String content;

  private Long tenantId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}



