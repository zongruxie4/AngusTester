package cloud.xcan.angus.core.tester.application.query.data;

import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VariableSearch {

  Page<Variable> search(Set<SearchCriteria> criteria, Pageable pageable, Class<Variable> clz,
      String... matches);

}




