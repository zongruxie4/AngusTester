package cloud.xcan.sdf.core.angustester.interfaces.node.facade.internal.assembler;

import cloud.xcan.sdf.api.enums.NormalStatus;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.node.domain.NodeDomain;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain.NodeDomainAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain.NodeDomainFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain.NodeDomainUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.domain.NodeDomainDetailVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
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
