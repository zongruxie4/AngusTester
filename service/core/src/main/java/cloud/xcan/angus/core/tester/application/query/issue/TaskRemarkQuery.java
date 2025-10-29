package cloud.xcan.angus.core.tester.application.query.issue;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.issue.remark.TaskRemark;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskRemarkSummary;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author XiaoLong Liu
 */
public interface TaskRemarkQuery {

  Page<TaskRemark> list(GenericSpecification<TaskRemark> spec, PageRequest pageable);

  void checkAddQuota(Long taskId, int inc);

  TaskRemark checkAndFind(Long id);

  List<TaskRemarkSummary> findSummaryByTaskId(Long targetId);

  int getRemarkNum(Long id);

}
