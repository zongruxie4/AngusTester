package cloud.xcan.sdf.core.angustester.interfaces.node.facade.internal.assembler;

import static cloud.xcan.sdf.core.angustester.application.converter.NodeConverter.encryptHostPassd;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.angusctrl.node.vo.NodeInfoDetailVo;
import cloud.xcan.sdf.api.angustester.node.dto.NodeCountFindDto;
import cloud.xcan.sdf.api.angustester.node.dto.NodeFindDto;
import cloud.xcan.sdf.api.angustester.node.vo.NodeDetailVo;
import cloud.xcan.sdf.api.commonlink.node.NodeSource;
import cloud.xcan.sdf.api.dto.EnabledOrDisabledDto;
import cloud.xcan.sdf.api.enums.NodeRole;
import cloud.xcan.sdf.api.pojo.node.NodeSpecData;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceInfo;
import cloud.xcan.sdf.core.angustester.domain.node.Node;
import cloud.xcan.sdf.core.angustester.infra.iaas.InstanceChargeType;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodePurchaseDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.NodeUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.NodeMockServiceListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.sdf.spec.utils.DataSizeUtils;
import com.google.common.collect.Sets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class NodeAssembler {

  public static Node addDtoToDomain(NodeAddDto dto) {
    return new Node()
        .setName(dto.getName())
        .setIp(dto.getIp())
        .setPublicIp(dto.getPublicIp())
        .setUsername(dto.getUsername())
        .setPassd(isEmpty(dto.getPassd()) ? null : encryptHostPassd(dto.getPassd()))
        .setSshPort(dto.getSshPort())
        .setDomain(stringSafe(dto.getDomain()))
        .setSource(NodeSource.OWN_NODE)
        .setRoles(dto.getRoles())
        .setFreeFlag(nullSafe(dto.getFreeFlag(), false))
        .setEnabledFlag(true)
        .setInstallAgentFlag(null/*False will ignore automatic agent installation*/)
        .setDeletedFlag(false);
  }

  public static Node updateDtoToDomain(NodeUpdateDto dto) {
    Long userId = Objects.isNull(getUserId()) ? -1L : getUserId();
    Node node = new Node()
        .setId(dto.getId())
        .setName(dto.getName())
        .setIp(dto.getIp())
        .setPublicIp(dto.getPublicIp())
        .setUsername(dto.getUsername())
        .setPassd(isEmpty(dto.getPassd()) ? null : encryptHostPassd(dto.getPassd()))
        .setSshPort(dto.getSshPort())
        .setDomain(stringSafe(dto.getDomain()))
        .setRoles(dto.getRoles());
    node.setLastModifiedBy(userId);
    node.setLastModifiedDate(LocalDateTime.now());
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
        .setFreeFlag(dto.getFreeFlag())
        .setEnabledFlag(true)
        .setInstallAgentFlag(null/*False will ignore automatic agent installation*/)
        .setDeletedFlag(false);
  }

  public static Node enabledDtoToDomain(EnabledOrDisabledDto dto) {
    return new Node().setId(dto.getId())
        .setEnabledFlag(dto.getEnabledFlag());
  }

  public static List<Node> deletesDtoToDomain(Collection<Long> ids) {
    return ids.stream().map(i -> new Node().setId(i))
        .collect(Collectors.toList());
  }

  public static void completeNodeDetailVo(NodeDetailVo nodeDetailVo, NodeInfoDetailVo nodeInfoVo) {
    if (nodeInfoVo != null) {
      nodeDetailVo.setOnlineFlag(nodeInfoVo.getAgentOnlineFlag());
      nodeDetailVo.setOs(nodeInfoVo.getOs());
      //      // Fix: The installation status of the Agent is not synchronized during manual installation.
      //      if (nonNull(nodeInfoVo.getAgentOnlineFlag()) && nodeInfoVo.getAgentOnlineFlag()){
      //        nodeDetailVo.setInstallAgentFlag(true);
      //      }
      if (!nodeDetailVo.getSource().isOnlineBuy() && nonNull(nodeInfoVo.getInfo())) {
        setAgentNodeSpec(nodeDetailVo, nodeInfoVo);
      }
    }
  }

  public static void setAgentNodeSpec(NodeDetailVo nodeDetailVo, NodeInfoDetailVo infoDetailVo) {
    NodeSpecData nodeSpecData = new NodeSpecData();
    nodeSpecData.setCpu(infoDetailVo.getInfo().getCpuNum());
    nodeSpecData.setMemory((int) DataSizeUtils.formatRoundingDataSize(infoDetailVo.getInfo()
        .getMemTotal()).toGigabytes());
    nodeSpecData.setSysDisk((int) DataSizeUtils.formatDataSize(infoDetailVo.getInfo()
        .getFsTotal()).toGigabytes());
    if (isNotEmpty(infoDetailVo.getInfo().getNetworkSpeed())) {
      int bandwidth = Double.valueOf(infoDetailVo.getInfo().getNetworkSpeed()).intValue();
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
        .setPassd(
            null/*node.getSource().equals(NodeSource.ONLINE_BUY) ? "******" : node.getPassd()*/)
        .setSshPort(node.getSshPort())
        .setSpec(node.getSpec())
        .setSource(node.getSource())
        .setRoles(node.getRoles())
        .setFreeFlag(node.getFreeFlag())
        .setInstanceExpiredDate(node.getInstanceExpiredDate())
        .setEnabledFlag(node.getEnabledFlag())
        .setInstallAgentFlag(node.getInstallAgentFlag())
        //.setOnlineFlag(false) // Default
        .setOrderId(node.getOrderId())
        .setAdmin(isAdmin)
        .setRoles(node.getRoles())
        .setDeletedFlag(node.getDeletedFlag())
        .setTenantId(node.getTenantId())
        .setCreatedBy(node.getCreatedBy())
        .setCreatedDate(LocalDateTime.now())
        .setLastModifiedBy(node.getLastModifiedBy())
        .setLastModifiedDate(node.getLastModifiedDate());
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
        .setAuthFlag(service.getAuthFlag())
        .setAssocServicesFlag(Objects.nonNull(service.getAssocServiceId()))
        .setTenantId(service.getTenantId())
        .setCreatedBy(service.getCreatedBy())
        .setCreatedDate(service.getCreatedDate())
        .setLastModifiedBy(service.getLastModifiedBy())
        .setLastModifiedDate(service.getLastModifiedDate());
  }

  public static Specification<Node> getCountSpecification(NodeCountFindDto dto) {
    return (Specification<Node>) (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      Predicate deletedPredicate = criteriaBuilder.equal(root.get("deletedFlag"), dto.getSource());
      predicates.add(deletedPredicate);
      if (nonNull(dto.getTenantId())) {
        Predicate tenantPredicate = criteriaBuilder.equal(root.get("tenantId"), dto.getTenantId());
        predicates.add(tenantPredicate);
      }
      if (nonNull(dto.getEnabledFlag())) {
        Predicate enabledPredicate = criteriaBuilder
            .equal(root.get("enabledFlag"), dto.getEnabledFlag());
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

  public static Set<SearchCriteria> getSearchCriteria(NodeSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate", "instanceExpiredDate")
        .matchSearchFields("name", "extSearchMerge")
        .orderByFields("id", "createdDate")
        .build();
  }

}
