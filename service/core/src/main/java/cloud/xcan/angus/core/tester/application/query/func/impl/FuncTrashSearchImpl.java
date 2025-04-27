package cloud.xcan.angus.core.tester.application.query.func.impl;


import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.func.FuncTrashSearch;
import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrash;
import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrashSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class FuncTrashSearchImpl implements FuncTrashSearch {

  @Resource
  private FuncTrashSearchRepo funcTrashSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Page<FuncTrash> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<FuncTrash> clz, String... matches) {
    return new BizTemplate<Page<FuncTrash>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<FuncTrash> process() {
        Page<FuncTrash> page = funcTrashSearchRepo.find(criteria, pageable, clz, matches);
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
