package cloud.xcan.sdf.core.angustester.application.query.scenario.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.converter.ScenarioConverter;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioSearch;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
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
  public Page<Scenario> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<Scenario> clz, String... matches) {
    return new BizTemplate<Page<Scenario>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<Scenario> process() {
        criterias.add(SearchCriteria.equal("deletedFlag", false));

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criterias);
        Page<Scenario> page = scenarioSearchRepo.find(criterias, pageable, clz,
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




