package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioTrashSearch;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrashSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
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
