package cloud.xcan.angus.api.tester.script.vo;


import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.TestPlatform;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ScriptInfosVo {

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

  private String plugin;

  private String description;

  //private String content;

  private Long tenantId;

  private Long createdBy;

  private LocalDateTime createdDate;

  private Long modifiedBy;

  private LocalDateTime modifiedDate;

}



