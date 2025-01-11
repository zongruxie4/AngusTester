package cloud.xcan.sdf.core.angustester.application.query.node;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.node.Node;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NodeSearch {

  Page<Node> search(Set<SearchCriteria> criterias, Pageable pageable, Class<Node> clz,
      String... matches);

}




