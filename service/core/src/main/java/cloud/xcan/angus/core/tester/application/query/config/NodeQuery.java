package cloud.xcan.angus.core.tester.application.query.config;

import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

public interface NodeQuery {

  Node detail(Long id);

  Long count(Specification<Node> spec);

  Page<Node> list(GenericSpecification<Node> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  List<Node> findByRole(NodeRole nodeRole);

  List<Node> findByFilters(Set<SearchCriteria> filters);

  boolean hasOwnNodes(Long tenantId);

  boolean isTrailNode(Long nodeId);

  Page<Node> getFreeWhenNonNodes(String role);

  Map<Long, Node> findNodeMap(Collection<Long> ids);

  List<Node> getNodes(Set<Long> nodeIds, NodeRole role, Boolean enabled, int size);

  List<Node> getNodes(Set<Long> nodeIds, NodeRole role, Boolean enabled, int size, Long tenantId);

  Node checkAndFind(Long id);

  List<Node> checkAndFind(Collection<Long> ids);

  Node checkRoleAndGetNode(Long id, NodeRole role);

  void checkIpNotExisted(List<Node> nodes);

  void checkNodeQuota(int incNum);

  void checkUpdateIpNotExisted(List<Node> nodes);

  void checkNotPurchasedUpdate(List<Node> nodesDb);

  void checkOrderPurchasedExisted(Long orderId);

  void checkNodeAgentAvailable(Long nodeId);

  Map<Long, Set<NodeRole>> getNodeRoles(List<Long> nodeIds);

  void setNodeRoles(List<Node> nodes);

}




