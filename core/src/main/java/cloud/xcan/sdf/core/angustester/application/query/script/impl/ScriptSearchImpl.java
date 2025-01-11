package cloud.xcan.sdf.core.angustester.application.query.script.impl;

import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findInfoScope;

import cloud.xcan.sdf.api.InfoScope;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptSearch;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfo;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfoSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
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
  public Page<ScriptInfo> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<ScriptInfo> clz, String... matches) {
    return new BizTemplate<Page<ScriptInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<ScriptInfo> process() {
        scriptQuery.safeScenarioQuery(criterias);

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criterias);
        Page<ScriptInfo> pages = scriptInfoSearchRepo.find(criterias, pageable, clz, matches);
        if (pages.isEmpty()) {
          return pages;
        }

        // For AngusCtrl remote query
        InfoScope infoScope = findInfoScope(criterias, InfoScope.DETAIL);
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
