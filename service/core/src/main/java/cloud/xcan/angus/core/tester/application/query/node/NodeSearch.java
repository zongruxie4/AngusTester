package cloud.xcan.angus.core.tester.application.query.node;

import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NodeSearch {

  Page<Node> search(Set<SearchCriteria> criteria, Pageable pageable, Class<Node> clz,
      String... matches);

}




