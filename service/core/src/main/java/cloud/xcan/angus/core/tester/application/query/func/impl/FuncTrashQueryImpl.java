package cloud.xcan.angus.core.tester.application.query.func.impl;

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
import cloud.xcan.angus.core.tester.application.query.func.FuncTrashQuery;
import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrash;
import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrashRepo;
import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrashSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class FuncTrashQueryImpl implements FuncTrashQuery {

  @Resource
  private FuncTrashRepo funcTrashRepo;

  @Resource
  private FuncTrashSearchRepo funcTrashSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? funcTrashRepo.count()
            : funcTrashRepo.countByProjectId(projectId);
      }
    }.execute();
  }

  @Override
  public Page<FuncTrash> list(GenericSpecification<FuncTrash> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<FuncTrash>>() {

      @Override
      protected Page<FuncTrash> process() {
        Page<FuncTrash> page = fullTextSearch
            ? funcTrashSearchRepo.find(spec.getCriteria(), pageable, FuncTrash.class, match)
            : funcTrashRepo.findAll(spec, pageable);
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
  public FuncTrash findMyTrashForBiz(Long id, String biz) {
    FuncTrash trashDb = funcTrashRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "FuncTrash"));
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
