package cloud.xcan.sdf.core.angustester.application.query.task;

import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrash;

public interface TaskTrashQuery {

  Long count(Long projectId);

  TaskTrash findMyTrashForBiz(Long id, String biz);

}