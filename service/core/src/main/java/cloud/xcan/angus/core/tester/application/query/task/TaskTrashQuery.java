package cloud.xcan.angus.core.tester.application.query.task;

import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrash;

public interface TaskTrashQuery {

  Long count(Long projectId);

  TaskTrash findMyTrashForBiz(Long id, String biz);

}
