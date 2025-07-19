package cloud.xcan.angus.core.tester.application.query.task.impl;

import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.task.TaskTrashQuery;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrash;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrashRepo;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrashSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class TaskTrashQueryImpl implements TaskTrashQuery {

  @Resource
  private TaskTrashRepo taskTrashRepo;

  @Resource
  private TaskTrashSearchRepo taskTrashSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {
      @Override
      protected Long process() {
        return isNull(projectId) ? taskTrashRepo.count()
            : taskTrashRepo.countByProjectId(projectId);
      }
    }.execute();
  }

  @Override
  public Page<TaskTrash> list(GenericSpecification<TaskTrash> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<TaskTrash>>() {

      @Override
      protected Page<TaskTrash> process() {
        Page<TaskTrash> page = fullTextSearch
            ? taskTrashSearchRepo.find(spec.getCriteria(), pageable, TaskTrash.class, match)
            : taskTrashRepo.findAll(spec, pageable);

        if (!page.isEmpty()) {
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy", "createdByName",
              "createdByAvatar");
          userManager.setUserNameAndAvatar(page.getContent(), "deletedBy", "deletedByName",
              "deletedByAvatar");
        }
        return page;
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
