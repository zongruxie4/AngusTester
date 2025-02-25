package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesign;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignSource;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.design.ApisDesignDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.design.ApisDesignVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class ApisDesignAssembler {

  public static ApisDesign addToDomain(ApisDesignAddDto dto) {
    return new ApisDesign().setProjectId(dto.getProjectId())
        .setName(dto.getName()).setReleaseFlag(false)
        .setOpenapiSpecVersion(dto.getOpenapiSpecVersion())
        .setDesignSource(ApisDesignSource.MANUAL_CREATED);
  }

  public static ApisDesignDetailVo toDetailVo(ApisDesign design) {
    return new ApisDesignDetailVo().setId(design.getId())
        .setProjectId(design.getProjectId())
        .setName(design.getName()).setReleaseFlag(design.getReleaseFlag())
        .setOpenapiSpecVersion(design.getOpenapiSpecVersion())
        .setOpenapi(design.getLatestOpenapi())
        .setDesignSource(design.getDesignSource())
        .setDesignSourceId(design.getDesignSourceId())
        .setDesignSourceName(design.getDesignSourceName())
        .setTenantId(design.getTenantId())
        .setCreatedBy(design.getCreatedBy()).setCreatedDate(design.getCreatedDate())
        .setLastModifiedBy(design.getLastModifiedBy())
        .setLastModifiedDate(design.getLastModifiedDate());
  }

  public static ApisDesignVo toVo(ApisDesignInfo design) {
    return new ApisDesignVo().setId(design.getId())
        .setProjectId(design.getProjectId())
        .setName(design.getName()).setReleaseFlag(design.getReleaseFlag())
        .setOpenapiSpecVersion(design.getOpenapiSpecVersion())
        .setDesignSource(design.getDesignSource())
        .setDesignSourceId(design.getDesignSourceId())
        .setDesignSourceName(design.getDesignSourceName())
        .setTenantId(design.getTenantId())
        .setCreatedBy(design.getCreatedBy()).setCreatedDate(design.getCreatedDate())
        .setLastModifiedBy(design.getLastModifiedBy())
        .setLastModifiedDate(design.getLastModifiedDate());
  }

  public static GenericSpecification<ApisDesignInfo> getSpecification(ApisDesignFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("summary")
        .orderByFields("id", "name", "createdBy", "createdDate", "lastModifiedBy",
            "lastModifiedDate")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(ApisDesignSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("name")
        .orderByFields("id", "name", "createdBy", "createdDate", "lastModifiedBy",
            "lastModifiedDate")
        .build();
  }
}
