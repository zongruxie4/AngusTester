package cloud.xcan.sdf.core.angustester.application.query.func.impl;


import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncTrashSearch;
import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrash;
import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrashSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class FuncTrashSearchImpl implements FuncTrashSearch {

  @Resource
  private FuncTrashSearchRepo funcTrashSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Page<FuncTrash> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<FuncTrash> clz, String... matches) {
    return new BizTemplate<Page<FuncTrash>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<FuncTrash> process() {
        Page<FuncTrash> page = funcTrashSearchRepo.find(criterias, pageable, clz, matches);
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
