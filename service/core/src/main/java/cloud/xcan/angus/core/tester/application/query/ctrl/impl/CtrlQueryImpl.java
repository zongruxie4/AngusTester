package cloud.xcan.angus.core.tester.application.query.ctrl.impl;

import static cloud.xcan.angus.api.commonlink.CtrlConstant.BROADCAST_CTRL_CONNECTION_TIMEOUT;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.BROADCAST_CTRL_REQUEST_TIMEOUT_0;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.OPEN2P_DISCOVER_CONN_INFO_ENDPOINT;
import static cloud.xcan.angus.api.commonlink.CtrlConstant.QUERY_MAX_EXEC_NODES;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getApplicationInfo;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isPrivateEdition;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER;
import static cloud.xcan.angus.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getAuthorization;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.enums.NodeRole;
import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.application.query.config.NodeQuery;
import cloud.xcan.angus.core.tester.application.query.ctrl.CtrlQuery;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.core.tester.infra.agent.AgentServerProperties;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remoting.common.node.DiscoveryNodeDto;
import cloud.xcan.angus.remoting.common.node.DiscoveryNodeVo;
import cloud.xcan.angus.remoting.common.node.DiscoveryNodeVo.DiscoveryNode;
import cloud.xcan.angus.remoting.common.router.ChannelRouter;
import cloud.xcan.angus.remoting.common.router.LocalChannelRouterManager;
import cloud.xcan.angus.spec.http.HttpSender;
import cloud.xcan.angus.spec.http.HttpSender.Request.Builder;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.http.HttpUrlConnectionSender;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Implementation of controller query operations for node discovery and connection management.
 *
 * <p>This class provides functionality for discovering controller nodes in distributed
 * environments, managing connections, and handling node election for load balancing.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Controller node discovery and validation</li>
 *   <li>Service instance health checking and filtering</li>
 *   <li>Connection weight calculation for load balancing</li>
 *   <li>Remote controller communication and connection querying</li>
 *   <li>Support for both cloud service and private deployment editions</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Service
@Slf4j
public class CtrlQueryImpl implements CtrlQuery {

  @Resource
  private NodeQuery nodeQuery;
  @Resource
  private DiscoveryClient discoveryClient;
  @Resource
  private ApplicationInfo appInfo;
  @Resource
  private AgentServerProperties serverProperties;
  @Resource
  private ObjectMapper objectMapper;

  /**
   * Discovers available controller nodes for load balancing.
   *
   * <p>This method performs comprehensive node discovery by querying available
   * controller nodes, validating their health status, and calculating connection weights for load
   * balancing purposes.</p>
   *
   * <p>The discovery process includes:
   * <ul>
   *   <li>Retrieving controller nodes from the database</li>
   *   <li>Validating service instance health status</li>
   *   <li>Filtering nodes based on edition type (cloud vs private)</li>
   *   <li>Querying remote connections for weight calculation</li>
   *   <li>Building discovery response with weighted nodes</li>
   * </ul></p>
   *
   * @param dto the discovery request parameters
   * @return discovery result with available nodes and their weights
   */
  @Override
  public DiscoveryNodeVo discovery(DiscoveryNodeDto dto) {
    return new BizTemplate<DiscoveryNodeVo>(false) {

      @Override
      protected DiscoveryNodeVo process() {
        DiscoveryNodeVo discoveryNodeVo = new DiscoveryNodeVo();
        Long tenantId = getOptTenantId();

        // Retrieve controller nodes from database
        List<Node> nodes = nodeQuery.getNodes(null, NodeRole.CONTROLLER,
            true, QUERY_MAX_EXEC_NODES, isPrivateEdition() ? tenantId : OWNER_TENANT_ID);
        if (isEmpty(nodes)) {
          log.error("Fatal: No controller nodes found, edition={}, tenantId={}, dto={}",
              getApplicationInfo().getEditionType(), tenantId, dto);
          discoveryNodeVo.setFailed("Fatal: No controller nodes found");
          return discoveryNodeVo;
        }
        log.info("Discovery controller nodes: {}", JsonUtils.toJson(nodes));

        // Get healthy service instances from discovery client
        List<ServiceInstance> upInstances = discoveryClient.getInstances(appInfo.getArtifactId());
        if (isEmpty(upInstances)) {
          log.error("Fatal: No controller instance in up state, tenantId={}, dto={}", tenantId,
              dto);
          discoveryNodeVo.setFailed("Fatal: No controller instance in up state");
          return discoveryNodeVo;
        }

        // Extract IP addresses of healthy instances
        List<String> upInstanceIps = upInstances.stream().map(ServiceInstance::getHost)
            .toList();
        log.info("Discovery up instance ips: {}", upInstanceIps);
        log.info("Discovery isCloudServiceEdition={}, upInstanceIps.contains(nodes[0].ip)={}",
            isCloudServiceEdition(), nodes.get(0).getIp());

        // Filter valid nodes based on edition type and health status
        List<Node> validNodes = isCloudServiceEdition()
            ? nodes.stream().filter(x ->
            (isNotEmpty(x.getPublicIp()) || isNotEmpty(x.getDomain()) && isNotEmpty(x.getIp())
                && upInstanceIps.contains(x.getIp()))).toList()
            : nodes.stream().filter(x ->
                    isNotEmpty(x.getIp()) && upInstanceIps.contains(x.getIp()))
                .toList();
        if (isEmpty(validNodes)) {
          log.error("Fatal: No valid controller nodes found, tenantId={}, dto={}", tenantId, dto);
          discoveryNodeVo.setFailed("Fatal: No valid controller nodes found");
          return discoveryNodeVo;
        }
        log.info("Discovery valid nodes: {}", JsonUtils.toJson(validNodes));

        // Create IP to node mapping for efficient lookup
        Map<String, Node> ipNodesMap = validNodes.stream()
            .collect(Collectors.toMap(Node::getIp, x -> x));

        // Build discovery nodes with connection weights
        List<DiscoveryNode> discoveryNodes = new ArrayList<>();
        for (ServiceInstance inst : upInstances) {
          if (ipNodesMap.containsKey(inst.getHost())) {
            String remote = "http://" + inst.getInstanceId() + OPEN2P_DISCOVER_CONN_INFO_ENDPOINT;
            List<ChannelRouter> remoteResults = broadcastQueryConnections2RemoteCtrl(remote);
            if (isNull(remoteResults)) { // Controller node unavailable
              continue;
            }
            DiscoveryNode discoveryNode = new DiscoveryNode();
            Node node = ipNodesMap.get(inst.getHost());
            // Determine discovery host (domain > public IP > private IP)
            String discoveryHost = isNotEmpty(node.getDomain())
                ? node.getDomain() : isNotEmpty(node.getPublicIp())
                ? node.getPublicIp() : node.getIp();
            discoveryNode.setHost(discoveryHost + ":" + serverProperties.getServerPort());
            // Calculate weight based on connection count (fewer connections = higher weight)
            if (isEmpty(remoteResults)) {
              discoveryNode.setWeight(1);
            } else {
              discoveryNode.setWeight(1D / remoteResults.size());
            }
            discoveryNodes.add(discoveryNode);
          }
        }
        if (discoveryNodes.isEmpty()) {
          log.error("Fatal: No controller node election successful, tenantId={}, dto={}",
              tenantId, dto);
          discoveryNodeVo.setFailed("Fatal: No controller node election successful");
          return discoveryNodeVo;
        }

        discoveryNodeVo.setNodes(discoveryNodes);
        return discoveryNodeVo;
      }
    }.execute();
  }


