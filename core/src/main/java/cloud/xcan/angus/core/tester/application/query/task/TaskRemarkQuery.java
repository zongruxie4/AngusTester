package cloud.xcan.angus.core.tester.application.query.task;

import cloud.xcan.angus.core.tester.domain.task.remark.TaskRemark;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskRemarkSummary;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author XiaoLong Liu
 */public interface TaskRemarkQuery {

  Page<TaskRemark> list(GenericSpecification<TaskRemark> spec, PageRequest pageable);

  void checkAddQuota(Long taskId, int inc);

  TaskRemark checkAndFind(Long id);

  List<TaskRemarkSummary> findSummaryByTaskId(Long targetId);

  int getRemarkNum(Long id);

}
