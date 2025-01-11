package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_TAGS_NUM;
import static cloud.xcan.sdf.core.angustester.domain.TesterFuncPluginConstant.MAX_CASE_OR_TASK_REFS_NUM;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_KEY_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X30;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Accessors(chain = true)
public class TaskReplaceDto {

  // No modification allowed, only movement allowed
  //  @NotNull
  //  @ApiModelProperty(value = "Sprint Id", example = "1", required = true)
  //  private Long sprintId;

  @ApiModelProperty(value = "Function case module id")
  private Long moduleId;

  @NotEmpty
  @Length(max = DEFAULT_NAME_LENGTH_X4)
  @ApiModelProperty(example = "A task", required = true)
  private String name;

  @Length(max = DEFAULT_KEY_LENGTH)
  @ApiModelProperty(value = "Version of software for the task")
  private String softwareVersion;

  // @EnumPart(enumClass = TaskType.class, allowableValues = {"BUG", "STORY", "TASK"}) -> Do in biz
  @ApiModelProperty(value = "Task type. Note: The test task type cannot be modified", example = "TASK")
  private TaskType taskType;

  @ApiModelProperty(value = "Bug level, default MINOR",example = "MINOR")
  private BugLevel bugLevel;

  //@NotNull Fix:: Backlog assignee is nullable
  @ApiModelProperty(value = "Assignee id"/*, required = true*/)
  private Long assigneeId;

  @ApiModelProperty(value = "Confirmor id")
  private Long confirmorId;

  @ApiModelProperty(value = "Task tester")
  private Long testerId;

  @ApiModelProperty(value = "Is it a missing bug")
  private Boolean missingBugFlag;

  @Size(max = MAX_TAGS_NUM)
  @ApiModelProperty(value = "Report ids")
  private LinkedHashSet<Long> tagIds;

  @ApiModelProperty(example = "MEDIUM", value = "Default MEDIUM")
  private Priority priority = Priority.DEFAULT;

  //@Future Fix:: Save expired task exception
  //@NotNull Fix:: Backlog deadlineDate is nullable
  @ApiModelProperty(example = "2023-01-02 12:00:00", value = "Task deadline")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  @Valid
  @Size(max = MAX_ATTACHMENT_NUM)
  @ApiModelProperty(value = "Task attachments")
  private List<Attachment> attachments;

  @Length(max = DEFAULT_REMARK_LENGTH_X30)
  @ApiModelProperty(value = "Task description")
  private String description;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @ApiModelProperty(value = "Task Workload")
  private BigDecimal evalWorkload;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @ApiModelProperty(value = "Actual usage workload")
  private BigDecimal actualWorkload;

  private Long parentTaskId;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @ApiModelProperty(value = "Reference task ids")
  private LinkedHashSet<Long> refTaskIds;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @ApiModelProperty(value = "Reference case ids")
  private LinkedHashSet<Long> refCaseIds;
}
