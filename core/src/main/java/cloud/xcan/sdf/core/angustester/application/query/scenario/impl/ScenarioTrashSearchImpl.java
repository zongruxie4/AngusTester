package cloud.xcan.sdf.core.angustester.application.query.scenario.impl;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioTrashSearch;
import cloud.xcan.sdf.core.angustester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.sdf.core.angustester.domain.scenario.trash.ScenarioTrashSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ScenarioTrashSearchImpl implements ScenarioTrashSearch {

  @Resource
  private ScenarioTrashSearchRepo scenarioTrashSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Page<ScenarioTrash> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ScenarioTrash> clz, String... matches) {
    return new BizTemplate<Page<ScenarioTrash>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<ScenarioTrash> process() {
        Page<ScenarioTrash> page = scenarioTrashSearchRepo.find(criteria, pageable, clz, matches);
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
