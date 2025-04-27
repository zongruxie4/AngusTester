package cloud.xcan.angus.core.tester.application.query.node;

import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface NodeQuery {

  Node detail(Long id);

  Long count(Specification<Node> spec);

  Page<Node> find(GenericSpecification<Node> spec, Pageable pageable);

  List<Node> findByRole(NodeRole nodeRole);

  boolean hasOwnNodes(Long tenantId);

  Page<Node> getFreeWhenNonNodes(String role);

  Map<Long, Node> findNodeMap(Collection<Long> ids);

  Node checkAndFind(Long id);

  List<Node> checkAndFind(Collection<Long> ids);

  Node checkRoleAndGetNode(Long id, NodeRole role);

  void checkIpNotExisted(List<Node> nodes);

  void checkNodeQuota(int incNum);

  void checkUpdateIpNotExisted(List<Node> nodes);

  void checkNotPurchasedUpdate(List<Node> nodesDb);

  void checkOrderPurchasedExisted(Long orderId);

  void checkNodeAgentAvailable(Long nodeId);

  void setNodeRoles(List<Node> nodes);

}




