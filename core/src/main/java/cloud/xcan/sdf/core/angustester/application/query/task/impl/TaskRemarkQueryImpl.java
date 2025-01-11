package cloud.xcan.sdf.core.angustester.application.query.task.impl;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_TASK_REMARK_NUM;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_REMARK_OVER_LIMIT_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_REMARK_OVER_LIMIT_T;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.converter.TaskConverter;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskRemarkQuery;
import cloud.xcan.sdf.core.angustester.domain.task.remark.TaskRemark;
import cloud.xcan.sdf.core.angustester.domain.task.remark.TaskRemarkRepo;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskRemarkSummary;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author xiaolong.liu
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
      protected void checkParams() {
        // NOOP
      }

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
