package cloud.xcan.sdf.core.angustester.application.query.task.impl;

import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.sdf.core.angustester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskTrashQuery;
import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrash;
import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrashRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.exception.BizException;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import javax.annotation.Resource;

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