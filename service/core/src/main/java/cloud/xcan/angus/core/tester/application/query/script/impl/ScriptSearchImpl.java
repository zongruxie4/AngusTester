package cloud.xcan.angus.core.tester.application.query.script.impl;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findInfoScope;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptSearch;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfoSearchRepo;
import cloud.xcan.angus.remote.InfoScope;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ScriptSearchImpl implements ScriptSearch {

  @Resource
  private ScriptInfoSearchRepo scriptInfoSearchRepo;

  @Resource
  private ScriptQuery scriptQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Page<ScriptInfo> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ScriptInfo> clz, String... matches) {
    return new BizTemplate<Page<ScriptInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<ScriptInfo> process() {
        scriptQuery.safeScenarioQuery(criteria);

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
        Page<ScriptInfo> pages = scriptInfoSearchRepo.find(criteria, pageable, clz, matches);
        if (pages.isEmpty()) {
          return pages;
        }

        // For AngusCtrl remote query
        InfoScope infoScope = findInfoScope(criteria, InfoScope.DETAIL);
        if (InfoScope.DETAIL.equals(infoScope)) {
          scriptQuery.setScriptInfoTag(pages.getContent());
          scriptQuery.setScriptSourceName(pages.getContent());
          //setScriptCurrentPermission(pages.getContent());
        }
        // scriptQuery.setScriptCurrentPermission(pages.getContent());
        return pages;
      }
    }.execute();
  }

}
