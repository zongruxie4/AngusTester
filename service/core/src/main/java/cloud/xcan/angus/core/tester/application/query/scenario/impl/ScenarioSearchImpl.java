package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.converter.ScenarioConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioSearch;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ScenarioSearchImpl implements ScenarioSearch {

  @Resource
  private ScenarioSearchRepo scenarioSearchRepo;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<Scenario> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<Scenario> clz, String... matches) {
    return new BizTemplate<Page<Scenario>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<Scenario> process() {
        criteria.add(SearchCriteria.equal("deleted", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
        Page<Scenario> page = scenarioSearchRepo.find(criteria, pageable, clz,
            ScenarioConverter::objectArrToScenario, matches);

        if (page.hasContent()) {
          if (isUserAction()) {
            // Set favourite state
            scenarioQuery.setFavourite(page.getContent());
            // Set follow state
            scenarioQuery.setFollow(page.getContent());
          }
          // Set exec infos
          scenarioQuery.setExecInfo(page.getContent());
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
        }
        return page;
      }
    }.execute();
  }
}




