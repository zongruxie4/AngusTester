package cloud.xcan.angus.api.tester.script.vo;


import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ScriptInfoVo {

  private Long id;

  private Long projectId;

  private Long serviceId;

  private String name;

  private ScriptType type;

  private ScriptSource source;

  private Long sourceId;

  //private String sourceName;

  private Boolean auth;

  // Note: For angusctrl check permission
  @Schema(description = "Authorization permissions(Operation permission)")
  private List<ScriptPermission> permissions;

  private String plugin;

  private String description;

  private String content;

  private Long tenantId;

  private Long createdBy;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  private LocalDateTime lastModifiedDate;

}



