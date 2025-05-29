package cloud.xcan.angus.core.tester.application.query.node.impl;

import static cloud.xcan.angus.core.biz.BizAssert.assertTrue;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_AGENT_NOT_ONLINE_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_AGENT_NOT_ONLINE_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_IP_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_NOT_CONFIGURED_ROLE_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_NOT_CONFIGURED_ROLE_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_NOT_INSTALLED_AGENT_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_NOT_INSTALLED_AGENT_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_PURCHASE_BY_ORDER_REPEATED_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_PURCHASE_BY_ORDER_REPEATED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_PURCHASE_UPDATE_ERROR_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_PURCHASE_UPDATE_ERROR_T;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isPrivateEdition;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isTenantClient;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAwareProcessor;
import cloud.xcan.angus.core.jpa.page.FixedPageImpl;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeQuery;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.domain.node.NodeListRepo;
import cloud.xcan.angus.core.tester.domain.node.NodeRepo;
import cloud.xcan.angus.core.tester.domain.node.role.NodeRoleRepo;
import cloud.xcan.angus.core.utils.PrincipalContextUtils;
import cloud.xcan.angus.model.result.command.SimpleCommandResult;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Biz
@SummaryQueryRegister(name = "Node", table = "node",
    groupByColumns = {"created_date", "source", "enabled", "free", "install_agent"})
public class NodeQueryImpl implements NodeQuery {

  @Resource
  private NodeRepo nodeRepo;

  @Resource
  private NodeListRepo nodeListRepo;

  @Resource
  private NodeRoleRepo nodeRoleRepo;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  @Resource
  private NodeInfoQuery nodeInfoQuery;

  @Transactional
  @Override
  public Node detail(Long id) {
    return new BizTemplate<Node>(false) {

      @Override
      protected Node process() {
        Node node = nodeRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Node"));
        assertResourceNotFound(node.isFreeNode()
            || node.getTenantId().equals(getOptTenantId()), id, "node");
        setNodeRoles(List.of(node));
        return node;
      }
    }.execute();
  }

