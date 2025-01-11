package cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TaskInfoVo {

  private Long id;

  private String name;

  private String code;

  private String version;

  private TaskStatus status;

  private Long projectId;

  private Long sprintId;

  private Priority priority;

  private TaskType taskType;

  private BugLevel bugLevel;

  private TestType testType;

  private BigDecimal evalWorkload;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  private Boolean favouriteFlag;

  private Boolean followFlag;

  private Long assigneeId;

  @NameJoinField(id = "assigneeId", repository = "commonUserBaseRepo")
  private String assigneeName;

  private String assigneeAvatar;

  private Long confirmorId;

  @NameJoinField(id = "confirmorId", repository = "commonUserBaseRepo")
  private String confirmorName;

  private Long testerId;

  @NameJoinField(id = "testerId", repository = "commonUserBaseRepo")
  private String testerName;

  private Boolean missingBugFlag;

  private Boolean unplannedFlag;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
