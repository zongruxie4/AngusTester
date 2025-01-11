package cloud.xcan.sdf.core.angustester.application.query.apis.impl;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisTrashSearch;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.trash.ApisTrash;
import cloud.xcan.sdf.core.angustester.domain.apis.trash.ApisTrashSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
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
  public Page<ApisTrash> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<ApisTrash> clz, String... matches) {
    return new BizTemplate<Page<ApisTrash>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<ApisTrash> process() {
        Page<ApisTrash> page = apisTrashSearchRepo.find(criterias, pageable, clz, matches);

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
