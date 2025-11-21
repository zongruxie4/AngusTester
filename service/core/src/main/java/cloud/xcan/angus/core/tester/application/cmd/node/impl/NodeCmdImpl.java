package cloud.xcan.angus.core.tester.application.cmd.node.impl;

import static cloud.xcan.angus.api.commonlink.TesterConstant.AGENT_STARTED_MESSAGE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.BATCH_INSTANCE_SIZE;
import static cloud.xcan.angus.api.commonlink.node.NodeSource.ONLINE_BUY;
import static cloud.xcan.angus.core.tester.application.converter.NodeConverter.buildPurchaseExceptionEvent;
import static cloud.xcan.angus.core.tester.application.converter.NodeConverter.decryptHostPassword;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_AGENT_IS_INSTALLED;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_AGENT_IS_INSTALLED_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_CLOUD_RES_NOT_AVAILABLE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_CLOUD_RES_NOT_AVAILABLE_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_CONN_ERROR_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_INSTALL_AGENT_FAILED;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_INSTALL_AGENT_FAILED_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_IP_NOT_AVAILABLE_T;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isToUser;
import static cloud.xcan.angus.spec.utils.ObjectUtils.emptySafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.node.AgentInstallCmd;
import cloud.xcan.angus.api.expense.order.OrderInnerRemote;
import cloud.xcan.angus.api.expense.order.vo.OrderDetailVo;
import cloud.xcan.angus.api.pojo.node.NodeSpecData;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.biz.exception.NoRollbackException;
import cloud.xcan.angus.core.event.EventSender;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeCmd;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeRoleCmd;
import cloud.xcan.angus.core.tester.application.converter.NodeConverter;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeQuery;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.core.tester.domain.config.node.NodeRepo;
import cloud.xcan.angus.core.tester.domain.config.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.config.node.role.NodeRole;
import cloud.xcan.angus.core.tester.infra.iaas.EcsClient;
import cloud.xcan.angus.core.tester.infra.iaas.InstanceChargeType;
import cloud.xcan.angus.core.tester.infra.util.SshUtil;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.utils.NetworkUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for node management.
 * <p>
 * Provides methods for adding, updating, deleting, purchasing, renewing, enabling, stopping, restarting, and managing agent installation for nodes.
 * <p>
 * Ensures permission checks, cloud resource management, distributed coordination, and batch operations with transaction management.
 */
@Slf4j
@Biz
public class NodeCmdImpl extends CommCmd<Node, Long> implements NodeCmd {

  @Resource
  private NodeRepo nodeRepo;
  @Resource
  private NodeQuery nodeQuery;
  @Resource
  private NodeRoleCmd nodeRoleCmd;
  @Resource
  private EcsClient ecsClient;
  @Resource
  private NodeInfoQuery nodeInfoQuery;
  @Resource
  private NodeInfoCmd nodeInfoCmd;
  @Resource
  private OrderInnerRemote orderInnerRemote;

