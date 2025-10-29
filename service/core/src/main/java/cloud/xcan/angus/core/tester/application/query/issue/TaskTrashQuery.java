package cloud.xcan.angus.core.tester.application.query.issue;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.issue.trash.TaskTrash;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TaskTrashQuery {

  Long count(Long projectId);

  Page<TaskTrash> list(GenericSpecification<TaskTrash> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  TaskTrash findMyTrashForBiz(Long id, String biz);

}
