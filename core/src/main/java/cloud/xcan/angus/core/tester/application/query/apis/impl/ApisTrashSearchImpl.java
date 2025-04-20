package cloud.xcan.angus.core.tester.application.query.apis.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTrashSearch;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrashSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ApisTrashSearchImpl implements ApisTrashSearch {

  @Resource
  private ApisTrashSearchRepo apisTrashSearchRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<ApisTrash> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ApisTrash> clz, String... matches) {
    return new BizTemplate<Page<ApisTrash>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<ApisTrash> process() {
        Page<ApisTrash> page = apisTrashSearchRepo.find(criteria, pageable, clz, matches);

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

}
