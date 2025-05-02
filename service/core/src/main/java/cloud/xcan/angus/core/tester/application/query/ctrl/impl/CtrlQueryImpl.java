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
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.application.query.ctrl.CtrlQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeInfoQuery;
import cloud.xcan.angus.core.tester.domain.node.Node;
import cloud.xcan.angus.core.tester.infra.remoting.RemotingServerProperties;
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
import cloud.xcan.angus.spec.utils.ObjectUtils;
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

@Biz
@Slf4j
public class CtrlQueryImpl implements CtrlQuery {

  @Resource
  private NodeInfoQuery nodeInfoQuery;

  @Resource
  private DiscoveryClient discoveryClient;

  @Resource
  private ApplicationInfo appInfo;

  @Resource
  private RemotingServerProperties serverProperties;

  @Resource
  private ObjectMapper objectMapper;

  /**
   * The smaller the number of connections, the higher the weight.
   */
  @Override
  public DiscoveryNodeVo discovery(DiscoveryNodeDto dto) {
    return new BizTemplate<DiscoveryNodeVo>() {

      @Override
      protected DiscoveryNodeVo process() {
        DiscoveryNodeVo discoveryNodeVo = new DiscoveryNodeVo();
        Long tenantId = getOptTenantId();

        List<Node> nodes = nodeInfoQuery.getNodes(null, NodeRole.CONTROLLER,
            true, QUERY_MAX_EXEC_NODES, isPrivateEdition() ? tenantId : OWNER_TENANT_ID);
        if (isEmpty(nodes)) {
          log.error("Fatal: No controller nodes found, edition={}, tenantId={}, dto={}",
              getApplicationInfo().getEditionType(), tenantId, dto);
          discoveryNodeVo.setFailed("Fatal: No controller nodes found");
          return discoveryNodeVo;
        }
        log.info("Discovery controller nodes: {}", JsonUtils.toJson(nodes));

        List<ServiceInstance> upInstances = discoveryClient.getInstances(appInfo.getArtifactId());
        if (isEmpty(upInstances)) {
          log.error("Fatal: No controller instance in up state, tenantId={}, dto={}", tenantId,
              dto);
          discoveryNodeVo.setFailed("Fatal: No controller instance in up state");
          return discoveryNodeVo;
        }

        List<String> upInstanceIps = upInstances.stream().map(ServiceInstance::getHost)
            .collect(Collectors.toList());
        log.info("Discovery up instance ips: {}", upInstanceIps);
        log.info("Discovery isCloudServiceEdition={}, upInstanceIps.contains(nodes[0].ip)={}",
            isCloudServiceEdition(), nodes.get(0).getIp());
        List<Node> validNodes = isCloudServiceEdition()
            ? nodes.stream().filter(x ->
            (isNotEmpty(x.getPublicIp()) || isNotEmpty(x.getDomain()) && isNotEmpty(x.getIp())
                && upInstanceIps.contains(x.getIp()))).collect(Collectors.toList())
            : nodes.stream().filter(x ->
                isNotEmpty(x.getIp()) && upInstanceIps.contains(x.getIp()))
                .collect(Collectors.toList());
        if (isEmpty(validNodes)) {
          log.error("Fatal: No valid controller nodes found, tenantId={}, dto={}", tenantId, dto);
          discoveryNodeVo.setFailed("Fatal: No valid controller nodes found");
          return discoveryNodeVo;
        }
        log.info("Discovery valid nodes: {}", JsonUtils.toJson(validNodes));

        Map<String, Node> ipNodesMap = validNodes.stream()
            .collect(Collectors.toMap(Node::getIp, x -> x));

        List<DiscoveryNode> discoveryNodes = new ArrayList<>();
        for (ServiceInstance inst : upInstances) {
          if (ipNodesMap.containsKey(inst.getHost())) {
            String remoteStartUrl = "http://" + inst.getInstanceId()
                + OPEN2P_DISCOVER_CONN_INFO_ENDPOINT;
            List<ChannelRouter> remoteResults = broadcastQueryConnections2RemoteCtrl(
                remoteStartUrl);
            if (isNull(remoteResults)) { // Controller node unavailable
              continue;
            }
            DiscoveryNode discoveryNode = new DiscoveryNode();
            Node node = ipNodesMap.get(inst.getHost());
            String discoveryHost = isNotEmpty(node.getDomain())
                ? node.getDomain() : isNotEmpty(node.getPublicIp())
                ? node.getPublicIp() : node.getIp();
            discoveryNode.setHost(discoveryHost + ":" + serverProperties.getServerPort());
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
   * @return If a empty list indicates that the service no connections.
   */
  @Override
  public List<ChannelRouter> getConnections() {
    return new BizTemplate<List<ChannelRouter>>() {

      @Override
      protected List<ChannelRouter> process() {
        return LocalChannelRouterManager.LOCAL_CHANNEL_MAP.values().stream().map(
            ConcurrentHashMap::values).flatMap(Collection::stream).collect(Collectors.toList());
      }
    }.execute();
  }

  private List<ChannelRouter> broadcastQueryConnections2RemoteCtrl(String remoteUrl) {
    try {
      Response response = doHttpGetRequest(remoteUrl);
      if (response.isSuccessful()) {
        List<ChannelRouter> infoVos = objectMapper.readValue(response.body(),
            new TypeReference<ApiLocaleResult<List<ChannelRouter>>>() {
            }).orElseContentThrow();
        if (isEmpty(infoVos)) {
          // Safe result, return empty when there is no connections
          return new ArrayList<>();
        }
        return infoVos;
      }
    } catch (Throwable e) {
      log.error("Broadcast query controller connections exception, cause: ", e);
    }
    // Controller node unavailable
    return null;
  }

  public static Response doHttpGetRequest(String remoteUrl) throws Throwable {
    HttpSender sender = new HttpUrlConnectionSender(
        Duration.ofMillis(BROADCAST_CTRL_CONNECTION_TIMEOUT),
        Duration.ofMillis(BROADCAST_CTRL_REQUEST_TIMEOUT_0));
    Builder builder = sender.get(remoteUrl);
    if (ObjectUtils.isNotEmpty(getAuthorization())) { // Fix: Door api invoke
      builder.withAuthentication(BEARER, getAuthorization().substring((BEARER + " ").length()));
    }
    return builder.send();
  }
}
