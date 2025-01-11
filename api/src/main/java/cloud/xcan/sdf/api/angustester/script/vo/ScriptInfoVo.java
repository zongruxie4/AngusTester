package cloud.xcan.sdf.api.angustester.script.vo;


import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.model.script.ScriptSource;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
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

  private Boolean authFlag;

  // Note: For angusctrl check permission
  @ApiModelProperty(value = "Authorization permissions(Operation permission)")
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



