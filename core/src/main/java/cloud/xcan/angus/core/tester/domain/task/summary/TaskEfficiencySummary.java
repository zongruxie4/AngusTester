package cloud.xcan.angus.core.tester.domain.task.summary;

import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
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

  private Boolean backlog;

  protected TaskType taskType;

  protected BugLevel bugLevel;

  protected TaskStatus status;

  protected Priority priority;

  protected Boolean overdue;

  protected int failNum;

  protected int totalNum;

  protected BigDecimal evalWorkload;

  protected BigDecimal actualWorkload;

  protected Long assigneeId;

  protected Long testerId;

  protected Boolean missingBug;

  protected Boolean unplanned;

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
   * @see cloud.xcan.angus.core.tester.application.cmd.task.TaskCmd#start(Long)
   */
  public Integer getActualTotalNum() {
    return status.isPending() ? totalNum + 1 : totalNum;
  }

}
