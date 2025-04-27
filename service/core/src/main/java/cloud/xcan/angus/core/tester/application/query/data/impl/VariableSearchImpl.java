package cloud.xcan.angus.core.tester.application.query.data.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.data.VariableSearch;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class VariableSearchImpl implements VariableSearch {

  @Resource
  private VariableSearchRepo variableSearchRepo;

  @Override
  public Page<Variable> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<Variable> clz, String... matches) {
    return new BizTemplate<Page<Variable>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Variable> process() {
        return variableSearchRepo.find(criteria, pageable, clz, matches);
      }
    }.execute();
  }

}




