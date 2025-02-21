package cloud.xcan.sdf.core.angustester.application.query.data;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VariableSearch {

  Page<Variable> search(Set<SearchCriteria> criteria, Pageable pageable, Class<Variable> clz,
      String... matches);

}




