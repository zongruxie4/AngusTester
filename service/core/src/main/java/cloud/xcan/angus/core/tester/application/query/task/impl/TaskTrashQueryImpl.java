package cloud.xcan.angus.core.tester.application.query.task.impl;

import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.core.tester.application.query.task.TaskTrashQuery;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrash;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrashRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;

@Biz
public class TaskTrashQueryImpl implements TaskTrashQuery {

  @Resource
  private TaskTrashRepo taskTrashRepo;

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Long process() {
        return isNull(projectId) ? taskTrashRepo.count()
            : taskTrashRepo.countByProjectId(projectId);
      }
    }.execute();
  }

  @Override
  public TaskTrash findMyTrashForBiz(Long id, String biz) {
    TaskTrash trashDb = taskTrashRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "TaskTrash"));
    if (!isAdmin() && ObjectUtils.notEqual(trashDb.getDeletedBy(), getUserId())) {
      if ("BACK".equals(biz)) {
        throw BizException.of(TRASH_NO_BACK_PERMISSION_CODE, TRASH_NO_BACK_PERMISSION);
      } else if ("CLEAR".equals(biz)) {
        throw BizException.of(TRASH_NO_CLEAR_PERMISSION_CODE, TRASH_NO_CLEAR_PERMISSION);
      }
    }
    return trashDb;
  }
}
