package cloud.xcan.angus.core.tester.application.query.issue.impl;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_TASK_REMARK_NUM;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_REMARK_OVER_LIMIT_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_REMARK_OVER_LIMIT_T;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.converter.TaskConverter;
import cloud.xcan.angus.core.tester.application.query.issue.TaskRemarkQuery;
import cloud.xcan.angus.core.tester.domain.issue.remark.TaskRemark;
import cloud.xcan.angus.core.tester.domain.issue.remark.TaskRemarkRepo;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskRemarkSummary;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of TaskRemarkQuery for task remark management and query operations.
 * </p>
 * <p>
 * Provides methods for task remark CRUD operations, quota validation, and remark statistics.
 * </p>
 */
@Biz
public class TaskRemarkQueryImpl implements TaskRemarkQuery {

  @Resource
  private TaskRemarkRepo taskRemarkRepo;

  /**
   * <p>
   * List task remarks with pagination and filtering.
   * </p>
   * <p>
   * Retrieves paginated task remarks based on generic specification criteria.
   * </p>
   *
   * @param spec     Generic specification for filtering
   * @param pageable Pagination information
   * @return Page of task remarks
   */
  @Override
  public Page<TaskRemark> list(GenericSpecification<TaskRemark> spec,
      PageRequest pageable) {
    return new BizTemplate<Page<TaskRemark>>() {

      @Override
      protected Page<TaskRemark> process() {
        return taskRemarkRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * <p>
   * Check and validate remark quota for a task.
   * </p>
   * <p>
   * Validates that adding the specified number of remarks does not exceed quota limits. Throws an
   * exception if quota would be exceeded.
   * </p>
   *
   * @param taskId Task ID
   * @param inc    Number of remarks to add
   */
  @Override
  public void checkAddQuota(Long taskId, int inc) {
    if (taskRemarkRepo.countAllByTaskId(taskId) + inc > MAX_TASK_REMARK_NUM) {
      throw BizException.of(TASK_REMARK_OVER_LIMIT_CODE, TASK_REMARK_OVER_LIMIT_T,
          new Object[]{MAX_TASK_REMARK_NUM});
    }
  }

  /**
   * <p>
   * Check and find a task remark by ID.
   * </p>
   * <p>
   * Validates the existence of a task remark and throws ResourceNotFound if not found.
   * </p>
   *
   * @param id Task remark ID
   * @return Task remark if found
   * @throws ResourceNotFound if task remark not found
   */
  @Override
  public TaskRemark checkAndFind(Long id) {
    return taskRemarkRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "TaskRemark"));
  }

  /**
   * <p>
   * Find remark summaries by task ID.
   * </p>
   * <p>
   * Retrieves aggregated remark information for a specific task.
   * </p>
   *
   * @param taskId Task ID
   * @return List of remark summaries
   */
  @NameJoin
  @Override
  public List<TaskRemarkSummary> findSummaryByTaskId(Long taskId) {
    List<TaskRemark> taskRemarks = taskRemarkRepo.findByTaskId(taskId);
    return isEmpty(taskRemarks) ? null
        : taskRemarks.stream().map(TaskConverter::toTaskRemarkSummary).toList();
  }

  /**
   * <p>
   * Get the number of remarks for a task.
   * </p>
   * <p>
   * Returns the total count of remarks associated with a specific task.
   * </p>
   *
   * @param taskId Task ID
   * @return Number of remarks
   */
  @Override
  public int getRemarkNum(Long taskId) {
    return taskRemarkRepo.countAllByTaskId(taskId);
  }
}
