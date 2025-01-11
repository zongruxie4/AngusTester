package cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 *
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TaskAssocVo {

  private Long id;

  private String name;

  private String code;

  //private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  private Long sprintId;

  @NameJoinField(id = "sprintId", repository = "taskSprintRepo")
  private String sprintName;

  private Long targetId;

  private String targetName;

  private TaskType taskType;

  private TestType testType;

  private Long assigneeId;

  @NameJoinField(id = "assigneeId", repository = "commonUserBaseRepo")
  private String assigneeName;

  private Long confirmorId;

  @NameJoinField(id = "confirmorId", repository = "commonUserBaseRepo")
  private String confirmorName;

  //private List<IdAndNameVo> tags;

  private TaskStatus status;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  public ScriptType getScriptType() {
    return testType.toScriptType();
  }
}
