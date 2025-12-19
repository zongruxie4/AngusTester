package cloud.xcan.angus.core.tester.interfaces.config.facade.vo;


import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeExecVo {

  private Long id;

  private String no;

  private String name;

  private String plugin;

  private ScriptType scriptType;

  private ExecStatus status;

  private int priority;

  private LocalDateTime actualStartDate;

  private Long execBy;

  @NameJoinField(id = "execBy", repository = "commonUserBaseRepo")
  private String execByName;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String creator;

  private LocalDateTime createdDate;

}