  /**
   * Add a batch of nodes.
   * <p>
   * Checks IP uniqueness and node quota before inserting nodes and their roles.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(List<Node> nodes) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // Check the ip is not repeated
        nodeQuery.checkIpNotExisted(nodes);

        // Check the node quota
        nodeQuery.checkNodeQuota(nodes.size());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = batchInsert(nodes, "name");

        addNodeRoles(nodes);
        return idKeys;
      }
    }.execute();
  }

  /**
   * Add a batch of nodes.
   * <p>
   * Checks IP uniqueness and node quota before inserting nodes and their roles.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add0(List<Node> nodes) {
    List<IdKey<Long, Object>> idKeys = batchInsert(nodes, "name");
    addNodeRoles(nodes);
    return idKeys;
  }

  /**
   * Update a batch of nodes.
   * <p>
   * Checks existence, IP uniqueness, and updates node properties and roles.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<Node> nodes) {
    new BizTemplate<Void>() {
      List<Node> nodesDb;

      @Override
      protected void checkParams() {
        // Check and find nodes
        nodesDb = nodeQuery.checkAndFind(
            nodes.stream().map(Node::getId).collect(Collectors.toSet()));

        // Check the ip is not repeated
        nodeQuery.checkUpdateIpNotExisted(nodes);

        // Purchase node update is not allowed
        // NOOP:: nodeQuery.checkNotPurchasedUpdate(nodesDb);
      }

      @Override
      protected Void process() {
        Map<Long, Node> nodesDbMap = nodesDb.stream()
            .collect(Collectors.toMap(Node::getId, x -> x));
        for (Node node : nodes) {
          Node nodeDb = nodesDbMap.get(node.getId());
          if (nodeDb.getSource().isOnlineBuy() && /*Allow operators to modify*/ !isToUser()) {
            nodeDb.setName(nullSafe(node.getName(), nodeDb.getName()));
            nodeDb.setRoles(nullSafe(node.getRoles(), nodeDb.getRoles()));
          } else {
            CoreUtils.copyPropertiesIgnoreNull(node, nodeDb);
          }
          // Allow triggering agent automatic installation after modifying the configuration
          if (Objects.equals(nodeDb.getInstallAgent(), Boolean.FALSE)) {
            nodeDb.setInstallAgent(null);
          }
        }

        replaceNodeRoles(nodes);

        nodeRepo.saveAll(nodesDb);
        return null;
      }
    }.execute();
  }

  /**
   * Rename a node by ID.
   * <p>
   * Checks existence and updates the node name if changed.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void rename(Long id, String name) {
    new BizTemplate<Void>() {
      Node nodeDb;

      @Override
      protected void checkParams() {
        // Check the node exists
        nodeDb = nodeQuery.checkAndFind(id);
      }

      @Override
      protected Void process() {
        if (!nodeDb.getName().equals(name)) {
          nodeRepo.updateNameById(id, name);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Stop a batch of purchased cloud nodes by IDs.
   * <p>
   * Only supports stopping nodes purchased online.
   */
  @Override
  public void stop(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<Node> nodesDb;

      @Override
      protected void checkParams() {
        // Check and find nodes
        nodesDb = nodeQuery.checkAndFind(ids);
      }

      @SneakyThrows
      @Override
      protected Void process() {
        // Only stop of purchased cloud nodes is supported
        Map<String, List<Node>> regionNodeMap = nodesDb.stream()
            .filter(x -> (x.getSource().isOnlineBuy() && !x.getDeleted()))
            .collect(Collectors.groupingBy(Node::getRegionId));
        if (isEmpty(regionNodeMap)) {
          return null;
        }

        for (String regionId : regionNodeMap.keySet()) {
          List<Node> nodes = regionNodeMap.get(regionId);
          List<String> instanceIds = nodes.stream().map(Node::getInstanceId)
              .toList();
          ecsClient.stopInstances(regionId, instanceIds);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Restart a batch of purchased cloud nodes by IDs.
   * <p>
   * Only supports restarting nodes purchased online.
   */
  @Override
  public void restart(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<Node> nodesDb;

      @Override
      protected void checkParams() {
        // Check and find nodes
        nodesDb = nodeQuery.checkAndFind(ids);
      }

      @SneakyThrows
      @Override
      protected Void process() {
        // Only restart of purchased cloud nodes is supported
        Map<String, List<Node>> regionNodeMap = nodesDb.stream()
            .filter(x -> (x.getSource().isOnlineBuy() && !x.getDeleted()))
            .collect(Collectors.groupingBy(Node::getRegionId));
        if (isEmpty(regionNodeMap)) {
          return null;
        }

        for (String regionId : regionNodeMap.keySet()) {
          List<Node> nodes = regionNodeMap.get(regionId);
          List<String> instanceIds = nodes.stream().map(Node::getInstanceId)
              .toList();
          ecsClient.restartInstances(regionId, instanceIds);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Enable a batch of nodes.
   * <p>
   * Checks existence and updates node status.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(List<Node> nodes) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // Check and find nodes
        nodeQuery.checkAndFind(
            nodes.stream().map(Node::getId).collect(Collectors.toSet()));

        // Purchase node update is not allowed
        // if (!PrincipalContext.isToUser()) {
        //  nodeQuery.checkNotPurchasedUpdate(nodesDb);
        // }
      }

      @Override
      protected Void process() {
        batchUpdateOrNotFound0(nodes);
        return null;
      }
    }.execute();
  }

  /**
   * Delete a batch of nodes.
   * <p>
   * Checks existence, permission, and deletes nodes and their info.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(List<Node> nodes) {
    new BizTemplate<Void>() {
      List<Node> nodesDb;
      Set<Long> ids;

      @Override
      protected void checkParams() {
        // Check and find nodes
        ids = nodes.stream().map(Node::getId).collect(Collectors.toSet());
        nodesDb = nodeQuery.checkAndFind(ids);

        // Purchase node update is not allowed
        if (!isToUser()) {
          nodeQuery.checkNotPurchasedUpdate(nodesDb);
        }

        // TODO 正在运行任务节点不允许删除
      }

      @Override
      protected Void process() {
        // Delete AliYun instance
        deleteNodesByOnlineBuy(nodesDb.stream().filter(node -> node.getSource().isOnlineBuy())
            .toList());
        // Delete own node
        nodesDb = nodesDb.stream().filter(node -> node.getSource().isOwnNode())
            .toList();
        nodeRepo.deleteAll(nodesDb);

        // Delete node info in AngusCtrl
        nodeInfoCmd.delete(ids);
        return null;
      }
    }.execute();
  }

  /**
   * Purchase nodes by order ID and tenant ID.
   * <p>
   * Checks order existence, cloud edition, and resource availability before purchasing and adding nodes.
   */
  @DoInFuture("Move purchase logic to IAAS proxy service(Multi-cloud management)")
  @Override
  public void purchase(Long orderId, Long tenantId) {
    new BizTemplate<Void>(false) {
      OrderDetailVo orderDetailVo;

      @Override
      protected void checkParams() {
        nodeQuery.checkOrderPurchasedExisted(orderId);
      }

      @Override
      protected Void process() {
        NodeSpecData nodeSpec = null;
        try {
          Assert.assertTrue(isCloudServiceEdition(),
              "The purchase nodes only applies to the cloud service edition");

          // Check the order exists
          orderDetailVo = orderInnerRemote.detail(String.valueOf(orderId)).orElseContentThrow();

          // NOOP:: Check tenant node quotas, prevent exceeding quota limit after purchase -> Do in order#add()

          // Assemble NodeSpecData
          nodeSpec = NodeConverter.assemblerNodeSpecData(orderDetailVo);

          // Query the region where all nodes can be purchased(Timeout after five minutes)
          String regionId = ecsClient.queryMeetResourceSpecRegion(
              InstanceChargeType.PostPaid, nodeSpec);
          BizAssert.assertNotEmpty(regionId, NODE_CLOUD_RES_NOT_AVAILABLE_CODE,
              NODE_CLOUD_RES_NOT_AVAILABLE);

          // Purchase nodes by order
          // Important:: Alibaba Cloud can purchase up to 100 nodes at a time
          List<Node> nodes = ecsClient.purchaseAndRunInstances(orderId,
              orderDetailVo.getExpiredDate(), tenantId, nodeSpec, regionId);

          // Add purchase nodes
          add0(nodes);
        } catch (Exception e) {
          log.error("Purchase order {} nodes exception, manual confirmation is required, cause: {}",
              orderId, e.getMessage());
          EventSender.CommonQueue.send(buildPurchaseExceptionEvent(e, orderId, tenantId, nodeSpec));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Purchase nodes by node specification and number.
   * <p>
   * Checks order existence, cloud edition, and resource availability before purchasing and adding nodes.
   */
  @Override
  public void purchase(Node node, Long nodeNum) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        Assert.assertTrue(isCloudServiceEdition(),
            "The purchase nodes only applies to the cloud service edition");

        // Check the order exists
        if (isNotEmpty(node.getOrderId())) {
          orderInnerRemote.detail(String.valueOf(node.getOrderId())).orElseContentThrow();
        }
      }

      @Override
      protected Void process() {
        // Query the region where all nodes can be purchased(Timeout after five minutes)
        Boolean availableResource = ecsClient.hasAvailableResource(node.getRegionId(),
            node.getChargeType(), node.getSpec());
        BizAssert.assertTrue(availableResource, NODE_CLOUD_RES_NOT_AVAILABLE_CODE,
            NODE_CLOUD_RES_NOT_AVAILABLE);

        //Purchase nodes
        List<Node> nodes = null;
        try {
          nodes = ecsClient.purchaseAndRunInstances(node.getOrderId(), node.getInstanceExpiredDate()
              , node.getTenantId(), node.getSpec(), node.getRegionId());
        } catch (Exception e) {
          log.error(e.getMessage(), e);
          BizAssert.assertTrue(false, NODE_CLOUD_RES_NOT_AVAILABLE_CODE,
              NODE_CLOUD_RES_NOT_AVAILABLE);
        }

        // Overwrite user submission attributes
        nodes = nodes.stream().map(t -> NodeConverter.copyPurchaseProperties(node, t))
            .toList();

        // Add purchase nodes
        add(nodes);
        return null;
      }
    }.execute();
  }

  /**
   * Renew nodes by order ID, original order ID, and tenant ID.
   * <p>
   * Checks order existence and cloud edition before updating expiration time.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void renew(Long orderId, Long originalOrderId, Long tenantId) {
    new BizTemplate<Void>(false) {
      OrderDetailVo orderDetailVo;

      @Override
      protected void checkParams() {
        Assert.assertTrue(isCloudServiceEdition(),
            "The purchase nodes only applies to the cloud service edition");

        // Check the order exists
        orderDetailVo = orderInnerRemote.detail(String.valueOf(orderId)).orElseContentThrow();

        // NOOP:: Check tenant node quotas, prevent exceeding quota limit after purchase -> Do in order#add()
      }

      @Override
      protected Void process() {
        // Update node expiration time
        nodeRepo.updateNotExpired(tenantId, originalOrderId, orderDetailVo.getExpiredDate());
        return null;
      }
    }.execute();
  }

  /**
   * Synchronize instance information for purchased nodes.
   * <p>
   * Updates local node info with cloud instance data.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void syncInstanceInfo() {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        // Max 100 instances
        Map<String, List<Node>> regionNodeMap = nodeRepo.findPurchuseAndNotSync(BATCH_INSTANCE_SIZE)
            .stream().collect(Collectors.groupingBy(Node::getRegionId));
        if (isEmpty(regionNodeMap)) {
          return null;
        }

        for (String regionId : regionNodeMap.keySet()) {
          List<Node> nodes = regionNodeMap.get(regionId);
          List<String> instanceIds = nodes.stream().map(Node::getInstanceId)
              .toList();
          Map<String, Node> descNodeMap;
          try {
            log.info("Instance info sync to update aliyun instance info, region: {}, nodes: {}",
                regionId, String.join(",", instanceIds.toString()));
            descNodeMap = ecsClient.getInstancesDescribe(regionId, instanceIds);
          } catch (Exception e) {
            log.error(
                "Instance info sync to update aliyun instance info exception, region: {}, nodes: {}, cause: {}",
                regionId, String.join(",", instanceIds.toString()), e.getMessage());
            return null;
          }

          List<Node> updateNodes = new ArrayList<>();
          for (Node node : nodes) {
            Node descNode = descNodeMap.get(node.getInstanceId());
            if (nonNull(descNode)) {
              descNode.setId(node.getId());
              updateNodes.add(descNode);
            }
          }
          if (isNotEmpty(updateNodes)) {
            batchUpdateOrNotFound0(updateNodes);
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Expire and delete AliYun instances for expired nodes.
   * <p>
   * Updates expired nodes, deletes cloud instances, and removes node info.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void expireAndDeleteAliYunInstances() {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        // Update expired nodes
        // Important:: ResponseDelay the release for 60 minutes to prevent it from being released during renewal
        nodeRepo.updateExpired(LocalDateTime.now().plusMinutes(60));

        // Get expiration instance
        List<Node> expiredNodes = nodeRepo.findExpiredByNotDeletedSource(ONLINE_BUY.getValue(),
            BATCH_INSTANCE_SIZE);
        if (isEmpty(expiredNodes)) {
          return null;
        }

        Set<Long> nodeIds = expiredNodes.stream().map(Node::getId).collect(Collectors.toSet());
        nodeInfoCmd.delete(new HashSet<>(nodeIds));

        // Delete AliYun instance
        deleteNodesByOnlineBuy(expiredNodes);
        return null;
      }
    }.execute();
  }

  /**
   * Install agent on a node by ID.
   * <p>
   * Checks installation status, connection, and installs agent via SSH.
   * <p>
   * Throws exception if installation fails.
   */
  @Transactional(noRollbackFor = NoRollbackException.class)
  @Override
  public AgentInstallCmd agentInstall(Long id) {
    return new BizTemplate<AgentInstallCmd>() {
      Node nodeDb;

      @Override
      protected void checkParams() {
        nodeDb = nodeQuery.checkAndFind(id);

        // Check the for duplicate installations
        BizAssert.assertTrue(isNull(nodeDb.getInstallAgent())
                || !nodeDb.getInstallAgent(), NODE_AGENT_IS_INSTALLED_CODE,
            NODE_AGENT_IS_INSTALLED);

        // Check the node connection configuration
        testConnectionNodeConfig(emptySafe(nodeDb.getPublicIp(), nodeDb.getIp()),
            nodeDb.getSshPort(), nodeDb.getUsername(), decryptHostPassword(nodeDb.getPassword()));
      }

      @Override
      protected AgentInstallCmd process() {
        syncAgentInstallStatus(nodeDb);

        AgentInstallCmd installCmd = nodeInfoCmd.agentInstallCmd(nodeDb.getId());

        SshUtil ssh = new SshUtil(emptySafe(nodeDb.getPublicIp(), nodeDb.getIp()),
            nodeDb.getSshPort(), nodeDb.getUsername(), decryptHostPassword(nodeDb.getPassword()));
        boolean installed = false;
        String resultMessage = "Unsupported OS";
        try {
          // SSH will be installed to the user's home directory
          String testLinuxOrMacOs = ssh.run("uname -s");
          if ("Linux".equalsIgnoreCase(testLinuxOrMacOs)) {
            String result = runLinuxAgentInstallCmd(installCmd, ssh, nodeDb);
            if (isNotEmpty(result) && result.indexOf(AGENT_STARTED_MESSAGE) > 0) {
              installed = true;
            } else {
              resultMessage = isNotEmpty(result) ? result : resultMessage;
            }
          }
        } catch (Exception e) {
          log.warn("Install node {} agent on linux exception:", nodeDb.getId(), e);
        }

        if (!installed) {
          try {
            String testWindowsOs = ssh.run("systeminfo | findstr Windows");
            if (StringUtils.isNotBlank(testWindowsOs)) {
              String result = ssh.run(installCmd.getWindowsOnlineInstallCmd());
              log.info("Install node {} agent on windows result:{}", nodeDb.getId(), result);
              if (isNotEmpty(result) && result.indexOf(AGENT_STARTED_MESSAGE) > 0) {
                installed = true;
              } else {
                resultMessage = isNotEmpty(result) ? result : resultMessage;
              }
            }
          } catch (Exception e) {
            log.warn("Install node {} agent on windows exception:", nodeDb.getId(), e);
          }
        }

        if (!installed) {
          // Prompt for manual installation
          throw BizException.of(NODE_INSTALL_AGENT_FAILED_CODE,
              MessageHolder.message(NODE_INSTALL_AGENT_FAILED) + " : " + resultMessage, null,
              installCmd);
        }
        nodeDb.setInstallAgent(true);
        nodeRepo.save(nodeDb);
        return installCmd;
      }
    }.execute();
  }

  /**
   * Restart agent on a node by ID.
   * <p>
   * Checks connection and restarts agent via SSH.
   */
  @Override
  public void agentRestart(Long id) {
    new BizTemplate<Void>() {
      Node nodeDb;

      @Override
      protected void checkParams() {
        nodeDb = nodeQuery.checkAndFind(id);
        // Check the node connection configuration
        testConnectionNodeConfig(emptySafe(nodeDb.getPublicIp(), nodeDb.getIp()),
            nodeDb.getSshPort(), nodeDb.getUsername(), decryptHostPassword(nodeDb.getPassword()));
      }

      @Override
      protected Void process() {
        try {
          SshUtil ssh = new SshUtil(emptySafe(nodeDb.getPublicIp(), nodeDb.getIp()),
              nodeDb.getSshPort(), nodeDb.getUsername(), decryptHostPassword(nodeDb.getPassword()));
          String testLinuxOrMacOs = ssh.run("uname -s");
          if ("Linux".equalsIgnoreCase(testLinuxOrMacOs)) {
            runLinuxAgentRestartCmd(ssh, nodeDb);
          } else {
            throw ProtocolException.of("Unsupported OS: " + testLinuxOrMacOs);
          }
        } catch (Exception e) {
          throw ProtocolException.of(e.getMessage(), e);
        }
        return null;
      }
    }.execute();
  }

  // TODO Test ...
  /**
   * Automatically install agent on all uninstalled nodes.
   * <p>
   * Checks connection and installs agent via SSH for each node.
   */
  @Override
  public void agentAutoInstall() {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        List<Node> uninstallAgentNodes = nodeRepo.findUninstallAgentNodes(BATCH_INSTANCE_SIZE);
        if (isEmpty(uninstallAgentNodes)) {
          return null;
        }

        for (Node node : uninstallAgentNodes) {
          boolean installed = false;
          if (testConnectionNodeConfig0(emptySafe(node.getPublicIp(), node.getIp()),
              node.getSshPort(), node.getUsername(), decryptHostPassword(node.getPassword()))) {
            AgentInstallCmd installCmd = nodeInfoCmd.agentInstallCmd(node.getId());
            SshUtil ssh = new SshUtil(emptySafe(node.getPublicIp(), node.getIp()),
                node.getSshPort(), node.getUsername(), decryptHostPassword(node.getPassword()));
            try {
              String result = runLinuxAgentInstallCmd(installCmd, ssh, node);
              if (isNotEmpty(result) && result.indexOf("AngusAgent started") > 0) {
                installed = true;
              }
            } catch (Exception e) {
              log.warn("Install node {} agent on linux exception:", node.getId(), e);
            }
            node.setInstallAgent(installed);
            nodeRepo.save(node);
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Test SSH connection configuration for a node.
   * <p>
   * Checks network and SSH availability.
   */
  @Override
  public void testConnectionNodeConfig(String ip, Integer sshPort, String username,
      String password) {
    boolean result = NetworkUtils.ping(ip);
    BizAssert.assertTrue(result, NODE_IP_NOT_AVAILABLE_T, new Object[]{ip});
    SshUtil sshUtil = new SshUtil(ip, sshPort, username, password);
    BizAssert.assertTrue(sshUtil.isAvailable(), NODE_CONN_ERROR_T, new Object[]{ip, sshPort});
  }

  /**
   * Test SSH connection configuration for a node (returns boolean).
   * <p>
   * Checks network and SSH availability.
   */
  @Override
  public boolean testConnectionNodeConfig0(String ip, Integer sshPort,
      String username, String password) {
    boolean result = NetworkUtils.ping(ip);
    if (result) {
      SshUtil sshUtil = new SshUtil(ip, sshPort, username, password);
      return sshUtil.isAvailable();
    }
    return false;
  }

  private void addNodeRoles(List<Node> nodes) {
    List<NodeRole> roles = new ArrayList<>();
    for (Node node : nodes) {
      if (isNotEmpty(node.getRoles())) {
        roles.addAll(node.getRoles().stream()
            .map(x -> new NodeRole().setNodeId(node.getId()).setRole(x.getValue())).toList());
      }
    }
    nodeRoleCmd.add0(roles);
  }

  private void replaceNodeRoles(List<Node> nodes) {
    List<NodeRole> roles = new ArrayList<>();
    for (Node node : nodes) {
      if (isNotEmpty(node.getRoles())) {
        roles.addAll(node.getRoles().stream()
            .map(x -> new NodeRole().setNodeId(node.getId()).setRole(x.getValue())).toList());
      }
    }
    nodeRoleCmd.replace0(roles);
  }

  private String runLinuxAgentInstallCmd(AgentInstallCmd cmdVo, SshUtil ssh, Node node)
      throws Exception {
    ssh.run(cmdVo.getLinuxDownloadInstallScriptCmd());
    ssh.run("chmod +x " + cmdVo.getLinuxInstallScriptName());
    String result = ssh.run(cmdVo.getLinuxRunInstallScriptCmd());
    log.info("Install node {} agent on linux result:{}", node.getId(), result);
    return result;
  }

  private void runLinuxAgentRestartCmd(SshUtil ssh, Node node) throws Exception {
    ssh.run("source /etc/profile && sh ${AGENT_HOME}/shutdown-agent.sh");
    String result = ssh.run("source /etc/profile && sh ${AGENT_HOME}/startup-agent.sh");
    log.info("Restart node {} agent on linux result:{}", node.getId(), result);
  }

  private void syncAgentInstallStatus(Node node) {
    try {
      NodeInfo nodeInfo = nodeInfoQuery.detail(node.getId(), node.isFreeNode());
      if (nonNull(nodeInfo) && nonNull(nodeInfo.getAgentInstalled())
          && nodeInfo.getAgentInstalled()) {
        node.setInstallAgent(true);
        nodeRepo.save(node);
        // The agent is installed, possibly manually offline
        throw NoRollbackException.of(NODE_AGENT_IS_INSTALLED_CODE, NODE_AGENT_IS_INSTALLED);
      }
    } catch (Exception e) {
      // 404: Not installed
      if (e instanceof ResourceNotFound) {
        return;
      }
      throw e;
    }
  }

  private void deleteNodesByOnlineBuy(List<Node> nodes) {
    if (isNotEmpty(nodes)) {
      // Delete AliYun instance
      Map<String, List<Node>> regionIdMap = nodes.stream()
          .filter(node -> node.getSource().isOnlineBuy())
          .collect(Collectors.groupingBy(Node::getRegionId));
      List<String> deletedInstanceIds = new ArrayList<>();
      for (String regionId : regionIdMap.keySet()) {
        List<String> instanceIds = regionIdMap.get(regionId).stream().map(Node::getInstanceId)
            .toList();
        try {
          log.info("Delete aliyun instances, region:{}, instanceIds:{}", regionId, instanceIds);
          ecsClient.deleteInstances(regionId, instanceIds);
          deletedInstanceIds.addAll(instanceIds);
        } catch (Exception e) {
          log.error("Delete aliyun instances exception, regionId={}, instanceIds={}, cause={}",
              regionId, String.join(",", instanceIds), e.getMessage());
        }
      }
      // Update deleted nodes
      if (isNotEmpty(deletedInstanceIds)) {
        nodeRepo.deleteByInstanceIdIn(deletedInstanceIds);
      }
    }
  }

  /**
   * Get the repository for nodes.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<Node, Long> getRepository() {
    return this.nodeRepo;
  }
}
