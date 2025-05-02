package cloud.xcan.angus.core.tester.infra.remoting.validator;

import static cloud.xcan.angus.api.commonlink.CtrlConstant.OPEN2P_DISCOVER_URI_PREFIX;
import static cloud.xcan.angus.remoting.common.node.ServerDiscovery.DISCOVERY_PING_ENDPOINT_SUFFIX;
import static cloud.xcan.angus.spec.SpecConstant.APP_NOT_READY;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.node.info.NodeInfoRepo;
import cloud.xcan.angus.remoting.common.MessageService;
import cloud.xcan.angus.remoting.common.spi.BindValidator;
import cloud.xcan.angus.spec.http.HttpSender;
import cloud.xcan.angus.spec.http.HttpSender.Request.Builder;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.http.HttpUrlConnectionSender;
import cloud.xcan.angus.spec.principal.Principal;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AngusBindValidator implements BindValidator {

  private NodeInfoRepo nodeInfoRepo = null;

  private ApplicationInfo applicationInfo = null;

  private ObjectMapper objectMapper = null;

  @Override
  public String validate(MessageService service, String deviceId, String tenantId, String userId,
      String accessToken) {
    log.info("Bind exchange server validation, deviceId={}, tenantId={}", deviceId, tenantId);

    if (isEmpty(deviceId)) {
      return "deviceId is missing";
    }
    if (isEmpty(tenantId)) {
      return "tenantId is missing";
    }
    if (isEmpty(accessToken)) {
      return "accessToken is missing";
    }

    if (!ApplicationInfo.APP_READY) {
      return APP_NOT_READY;
    }

    try {
      Principal principal = getPrincipal(accessToken);
      if (!Objects.equals(String.valueOf(principal.getTenantId()), tenantId)) {
        return "Inconsistent registered tenant " + tenantId;
      }
    } catch (Exception e) {
      log.error("Bind exchange server validation failure, cause: ", e);
      return e.getMessage();
    }

    NodeInfoRepo nodeInfoRepo = getNodeInfoRepo();
    if (isNull(nodeInfoRepo)) {
      return "Node repo not initialized";
    }
    NodeInfo nodeInfo = nodeInfoRepo.findById(Long.valueOf(deviceId)).orElse(null);
    if (isNull(nodeInfo)) {
      return "Device " + deviceId + " not found";
    }

    if (!Objects.equals(tenantId, String.valueOf(nodeInfo.getTenantId()))) {
      return "Inconsistent tenantId and deviceId, tenantId=" + tenantId + ", deviceId=" + deviceId;
    }
    return null /*Success*/;
  }

  public Principal getPrincipal(String accessToken) {
    String fullDiscoveryUrl = "http://" + getApplicationInfo().getInstanceId()
        + OPEN2P_DISCOVER_URI_PREFIX + DISCOVERY_PING_ENDPOINT_SUFFIX;
    HttpSender sender = new HttpUrlConnectionSender(Duration.ofSeconds(3), Duration.ofSeconds(5));
    Builder builder = sender.get(fullDiscoveryUrl).withAuthentication("Bearer", accessToken);
    Response response;
    try {
      response = builder.send();
      if (response.isSuccessful()) {
        return getObjectMapper().readValue(response.body(), Principal.class);
      }
    } catch (Throwable throwable) {
      throw new IllegalArgumentException(String.format("Invalid accessToken value: %s, cause: %s",
          accessToken, throwable.getMessage()));
    }
    throw new IllegalArgumentException(String.format("Invalid accessToken value: %s", accessToken));
  }

  public ObjectMapper getObjectMapper() {
    if (nonNull(objectMapper)) {
      return objectMapper;
    }
    objectMapper = SpringContextHolder.getBean(ObjectMapper.class);
    return objectMapper;
  }

  public ApplicationInfo getApplicationInfo() {
    if (nonNull(applicationInfo)) {
      return applicationInfo;
    }
    applicationInfo = SpringContextHolder.getBean(ApplicationInfo.class);
    return applicationInfo;
  }

  public NodeInfoRepo getNodeInfoRepo() {
    if (nonNull(nodeInfoRepo)) {
      return nodeInfoRepo;
    }
    nodeInfoRepo = SpringContextHolder.getBean(NodeInfoRepo.class);
    return nodeInfoRepo;
  }
}
