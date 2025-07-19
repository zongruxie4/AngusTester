package cloud.xcan.angus.core.tester.application.query.project.impl;

import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_BACK_PERMISSION_CODE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.TRASH_NO_CLEAR_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.project.ProjectTrashQuery;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrashRepo;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrashSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ProjectTrashQueryImpl implements ProjectTrashQuery {

  @Resource
  private ProjectTrashRepo projectTrashRepo;

  @Resource
  private ProjectTrashSearchRepo projectTrashSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Long count() {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return projectTrashRepo.count();
      }
    }.execute();
  }

  @Override
  public Page<ProjectTrash> list(GenericSpecification<ProjectTrash> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ProjectTrash>>() {

      @Override
      protected Page<ProjectTrash> process() {
        Page<ProjectTrash> page = fullTextSearch
            ? projectTrashSearchRepo.find(spec.getCriteria(), pageable, ProjectTrash.class, match)
            : projectTrashRepo.findAll(spec, pageable);

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
  public ProjectTrash findMyTrashForBiz(Long id, String biz) {
    ProjectTrash trashDb = projectTrashRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ProjectTrash"));
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
