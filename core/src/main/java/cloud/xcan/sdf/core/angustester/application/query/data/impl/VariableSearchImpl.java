package cloud.xcan.sdf.core.angustester.application.query.data.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableSearch;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class VariableSearchImpl implements VariableSearch {

  @Resource
  private VariableSearchRepo variableSearchRepo;

  @Override
  public Page<Variable> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<Variable> clz, String... matches) {
    return new BizTemplate<Page<Variable>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Variable> process() {
        return variableSearchRepo.find(criterias, pageable, clz, matches);
      }
    }.execute();
  }

}




