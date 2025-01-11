package cloud.xcan.sdf.core.angustester.application.query.task;

import cloud.xcan.sdf.core.angustester.domain.task.remark.TaskRemark;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskRemarkSummary;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author xiaolong.liu
 */
public interface TaskRemarkQuery {

  Page<TaskRemark> list(GenericSpecification<TaskRemark> spec, PageRequest pageable);

  void checkAddQuota(Long taskId, int inc);

  TaskRemark checkAndFind(Long id);

  List<TaskRemarkSummary> findSummaryByTaskId(Long targetId);

  int getRemarkNum(Long id);

}
