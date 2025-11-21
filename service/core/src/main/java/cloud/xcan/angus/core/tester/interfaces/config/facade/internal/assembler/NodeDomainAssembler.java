package cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler;

import cloud.xcan.angus.api.enums.NormalStatus;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.config.node.domain.NodeDomain;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain.NodeDomainAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain.NodeDomainFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain.NodeDomainUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.domain.NodeDomainDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class NodeDomainAssembler {

  public static NodeDomain addDtoToDomain(NodeDomainAddDto dto) {
    return new NodeDomain()
        .setName(dto.getName())
        .setStatus(NormalStatus.UNKNOWN);
  }

  public static NodeDomain updateDtoToDomain(NodeDomainUpdateDto dto) {
    return new NodeDomain()
        .setId(dto.getId())
        //.setName(dto.getName())
        .setStatus(dto.getStatus());
  }

  public static NodeDomainDetailVo toDetailVo(NodeDomain dto) {
    return new NodeDomainDetailVo()
        .setId(dto.getId())
        .setName(dto.getName())
        .setStatus(dto.getStatus())
        .setDnsNum(dto.getDnsNum())
        .setCreatedBy(dto.getCreatedBy())
        .setCreatedDate(dto.getCreatedDate())
        .setLastModifiedBy(dto.getLastModifiedBy())
        .setLastModifiedDate(dto.getLastModifiedDate());
  }

  public static GenericSpecification<NodeDomain> getSpecification(NodeDomainFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("name")
        .orderByFields("id", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }
}
