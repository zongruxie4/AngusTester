package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.api.commonlink.StoreContant.PRICING_RESCODE_BANDWIDTH_PREFIX;
import static cloud.xcan.sdf.api.commonlink.StoreContant.PRICING_RESCODE_CPU_PREFIX;
import static cloud.xcan.sdf.api.commonlink.StoreContant.PRICING_RESCODE_MEMORY_PREFIX;
import static cloud.xcan.sdf.api.commonlink.StoreContant.PRICING_RESCODE_SYSTEMDISK_PREFIX;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_PURCHASE_EXCEPTION;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.NODE_PURCHASE_EXCEPTION_CODE;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.commonlink.TesterConstant;
import cloud.xcan.sdf.api.commonlink.node.NodeSource;
import cloud.xcan.sdf.api.commonlink.order.OrderSpecsData;
import cloud.xcan.sdf.api.commonlink.pricing.PricingTemplateSpecPriceCalc;
import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.api.enums.NodeRole;
import cloud.xcan.sdf.api.expense.order.vo.OrderDetailVo;
import cloud.xcan.sdf.api.obf.Str0;
import cloud.xcan.sdf.api.pojo.Pair;
import cloud.xcan.sdf.api.pojo.node.NodeSpecData;
import cloud.xcan.sdf.core.angustester.domain.node.Node;
import cloud.xcan.sdf.core.biz.SystemAssert;
import cloud.xcan.sdf.core.event.source.EventContent;
import cloud.xcan.sdf.core.pojo.principal.Principal;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import cloud.xcan.sdf.spec.utils.crypto.AESUtils;
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
        //.fullname(principal.getUserFullname())
        .level(ExceptionLevel.URGENT)
        .cause(exception.getMessage())
        .ext(Map.of("orderId", orderId, "nodeSpec", nodeSpec))
        .build();
    exceptions.setType(EventType.SYSTEM);
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
          .setPassd(encryptHostPassd(TesterConstant.NODE_PASSD_SALT + orderId))
          .setSshPort(22)
          .setSource(NodeSource.ONLINE_BUY)
          .setRoles(new LinkedHashSet<>())
          .setFreeFlag(false)
          .setEnabledFlag(true)
          .setDeletedFlag(false)
          .setExpiredFlag(false)
          .setInstallAgentFlag(null/*False will ignore automatic agent installation*/)
          .setSyncFlag(false)
          .setCreatedBy(-1L)
          .setCreatedDate(LocalDateTime.now())
          .setLastModifiedBy(-1L)
          .setLastModifiedDate(LocalDateTime.now());
      LinkedHashSet<NodeRole> roles = new LinkedHashSet<>();
      roles.add(NodeRole.EXECUTION);
      node.setRoles(roles);
      nodes.add(node);
    }
    return nodes;
  }

  public static Node copyPurchaseProperties(Node source, Node target) {
    target.setFreeFlag(source.getFreeFlag())
        .setChargeType(source.getChargeType())
        .setCreatedBy(source.getCreatedBy())
        .setLastModifiedBy(source.getLastModifiedBy());
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

  public static String encryptHostPassd(String content) {
    if (isEmpty(content)) {
      return null;
    }
    Pair<String, String> passdAndContent = Pair.of(new Str0(
        new long[]{0x3CB217EECD9BD24AL, 0xE23D70D74CFC70F9L, 0xB75B7B5B72A459F3L,
            0x673BBB20789A3226L}).toString() /* => "BBQQG8HZNK2MQKNAXQ7U" */, content);
    return AESUtils.encrypt(passdAndContent);
  }

  public static String decryptHostPassd(String encrypted) {
    if (isEmpty(encrypted)) {
      return null;
    }
    Pair<String, String> passdAndEncrypted = Pair.of(new Str0(
        new long[]{0xC87F90550125886AL, 0x66F4FA5695A128FAL, 0xD220766D71E67163L,
            0x5DADE0FFCCCB3541L}).toString() /* => "BBQQG8HZNK2MQKNAXQ7U" */, encrypted);
    return AESUtils.decrypt(passdAndEncrypted);
  }
}
