package cloud.xcan.sdf.core.angustester.application.query.analysis.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.analysis.AnalysisSearch;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class AnalysisSearchImpl implements AnalysisSearch {

  @Resource
  private AnalysisSearchRepo analysisSearchRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public Page<Analysis> search(Set<SearchCriteria> criterias, PageRequest pageable,
      Class<Analysis> clz, String... matches) {
    return new BizTemplate<Page<Analysis>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<Analysis> process() {
        return analysisSearchRepo.find(criterias, pageable, clz, matches);
      }
    }.execute();
  }
}