  @Override
  public Long count(Specification<Node> spec) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return nodeRepo.count(spec);
      }
    }.execute();
  }

  @Override
  public Page<Node> find(GenericSpecification<Node> spec, Pageable pageable) {
    return new BizTemplate<Page<Node>>() {

      @Override
      protected Page<Node> process() {
        if (isTenantClient()) {
          spec.getCriteria().add(SearchCriteria.equal("deleted", false));
        }

        Page<Node> page = null;
        Long tenantId0 = getTenantId(spec);
        if (isPrivateEdition() || hasOwnNodes(tenantId0)) {
          page = nodeListRepo.find(spec.getCriteria(), pageable, Node.class, null);
        } else if (isUserAction()) {
          PrincipalContext.addExtension("isFreeNodes", true);
          page = getFreeWhenNonNodes(findFirstValue(spec.getCriteria(), "role"));
        }

        if (nonNull(page) && page.hasContent()) {
          setNodeRoles(page.getContent());
        }
        return page;
      }
    }.execute();
  }

  @Override
  public List<Node> findByRole(NodeRole nodeRole) {
    return nodeRepo.findByTenantIdAndRole(getOptTenantId(), nodeRole.getValue());
  }

  @Override
  public List<Node> findByFilters(Set<SearchCriteria> filters) {
    return nodeRepo.findAllByFilters(filters);
  }

  @Override
  public boolean hasOwnNodes(Long tenantId) {
    return nodeRepo.countByTenantId(tenantId) > 0;
  }

  @Override
  public boolean isTrailNode(Long nodeId) {
    // Show shared nodes when there are no nodes in the cloud service version
    if (isCloudServiceEdition()) {
      return new TenantAwareProcessor().call(() ->
          nodeRepo.countByIdInAndFree(List.of(nodeId), true) > 0, null);
    }
    return false;
  }

  @Override
  public Page<Node> getFreeWhenNonNodes(String role) {
    // Show shared nodes when there are no nodes in the cloud service version
    if (isCloudServiceEdition()) {
      int count = nodeRepo.countByTenantId(getOptTenantId());
      if (count <= 0) {
        return new TenantAwareProcessor().call(() -> {
          List<Node> nodes = isNull(role) ? nodeRepo.findByTenantIdAndFree(OWNER_TENANT_ID, true)
              : nodeRepo.findByTenantIdAndFreeAndRole(OWNER_TENANT_ID, true, role);
          if (isNotEmpty(nodes)) {
            return new FixedPageImpl<>(nodes);
          }
          return Page.empty();
        }, null);
      }
    }
    return Page.empty();
  }

  @Override
  public Map<Long, Node> findNodeMap(Collection<Long> ids) {
    return nodeRepo.findAllById(ids).stream().collect(Collectors.toMap(Node::getId, x -> x));
  }

  @Override
  public List<Node> getNodes(Set<Long> nodeIds, NodeRole role, Boolean enabled, int size) {
    return getNodes(nodeIds, role, enabled, size, null);
  }

  @Override
  public List<Node> getNodes(Set<Long> nodeIds, NodeRole role, Boolean enabled,
      int size, Long tenantId) {
    Set<SearchCriteria> filters = new HashSet<>();
    if (nonNull(tenantId)) {
      filters.add(SearchCriteria.equal("tenantId", tenantId));
    }
    if (isNotEmpty(nodeIds)) {
      filters.add(SearchCriteria.in("id", nodeIds));
    }
    if (nonNull(enabled)) {
      filters.add(SearchCriteria.equal("enabled", enabled));
    }
    List<Node> nodes = findByFilters(filters);
    if (nonNull(role) && isNotEmpty(nodes)) {
      Map<Long, Set<NodeRole>> nodeRoles = getNodeRoles(nodes.stream().map(Node::getId).toList());
      nodes = nodes.stream()
          .filter(x -> nodeRoles.containsKey(x.getId()) && nodeRoles.get(x.getId()).contains(role))
          .toList();
    }
    return size > 0 ? nodes.subList(0, Math.min(size, nodes.size())) : nodes;
  }

  @Override
  public Node checkAndFind(Long id) {
    return nodeRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Node"));
  }

  @Override
  public List<Node> checkAndFind(Collection<Long> ids) {
    List<Node> nodes = nodeRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(nodes), ids, "Node");
    if (ids.size() != nodes.size()) {
      for (Node node : nodes) {
        assertResourceNotFound(ids.contains(node.getId()), node.getId(), "Node");
      }
    }
    return nodes;
  }

  @Override
  public Node checkRoleAndGetNode(Long id, NodeRole role) {
    Node nodeDb = detail(id); // Multi tenant automatic assembly disabled
    assertTrue(isNotEmpty(nodeDb.getRoles()) && nodeDb.getRoles().contains(role),
        NODE_NOT_CONFIGURED_ROLE_CODE, NODE_NOT_CONFIGURED_ROLE_T, new Object[]{id, role});
    // Verify that the node has the agent installed
    assertTrue(nonNull(nodeDb.getInstallAgent()) && nodeDb.getInstallAgent(),
        NODE_NOT_INSTALLED_AGENT_CODE, NODE_NOT_INSTALLED_AGENT_T, new Object[]{id});
    return nodeDb;
  }

  @Override
  public void checkIpNotExisted(List<Node> nodes) {
    List<Node> existedNodes = nodeRepo
        .findByIpIn(nodes.stream().map(Node::getIp).collect(Collectors.toSet()));
    assertResourceExisted(existedNodes, NODE_IP_EXISTED_T,
        existedNodes.stream().map(Node::getIp).toArray());
  }

  @Override
  public void checkNodeQuota(int incNum) {
    int nodeNum = incNum + nodeRepo.countByTenantId(getOptTenantId());
    settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterNode, null,
        (long) nodeNum);
  }

  @Override
  public void checkUpdateIpNotExisted(List<Node> nodes) {
    // Check and modify the IP duplication, ignore modifying the node itself
    Set<Node> ipNodes = nodes.stream().filter(n -> StringUtils.isNotEmpty(n.getIp()))
        .collect(Collectors.toSet());
    if (isNotEmpty(ipNodes)) {
      List<Node> nodesDb = nodeRepo.findByIpIn(ipNodes.stream().map(Node::getIp)
          .collect(Collectors.toSet()));
      if (isNotEmpty(nodesDb)) {
        for (Node existedNode : nodesDb) {
          for (Node ipNode : ipNodes) {
            assertResourceExisted(!existedNode.getIp().equals(ipNode.getIp()) || existedNode.getId()
                .equals(ipNode.getId()), NODE_IP_EXISTED_T, new Object[]{ipNode.getIp()});
          }
        }
      }
    }
  }

  @Override
  public void checkNotPurchasedUpdate(List<Node> nodesDb) {
    for (Node node : nodesDb) {
      assertTrue(!node.getSource().isOnlineBuy(), NODE_PURCHASE_UPDATE_ERROR_CODE,
          NODE_PURCHASE_UPDATE_ERROR_T, new Object[]{node.getId()});
    }
  }

  @Override
  public void checkOrderPurchasedExisted(Long orderId) {
    int count = nodeRepo.countByOrderId(orderId);
    assertTrue(count < 1, NODE_PURCHASE_BY_ORDER_REPEATED_CODE,
        NODE_PURCHASE_BY_ORDER_REPEATED_T, new Object[]{orderId});
  }

  @Override
  public void checkNodeAgentAvailable(Long nodeId) {
    Map<Long, SimpleCommandResult> resultMap = nodeInfoQuery.agentStatus(
        true, singletonList(nodeId));
    assertTrue(isNotEmpty(resultMap)
            && resultMap.get(nodeId).isSuccess(), NODE_AGENT_NOT_ONLINE_CODE,
        NODE_AGENT_NOT_ONLINE_T, new Object[]{nodeId});
  }

  @Override
  public Map<Long, Set<NodeRole>> getNodeRoles(List<Long> nodeIds) {
    return nodeRoleRepo.findByNodeIdIn(nodeIds).stream()
        .collect(Collectors.groupingBy(
            cloud.xcan.angus.core.tester.domain.node.role.NodeRole::getNodeId,
            Collectors.mapping(x -> NodeRole.valueOf(x.getRole()), Collectors.toSet())));
  }

  @Override
  public void setNodeRoles(List<Node> nodes) {
    List<Long> nodeIds = nodes.stream().map(Node::getId).collect(Collectors.toList());
    Map<Long, Set<NodeRole>> rolesMap = getNodeRoles(nodeIds);
    if (isNotEmpty(rolesMap)) {
      for (Node node0 : nodes) {
        node0.setRoles(rolesMap.get(node0.getId()));
      }
    }
  }

  private Long getTenantId(GenericSpecification<Node> spec) {
    Object tenantId =
        PrincipalContextUtils.isInnerApi() ? findFirstValue(spec.getCriteria(), "tenantId") : null;
    return nonNull(tenantId) ? Long.valueOf(tenantId.toString()) : getOptTenantId();
  }
}
