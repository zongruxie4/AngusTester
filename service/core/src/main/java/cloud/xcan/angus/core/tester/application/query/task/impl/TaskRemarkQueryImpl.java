package cloud.xcan.angus.core.tester.application.query.task.impl;

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
import cloud.xcan.angus.core.tester.application.query.task.TaskRemarkQuery;
import cloud.xcan.angus.core.tester.domain.task.remark.TaskRemark;
import cloud.xcan.angus.core.tester.domain.task.remark.TaskRemarkRepo;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskRemarkSummary;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author XiaoLong Liu
 */
@Biz
public class TaskRemarkQueryImpl implements TaskRemarkQuery {

  @Resource
  private TaskRemarkRepo taskRemarkRepo;

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

  @Override
  public void checkAddQuota(Long taskId, int inc) {
    if (taskRemarkRepo.countAllByTaskId(taskId) + inc > MAX_TASK_REMARK_NUM) {
      throw BizException.of(TASK_REMARK_OVER_LIMIT_CODE, TASK_REMARK_OVER_LIMIT_T,
          new Object[]{MAX_TASK_REMARK_NUM});
    }
  }

  @Override
  public TaskRemark checkAndFind(Long id) {
    return taskRemarkRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "TaskRemark"));
  }

  @NameJoin
  @Override
  public List<TaskRemarkSummary> findSummaryByTaskId(Long taskId) {
    List<TaskRemark> taskRemarks = taskRemarkRepo.findByTaskId(taskId);
    return isEmpty(taskRemarks) ? null
        : taskRemarks.stream().map(TaskConverter::toTaskRemarkSummary).collect(Collectors.toList());
  }

  @Override
  public int getRemarkNum(Long taskId) {
    return taskRemarkRepo.countAllByTaskId(taskId);
  }
}
