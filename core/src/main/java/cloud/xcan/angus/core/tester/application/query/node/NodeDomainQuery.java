package cloud.xcan.angus.core.tester.application.query.node;

import cloud.xcan.angus.core.tester.domain.node.domain.NodeDomain;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface NodeDomainQuery {

  NodeDomain find(Long id);

  Page<NodeDomain> find(Specification<NodeDomain> spec, Pageable pageable);

  void setDnsNum(List<NodeDomain> domains);

  NodeDomain checkAndFind(Long id);

  NodeDomain checkAndFind(String name);

  void checkAddNameExists(String name);

  void checkUpdateNameExists(Long id, String name);

}




