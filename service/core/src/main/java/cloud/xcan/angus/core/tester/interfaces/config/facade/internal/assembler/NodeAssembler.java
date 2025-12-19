package cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler;

import static cloud.xcan.angus.core.tester.application.converter.NodeConverter.encryptHostPassword;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.NodeInfoAssembler.toOsVo;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.node.NodeSource;
import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.api.pojo.node.NodeSpecData;
import cloud.xcan.angus.api.tester.node.dto.NodeCountFindDto;
import cloud.xcan.angus.api.tester.node.dto.NodeFindDto;
import cloud.xcan.angus.api.tester.node.vo.NodeDetailVo;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.core.tester.domain.config.node.info.NodeInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.infra.iaas.InstanceChargeType;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodePurchaseDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeMockServiceListVo;
import cloud.xcan.angus.remote.dto.EnabledOrDisabledDto;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.utils.DataSizeUtils;
import com.google.common.collect.Sets;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public class NodeAssembler {

  public static Node addDtoToDomain(NodeAddDto dto) {
    return new Node()
        .setName(dto.getName())
        .setIp(dto.getIp())
        .setPublicIp(dto.getPublicIp())
        .setUsername(dto.getUsername())
        .setPassword(isEmpty(dto.getPassword()) ? null : encryptHostPassword(dto.getPassword()))
        .setSshPort(dto.getSshPort())
        .setDomain(stringSafe(dto.getDomain()))
        .setSource(NodeSource.OWN_NODE)
        .setRoles(dto.getRoles())
        .setFree(nullSafe(dto.getFree(), false))
        .setEnabled(true)
        .setInstallAgent(null/*False will ignore automatic agent installation*/)
        .setDeleted(false);
  }

  public static Node updateDtoToDomain(NodeUpdateDto dto) {
    Long userId = Objects.isNull(getUserId()) ? -1L : getUserId();
    Node node = new Node()
        .setId(dto.getId())
        .setName(dto.getName())
        .setIp(dto.getIp())
        .setPublicIp(dto.getPublicIp())
        .setUsername(dto.getUsername())
        .setPassword(isEmpty(dto.getPassword()) ? null : encryptHostPassword(dto.getPassword()))
        .setSshPort(dto.getSshPort())
        .setDomain(stringSafe(dto.getDomain()))
        .setRoles(dto.getRoles());
    node.setModifiedBy(userId);
    node.setModifiedDate(LocalDateTime.now());
    return node;
  }

  public static Node purchaseDtoToDomain(NodePurchaseDto dto) {
    NodeSpecData specData = new NodeSpecData()
        .setNodeNum(dto.getNodeNum())
        .setCpu(dto.getCpu())
        .setMemory(dto.getMemory())
        .setSysDiskCategory(dto.getSysDiskCategory())
        .setSysDisk(dto.getSysDisk())
        .setNetworkChargeType(dto.getBandwidthChargeType())
        .setNetwork(dto.getBandwidth());
    LocalDateTime orderExpiredDate = null;
    if (InstanceChargeType.PrePaid.equals(dto.getChargeType())) {
      orderExpiredDate = LocalDateTime.now().plusMonths(dto.getChargeCycle());
    }
    return new Node()
        .setRegionId(dto.getRegionId())
        .setSpec(specData)
        .setOrderId(dto.getOrderId())
        .setInstanceExpiredDate(orderExpiredDate)
        .setChargeType(dto.getChargeType())
        .setSource(NodeSource.ONLINE_BUY)
        .setRoles(Sets.newLinkedHashSet(List.of(NodeRole.EXECUTION)))
        .setFree(dto.getFree())
        .setEnabled(true)
        .setInstallAgent(null/*False will ignore automatic agent installation*/)
        .setDeleted(false);
  }

  public static Node enabledDtoToDomain(EnabledOrDisabledDto dto) {
    return new Node().setId(dto.getId())
        .setEnabled(dto.getEnabled());
  }

  public static List<Node> deletesDtoToDomain(Collection<Long> ids) {
    return ids.stream().map(i -> new Node().setId(i)).toList();
  }

  public static void completeNodeDetailVo(NodeDetailVo nodeDetailVo, NodeInfo nodeInfo) {
    if (nodeInfo != null) {
      nodeDetailVo.setOnline(nodeInfo.getAgentOnline());
      nodeDetailVo.setOs(toOsVo(nodeInfo.getOs()));
      if (!nodeDetailVo.getSource().isOnlineBuy() && nonNull(nodeInfo.getInfo())) {
        setAgentNodeSpec(nodeDetailVo, nodeInfo);
      }
    }
  }

  public static void setAgentNodeSpec(NodeDetailVo nodeDetailVo, NodeInfo nodeInfo) {
    NodeSpecData nodeSpecData = new NodeSpecData();
    nodeSpecData.setCpu(nodeInfo.getInfo().getCpuNum());
    nodeSpecData.setMemory((int) DataSizeUtils.formatRoundingDataSize(nodeInfo.getInfo()
        .getMemTotal()).toGigabytes());
    nodeSpecData.setSysDisk((int) DataSizeUtils.formatDataSize(nodeInfo.getInfo()
        .getFsTotal()).toGigabytes());
    if (isNotEmpty(nodeInfo.getInfo().getNetworkSpeed())) {
      int bandwidth = Double.valueOf(nodeInfo.getInfo().getNetworkSpeed()).intValue();
      if (bandwidth > 0) {
        nodeSpecData.setNetwork(bandwidth);
      }
    }
    nodeDetailVo.setSpec(nodeSpecData);
  }

  public static NodeDetailVo toDetailVo(Node node, boolean isAdmin) {
    return new NodeDetailVo()
        .setId(node.getId())
        .setName(node.getName())
        .setIp(node.getIp())
        .setPublicIp(node.getPublicIp())
        .setDomain(node.getDomain())
        .setUsername(node.getUsername())
        .setPassword(
            null/*node.getSource().equals(NodeSource.ONLINE_BUY) ? "******" : node.getPassword()*/)
        .setSshPort(node.getSshPort())
        .setSpec(node.getSpec())
        .setSource(node.getSource())
        .setRoles(node.getRoles())
        .setFree(node.getFree())
        .setInstanceExpiredDate(node.getInstanceExpiredDate())
        .setEnabled(node.getEnabled())
        .setInstallAgent(node.getInstallAgent())
        //.setOnline(false) // Default
        .setOrderId(node.getOrderId())
        .setAdmin(isAdmin)
        .setRoles(node.getRoles())
        .setDeleted(node.getDeleted())
        .setTenantId(node.getTenantId())
        .setCreatedBy(node.getCreatedBy())
        .setCreatedDate(LocalDateTime.now())
        .setModifiedBy(node.getModifiedBy())
        .setModifiedDate(node.getModifiedDate());
  }

  public static NodeMockServiceListVo toServiceListVo(MockServiceInfo service) {
    return new NodeMockServiceListVo().setId(service.getId())
        .setProjectId(service.getProjectId())
        .setName(service.getName())
        .setSource(service.getSource())
        .setStatus(service.getServiceStatus())
        .setServicePort(service.getServicePort())
        .setServiceDomainUrl(service.getServiceDomainUrl())
        .setServiceHostUrl(service.getServiceHostUrl())
        .setAuth(service.getAuth())
        .setAssocServices(Objects.nonNull(service.getAssocServiceId()))
        .setTenantId(service.getTenantId())
        .setCreatedBy(service.getCreatedBy())
        .setCreatedDate(service.getCreatedDate())
        .setModifiedBy(service.getModifiedBy())
        .setModifiedDate(service.getModifiedDate());
  }

  public static Specification<Node> getCountSpecification(NodeCountFindDto dto) {
    return (Specification<Node>) (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      Predicate deletedPredicate = criteriaBuilder.equal(root.get("deleted"), dto.getSource());
      predicates.add(deletedPredicate);
      if (nonNull(dto.getTenantId())) {
        Predicate tenantPredicate = criteriaBuilder.equal(root.get("tenantId"), dto.getTenantId());
        predicates.add(tenantPredicate);
      }
      if (nonNull(dto.getEnabled())) {
        Predicate enabledPredicate = criteriaBuilder
            .equal(root.get("enabled"), dto.getEnabled());
        predicates.add(enabledPredicate);
      }
      if (nonNull(dto.getSource())) {
        Predicate sourcePredicate = criteriaBuilder.equal(root.get("source"), dto.getSource());
        predicates.add(sourcePredicate);
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }

  public static GenericSpecification<Node> getSpecification(NodeFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate", "instanceExpiredDate")
        .matchSearchFields("name", "extSearchMerge")
        .orderByFields("id", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}
