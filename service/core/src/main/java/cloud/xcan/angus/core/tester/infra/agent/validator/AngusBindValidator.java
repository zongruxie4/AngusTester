package cloud.xcan.angus.core.tester.infra.agent.validator;

import static cloud.xcan.angus.api.commonlink.CtrlConstant.OPEN2P_DISCOVER_URI_PREFIX;
import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.angus.remoting.common.node.ServerDiscovery.DISCOVERY_PING_ENDPOINT_SUFFIX;
import static cloud.xcan.angus.spec.SpecConstant.APP_NOT_READY;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.domain.config.node.NodeRepo;
import cloud.xcan.angus.core.tester.domain.config.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.config.node.info.NodeInfoRepo;
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

    NodeInfoRepo nodeInfoRepo = getBean(NodeInfoRepo.class);
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

    nodeInfoRepo.updateInstalled(deviceId);
    requireNonNull(getBean(NodeRepo.class)).updateInstalled(deviceId);
    return null /*Success*/;
  }

  public Principal getPrincipal(String accessToken) {
    String fullDiscoveryUrl = "http://" + requireNonNull(getBean(ApplicationInfo.class))
        .getInstanceId() + OPEN2P_DISCOVER_URI_PREFIX + DISCOVERY_PING_ENDPOINT_SUFFIX;
    HttpSender sender = new HttpUrlConnectionSender(Duration.ofSeconds(3), Duration.ofSeconds(5));
    Builder builder = sender.get(fullDiscoveryUrl).withAuthentication("Bearer", accessToken);
    Response response;
    try {
      response = builder.send();
      if (response.isSuccessful()) {
        return requireNonNull(getBean(ObjectMapper.class)).readValue(response.body(),
            Principal.class);
      }
    } catch (Throwable throwable) {
      throw new IllegalArgumentException(String.format("Invalid accessToken value: %s, cause: %s",
          accessToken, throwable.getMessage()));
    }
    throw new IllegalArgumentException(String.format("Invalid accessToken value: %s", accessToken));
  }
}
