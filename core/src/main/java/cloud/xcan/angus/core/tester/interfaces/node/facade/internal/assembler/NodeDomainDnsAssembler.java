package cloud.xcan.angus.core.tester.interfaces.node.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.api.enums.NormalStatus;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.node.dns.NodeDomainDns;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.dns.NodeDomainDnsAddDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.dns.NodeDomainDnsFindDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.dto.dns.NodeDomainDnsUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.node.facade.vo.dns.NodeDomainDnsDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class NodeDomainDnsAssembler {

  public static NodeDomainDns addDtoToDomain(NodeDomainDnsAddDto dto) {
    return new NodeDomainDns()
        .setDomainId(dto.getDomainId())
        .setType(dto.getType())
        .setName(dto.getName())
        .setLine(dto.getLine())
        .setStatus(NormalStatus.UNKNOWN)
        .setValue(dto.getValue())
        .setTtl(nullSafe(dto.getTtl(), 600));
  }

  public static NodeDomainDns updateDtoToDomain(NodeDomainDnsUpdateDto dto) {
    return new NodeDomainDns()
        .setId(dto.getId())
        .setDomainId(dto.getDomainId())
        .setType(dto.getType())
        .setName(dto.getName())
        .setLine(dto.getLine())
        .setValue(dto.getValue())
        .setTtl(dto.getTtl());
  }

  public static NodeDomainDnsDetailVo toDetailVo(NodeDomainDns dns) {
    return new NodeDomainDnsDetailVo()
        .setId(dns.getId())
        .setDomainId(dns.getDomainId())
        .setType(dns.getType())
        .setName(dns.getName())
        .setLine(dns.getLine())
        .setTtl(dns.getTtl())
        .setStatus(dns.getStatus())
        .setValue(dns.getValue())
        .setCreatedBy(dns.getCreatedBy())
        .setCreatedDate(dns.getCreatedDate())
        .setTenantId(dns.getTenantId())
        .setLastModifiedBy(dns.getLastModifiedBy())
        .setLastModifiedDate(dns.getLastModifiedDate());
  }

  public static GenericSpecification<NodeDomainDns> getSpecification(NodeDomainDnsFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "createdDate")
        .matchSearchFields("name")
        .build();
    return new GenericSpecification<>(filters);
  }
}
