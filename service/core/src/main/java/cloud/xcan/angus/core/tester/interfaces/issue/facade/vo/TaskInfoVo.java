package cloud.xcan.angus.core.tester.interfaces.issue.facade.vo;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.tester.domain.issue.BugLevel;
import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.NameJoinField;
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

  private Progress progress;

  private Long projectId;

  private Long sprintId;

  private Priority priority;

  private TaskType taskType;

  private BugLevel bugLevel;

  private TestType testType;

  private BigDecimal evalWorkload;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  private Boolean favourite;

  private Boolean follow;

  private Long assigneeId;

  @NameJoinField(id = "assigneeId", repository = "commonUserBaseRepo")
  private String assigneeName;

  private String assigneeAvatar;

  private Long confirmerId;

  @NameJoinField(id = "confirmerId", repository = "commonUserBaseRepo")
  private String confirmerName;

  private Long testerId;

  @NameJoinField(id = "testerId", repository = "commonUserBaseRepo")
  private String testerName;

  private Boolean missingBug;

  private Boolean unplanned;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
