package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.api.commonlink.PricingConstant.PRICING_RESCODE_BANDWIDTH_PREFIX;
import static cloud.xcan.angus.api.commonlink.PricingConstant.PRICING_RESCODE_CPU_PREFIX;
import static cloud.xcan.angus.api.commonlink.PricingConstant.PRICING_RESCODE_MEMORY_PREFIX;
import static cloud.xcan.angus.api.commonlink.PricingConstant.PRICING_RESCODE_SYSTEMDISK_PREFIX;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_PURCHASE_EXCEPTION;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.NODE_PURCHASE_EXCEPTION_CODE;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.commonlink.node.NodeSource;
import cloud.xcan.angus.api.commonlink.order.OrderSpecsData;
import cloud.xcan.angus.api.commonlink.pricing.PricingTemplateSpecPriceCalc;
import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.api.expense.order.vo.OrderDetailVo;
import cloud.xcan.angus.api.obf.Str0;
import cloud.xcan.angus.api.pojo.Pair;
import cloud.xcan.angus.api.pojo.node.NodeSpecData;
import cloud.xcan.angus.core.biz.SystemAssert;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.utils.crypto.AESUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class NodeConverter {

  public static EventContent buildPurchaseExceptionEvent(Exception exception,
      Long orderId, Long tenantId, NodeSpecData nodeSpec) {
    Principal principal = PrincipalContext.get();
    EventContent exceptions = EventContent.newBuilder()
        .clientId(principal.getClientId())
        .serviceCode(principal.getServiceCode())
        .serviceName(principal.getServiceName())
        .instanceId(principal.getInstanceId())
        .requestId(principal.getRequestId())
        .method(principal.getMethod())
        .uri(principal.getUri())
        .tenantId(tenantId)
        //.tenantName(principal.getTenantName())
        //.userId(principal.getUserId())
        //.fullName(principal.getUserFullName())
        .level(ExceptionLevel.URGENT)
        .cause(exception.getMessage())
        .ext(Map.of("orderId", orderId, "nodeSpec", nodeSpec))
        .build();
    exceptions.setType(EventType.SYSTEM.getValue());
    exceptions.setCode(NODE_PURCHASE_EXCEPTION_CODE);
    exceptions.setEKey(NODE_PURCHASE_EXCEPTION_CODE);
    exceptions.setDescription(MessageHolder.message(NODE_PURCHASE_EXCEPTION));
    return exceptions;
  }

  public static List<Node> initPurchaseNodeInfo(Long orderId, String hostname,
      LocalDateTime orderExpiredDate, Long tenantId, NodeSpecData specData, String regionId,
      List<String> instanceIds) {
    List<Node> nodes = new ArrayList<>();
    for (String instanceId : instanceIds) {
      Node node = new Node()
          .setInstanceId(instanceId)
          .setRegionId(regionId)
          .setOrderId(orderId);
      node.setTenantId(tenantId);
      node.setInstanceExpiredDate(orderExpiredDate);
      node.setSpec(specData)
          .setName(hostname)
          .setInstanceName(null)
          .setDomain(null)
          .setUsername("root")
          .setPassword(encryptHostPassword(TesterConstant.NODE_PASSWORD_SALT + orderId))
          .setSshPort(22)
          .setSource(NodeSource.ONLINE_BUY)
          .setRoles(new LinkedHashSet<>())
          .setFree(false)
          .setEnabled(true)
          .setDeleted(false)
          .setExpired(false)
          .setInstallAgent(null/*False will ignore automatic agent installation*/)
          .setSync(false)
          .setCreatedBy(-1L)
          .setCreatedDate(LocalDateTime.now())
          .setModifiedBy(-1L)
          .setModifiedDate(LocalDateTime.now());
      LinkedHashSet<NodeRole> roles = new LinkedHashSet<>();
      roles.add(NodeRole.EXECUTION);
      node.setRoles(roles);
      nodes.add(node);
    }
    return nodes;
  }

  public static Node copyPurchaseProperties(Node source, Node target) {
    target.setFree(source.getFree())
        .setChargeType(source.getChargeType())
        .setCreatedBy(source.getCreatedBy())
        .setModifiedBy(source.getModifiedBy());
    target.setTenantId(source.getTenantId());
    return target;
  }

  public static NodeSpecData assemblerNodeSpecData(OrderDetailVo orderDetailVo) {
    NodeSpecData nodeSpec = new NodeSpecData();

    // Find CPU&Memory Spec
    OrderSpecsData orderNodeSpecs = orderDetailVo.getGoodsSpecResources().stream()
        .filter(OrderSpecsData::isNodeSpecs).findFirst().orElse(null);
    SystemAssert.assertNotNull(orderNodeSpecs,
        String.format("Node specification not found in the order[%s]", orderDetailVo.getId()));
    PricingTemplateSpecPriceCalc specPriceCalc = orderDetailVo.getGoodsSpecQuotas()
        .findSpecById(orderNodeSpecs.getId());
    SystemAssert.assertNotNull(specPriceCalc,
        String.format("Node specification[%s] purchase quota not found in the order[%s]",
            orderNodeSpecs.getId(), orderDetailVo.getId()));
    nodeSpec.setNodeNum(specPriceCalc.getSpecNum());
    nodeSpec.setCpu(specPriceCalc.getResourceQuotas().get(orderNodeSpecs
        .findResourceByCodePrefix(PRICING_RESCODE_CPU_PREFIX).getId()).intValue());
    nodeSpec.setMemory(specPriceCalc.getResourceQuotas().get(orderNodeSpecs
        .findResourceByCodePrefix(PRICING_RESCODE_MEMORY_PREFIX).getId()).intValue());

    // Find SysDisk Spec
    OrderSpecsData orderSysDiskSpecs = orderDetailVo.getGoodsSpecResources().stream()
        .filter(x -> nonNull(x.findResourceByCodePrefix(PRICING_RESCODE_SYSTEMDISK_PREFIX)))
        .findFirst().orElse(null);
    SystemAssert.assertNotNull(orderSysDiskSpecs,
        String.format("Node specification[%s] purchase quota not found in the order[%s]",
            PRICING_RESCODE_SYSTEMDISK_PREFIX, orderDetailVo.getId()));
    specPriceCalc = orderDetailVo.getGoodsSpecQuotas().findSpecById(orderSysDiskSpecs.getId());
    SystemAssert.assertNotNull(specPriceCalc,
        String.format("Node specification[%s] purchase quota not found in the order[%s]",
            orderSysDiskSpecs.getId(), orderDetailVo.getId()));
    nodeSpec.setSysDisk(specPriceCalc.getResourceQuotas()
        .get(orderSysDiskSpecs.findResourceByCodePrefix(PRICING_RESCODE_SYSTEMDISK_PREFIX).getId())
        .intValue());

    // Find Network Spec
    OrderSpecsData orderNetworkSpecs = orderDetailVo.getGoodsSpecResources().stream()
        .filter(x -> nonNull(x.findResourceByCodePrefix(PRICING_RESCODE_BANDWIDTH_PREFIX)))
        .findFirst().orElse(null);
    SystemAssert.assertNotNull(orderNetworkSpecs,
        String.format("Node specification[%s] purchase quota not found in the order[%s]",
            PRICING_RESCODE_BANDWIDTH_PREFIX, orderDetailVo.getId()));
    specPriceCalc = orderDetailVo.getGoodsSpecQuotas().findSpecById(orderNetworkSpecs.getId());
    SystemAssert.assertNotNull(specPriceCalc,
        String.format("Node specification[%s] purchase quota not found in the order[%s]",
            orderNetworkSpecs.getId(), orderDetailVo.getId()));
    nodeSpec.setNetwork(specPriceCalc.getResourceQuotas()
        .get(orderNetworkSpecs.findResourceByCodePrefix(PRICING_RESCODE_BANDWIDTH_PREFIX).getId())
        .intValue());
    return nodeSpec;
  }

  public static String encryptHostPassword(String content) {
    if (isEmpty(content)) {
      return null;
    }
    Pair<String, String> passwordAndContent = Pair.of(new Str0(
        new long[]{0x3CB217EECD9BD24AL, 0xE23D70D74CFC70F9L, 0xB75B7B5B72A459F3L,
            0x673BBB20789A3226L}).toString() /* => "BBQQG8HZNK2MQKNAXQ7U" */, content);
    return AESUtils.encrypt(passwordAndContent);
  }

  public static String decryptHostPassword(String encrypted) {
    if (isEmpty(encrypted)) {
      return null;
    }
    Pair<String, String> passwordAndEncrypted = Pair.of(new Str0(
        new long[]{0xC87F90550125886AL, 0x66F4FA5695A128FAL, 0xD220766D71E67163L,
            0x5DADE0FFCCCB3541L}).toString() /* => "BBQQG8HZNK2MQKNAXQ7U" */, encrypted);
    return AESUtils.decrypt(passwordAndEncrypted);
  }
}
