package cloud.xcan.angus.core.tester.application.query.analysis.impl;

import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.analysis.AnalysisSearch;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.analysis.Analysis;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AnalysisSearchImpl implements AnalysisSearch {

  @Resource
  private AnalysisSearchRepo analysisSearchRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public Page<Analysis> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<Analysis> clz, String... matches) {
    return new BizTemplate<Page<Analysis>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<Analysis> process() {
        return analysisSearchRepo.find(criteria, pageable, clz, matches);
      }
    }.execute();
  }
}