  /**
   * Retrieves all active connections from the local channel router manager.
   *
   * <p>This method collects all channel routers from the local channel map,
   * flattening the nested structure into a single list of active connections.</p>
   *
   * @return list of active channel routers, empty list if no connections exist
   */
  @Override
  public List<ChannelRouter> getConnections() {
    return new BizTemplate<List<ChannelRouter>>() {

      @Override
      protected List<ChannelRouter> process() {
        // Flatten nested channel map structure into single list
        return LocalChannelRouterManager.LOCAL_CHANNEL_MAP.values().stream().map(
            ConcurrentHashMap::values).flatMap(Collection::stream).toList();
      }
    }.execute();
  }

  /**
   * Broadcasts connection query to remote controller nodes.
   *
   * <p>This method sends HTTP requests to remote controller nodes to query
   * their current connection information for load balancing calculations.</p>
   *
   * @param remoteUrl the remote controller endpoint URL
   * @return list of channel routers from remote controller, null if unavailable
   */
  private List<ChannelRouter> broadcastQueryConnections2RemoteCtrl(String remoteUrl) {
    try {
      Response response = doHttpGetRequest(remoteUrl);
      if (response.isSuccessful()) {
        List<ChannelRouter> routers = objectMapper.readValue(response.body(),
            new TypeReference<ApiLocaleResult<List<ChannelRouter>>>() {
            }).orElseContentThrow();
        if (isEmpty(routers)) {
          // Safe result, return empty when there is no connections
          return new ArrayList<>();
        }
        return routers;
      } else {
        log.error("Fatal: Failed to query connections for remote url={}, body={}",
            remoteUrl, response.body());
      }
    } catch (Throwable e) {
      log.error("Broadcast query controller connections exception, cause: ", e);
    }
    // Controller node unavailable
    return null;
  }

  /**
   * Performs HTTP GET request to remote endpoints with authentication.
   *
   * <p>This method creates an HTTP sender with configured timeouts and sends
   * GET requests to remote URLs, optionally including bearer token authentication for door API
   * invocations.</p>
   *
   * @param remoteUrl the target URL for the HTTP request
   * @return HTTP response from the remote endpoint
   * @throws Throwable if the request fails
   */
  public static Response doHttpGetRequest(String remoteUrl) throws Throwable {
    // Create HTTP sender with connection and request timeouts
    HttpSender sender = new HttpUrlConnectionSender(
        Duration.ofMillis(BROADCAST_CTRL_CONNECTION_TIMEOUT),
        Duration.ofMillis(BROADCAST_CTRL_REQUEST_TIMEOUT_0));
    Builder builder = sender.get(remoteUrl);
    if (isNotEmpty(getAuthorization())) { // Fix: Door api invoke
      builder.withAuthentication(BEARER, getAuthorization().substring((BEARER + " ").length()));
    }
    return builder.send();
  }
}
