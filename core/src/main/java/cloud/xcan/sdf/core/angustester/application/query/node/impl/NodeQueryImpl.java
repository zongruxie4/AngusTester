package cloud.xcan.sdf.core.angustester.application.query.node.impl;

import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_AGENT_NOT_ONLINE_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_AGENT_NOT_ONLINE_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_IP_EXISTED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_NOT_CONFIGURED_ROLE_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_NOT_CONFIGURED_ROLE_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_NOT_INSTALLED_AGENT_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_NOT_INSTALLED_AGENT_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_PURCHASE_BY_ORDER_REPEATED_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_PURCHASE_BY_ORDER_REPEATED_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_PURCHASE_UPDATE_ERROR_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_PURCHASE_UPDATE_ERROR_T;
import static cloud.xcan.sdf.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isCloudServiceEdition;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isDoorApi;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isPrivateEdition;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static cloud.xcan.sdf.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.model.result.command.SimpleCommandResult;
import cloud.xcan.sdf.api.angusctrl.node.NodeInfoRemote;
import cloud.xcan.sdf.api.angusctrl.node.dto.NodeAgentStatusQueryDto;
import cloud.xcan.sdf.api.angusctrl.node.vo.NodeInfoDetailVo;
import cloud.xcan.sdf.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.sdf.api.enums.NodeRole;
import cloud.xcan.sdf.api.manager.SettingTenantQuotaManager;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.node.NodeQuery;
import cloud.xcan.sdf.core.angustester.domain.node.Node;
import cloud.xcan.sdf.core.angustester.domain.node.NodeListRepo;
import cloud.xcan.sdf.core.angustester.domain.node.NodeRepo;
import cloud.xcan.sdf.core.angustester.domain.node.role.NodeRoleRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.page.FixedPageImpl;
import cloud.xcan.sdf.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.transaction.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Biz
@SummaryQueryRegister(name = "Node", table = "node",
    groupByColumns = {"created_date", "source", "enabled_flag", "free_flag", "install_agent_flag"})
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
  private NodeInfoRemote nodeInfoRemote;

  @Transactional
  @Override
  public Node detail(Long id) {
    return new BizTemplate<Node>(false) {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Node process() {
        Node node = nodeRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Node"));
        ProtocolAssert.assertResourceNotFound(
            node.isFreeNode() || node.getTenantId().equals(getOptTenantId()), id, "node");

        setNodeRoles(List.of(node));

        if (node.getInstallAgentFlag() == null || !node.getInstallAgentFlag()) {
          try {
            NodeInfoDetailVo vo = nodeInfoRemote.detail(id, node.isFreeNode()).orElseContentThrow();
            if (vo != null && vo.getAgentInstalledFlag()) {
              // The agent is installed, possibly manually offline
              node.setInstallAgentFlag(true);
              nodeRepo.save(node);
            }
          } catch (Exception e) {
            // NOOP
          }
        }
        return node;
      }
    }.execute();
  }

  @Override
  public Long count(Specification<Node> spec) {
    return new BizTemplate<Long>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Node> process() {
        if (PrincipalContext.isTenantClient()) {
          spec.getCriterias().add(SearchCriteria.equal("deletedFlag", false));
        }

        Page<Node> page = null;
        Long tenantId0 = getTenantId(spec);
        if (isPrivateEdition() || hasOwnNodes(tenantId0)) {
          page = nodeListRepo.find(spec.getCriterias(), pageable, Node.class, null);
        } else if (isUserAction()) {
          PrincipalContext.addExtension("isFreeNodes", true);
          page = getFreeWhenNonNodes(findFirstValue(spec.getCriterias(), "role"));
        }

        if (nonNull(page) && page.hasContent()) {
          setNodeRoles(page.getContent());
        }
        return page;
      }
    }.execute();
  }


  @Override
  public boolean hasOwnNodes(Long tenantId) {
    return nodeRepo.countByTenantId(tenantId) > 0;
  }

  @Override
  public Page<Node> getFreeWhenNonNodes(String role) {
    // Show shared nodes when there are no nodes in the cloud service version
    if (isCloudServiceEdition()) {
      int count = nodeRepo.countByTenantId(getOptTenantId());
      if (count <= 0) {
        PrincipalContext.setMultiTenantCtrl(false);
        List<Node> nodes = isNull(role) ? nodeRepo.findByTenantIdAndFreeFlag(OWNER_TENANT_ID, true)
            : nodeRepo.findByTenantIdAndFreeFlagAndRole(OWNER_TENANT_ID, true, role);
        if (isNotEmpty(nodes)) {
          return new FixedPageImpl<>(nodes);
        }
      }
    }
    return Page.empty();
  }

  @Override
  public Map<Long, Node> findNodeMap(Collection<Long> ids) {
    return nodeRepo.findAllById(ids).stream().collect(Collectors.toMap(Node::getId, x -> x));
  }

  @Override
  public Node checkAndFind(Long id) {
    return nodeRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Node"));
  }

  @Override
  public List<Node> checkAndFind(Collection<Long> ids) {
    List<Node> nodes = nodeRepo.findAllById(ids);
    ProtocolAssert.assertResourceNotFound(isNotEmpty(nodes), ids, "Node");
    if (ids.size() != nodes.size()) {
      for (Node node : nodes) {
        ProtocolAssert.assertResourceNotFound(ids.contains(node.getId()), node.getId(), "Node");
      }
    }
    return nodes;
  }

  @Override
  public Node checkRoleAndGetNode(Long id, NodeRole role) {
    Node nodeDb = detail(id); // Multi tenant automatic assembly disabled
    BizAssert.assertTrue(isNotEmpty(nodeDb.getRoles()) && nodeDb.getRoles().contains(role),
        NODE_NOT_CONFIGURED_ROLE_CODE, NODE_NOT_CONFIGURED_ROLE_T, new Object[]{id, role});
    // Verify that the node has the agent installed
    BizAssert.assertTrue(nonNull(nodeDb.getInstallAgentFlag()) && nodeDb.getInstallAgentFlag(),
        NODE_NOT_INSTALLED_AGENT_CODE, NODE_NOT_INSTALLED_AGENT_T, new Object[]{id});
    return nodeDb;
  }

  @Override
  public void checkIpNotExisted(List<Node> nodes) {
    List<Node> existedNodes = nodeRepo
        .findByIpIn(nodes.stream().map(Node::getIp).collect(Collectors.toSet()));
    ProtocolAssert.assertResourceExisted(existedNodes, NODE_IP_EXISTED_T,
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
    if (CollectionUtils.isNotEmpty(ipNodes)) {
      List<Node> nodesDb = nodeRepo.findByIpIn(ipNodes.stream().map(Node::getIp)
          .collect(Collectors.toSet()));
      if (CollectionUtils.isNotEmpty(nodesDb)) {
        for (Node existedNode : nodesDb) {
          for (Node ipNode : ipNodes) {
            ProtocolAssert.assertResourceExisted(
                !existedNode.getIp().equals(ipNode.getIp()) || existedNode.getId()
                    .equals(ipNode.getId()), NODE_IP_EXISTED_T, new Object[]{ipNode.getIp()});
          }
        }
      }
    }
  }

  @Override
  public void checkNotPurchasedUpdate(List<Node> nodesDb) {
    for (Node node : nodesDb) {
      BizAssert.assertTrue(!node.getSource().isOnlineBuy(), NODE_PURCHASE_UPDATE_ERROR_CODE,
          NODE_PURCHASE_UPDATE_ERROR_T, new Object[]{node.getId()});
    }
  }

  @Override
  public void checkOrderPurchasedExisted(Long orderId) {
    int count = nodeRepo.countByOrderId(orderId);
    BizAssert.assertTrue(count < 1, NODE_PURCHASE_BY_ORDER_REPEATED_CODE,
        NODE_PURCHASE_BY_ORDER_REPEATED_T, new Object[]{orderId});
  }

  @Override
  public void checkNodeAgentAvailable(Long nodeId) {
    NodeAgentStatusQueryDto dto = new NodeAgentStatusQueryDto()
        .setNodeIds(Collections.singletonList(nodeId)).setBroadcast(true);
    Map<Long, SimpleCommandResult> agentStatusVos = nodeInfoRemote.agentStatus(dto)
        .orElseContentThrow();
    BizAssert.assertTrue(isNotEmpty(agentStatusVos)
            && agentStatusVos.get(nodeId).isSuccess(), NODE_AGENT_NOT_ONLINE_CODE,
        NODE_AGENT_NOT_ONLINE_T, new Object[]{nodeId});
  }

  @Override
  public void setNodeRoles(List<Node> nodes) {
    List<Long> nodeIds = nodes.stream().map(Node::getId).collect(Collectors.toList());
    Map<Long, Set<NodeRole>> rolesMap = nodeRoleRepo.findByNodeIdIn(nodeIds).stream()
        .collect(Collectors.groupingBy(
            cloud.xcan.sdf.core.angustester.domain.node.role.NodeRole::getNodeId,
            Collectors.mapping(x -> NodeRole.valueOf(x.getRole()), Collectors.toSet())));
    if (isNotEmpty(rolesMap)) {
      for (Node node0 : nodes) {
        node0.setRoles(rolesMap.get(node0.getId()));
      }
    }
  }

  private Long getTenantId(GenericSpecification<Node> spec) {
    Object tenantId = isDoorApi() ? findFirstValue(spec.getCriterias(), "tenantId") : null;
    return nonNull(tenantId) ? Long.valueOf(tenantId.toString()) : getOptTenantId();
  }
}
