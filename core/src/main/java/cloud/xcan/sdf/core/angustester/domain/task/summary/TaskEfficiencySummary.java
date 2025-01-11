package cloud.xcan.sdf.core.angustester.domain.task.summary;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaskEfficiencySummary implements IdAndCreatedDateBase<TaskEfficiencySummary> {

  protected Long id;

  private Boolean backlogFlag;

  protected TaskType taskType;

  protected BugLevel bugLevel;

  protected TaskStatus status;

  protected Priority priority;

  protected Boolean overdueFlag;

  protected int failNum;

  protected int totalNum;

  protected BigDecimal evalWorkload;

  protected BigDecimal actualWorkload;

  protected Long assigneeId;

  protected Long testerId;

  protected Boolean missingBugFlag;

  protected Boolean unplannedFlag;

  protected LocalDateTime startDate;

  protected LocalDateTime deadlineDate;

  protected LocalDateTime completedDate;

  protected Long createdBy;

  protected LocalDateTime createdDate;

  public BugLevel getBugLevel() {
    return taskType.isBug() ? nullSafe(bugLevel, BugLevel.DEFAULT) : null;
  }

  public BigDecimal getEvalWorkload() {
    return nullSafe(evalWorkload, BigDecimal.ZERO);
  }

  public BigDecimal getActualWorkload() {
    return nullSafe(actualWorkload, BigDecimal.ZERO);
  }

  public BigDecimal getSavingWorkload() {
    return nullSafe(evalWorkload, BigDecimal.ZERO).subtract(
        nullSafe(actualWorkload, BigDecimal.ZERO));
  }

  /**
   * @see cloud.xcan.sdf.core.angustester.application.cmd.task.TaskCmd#start(Long)
   */
  public Integer getActualTotalNum() {
    return status.isPending() ? totalNum + 1 : totalNum;
  }

}
