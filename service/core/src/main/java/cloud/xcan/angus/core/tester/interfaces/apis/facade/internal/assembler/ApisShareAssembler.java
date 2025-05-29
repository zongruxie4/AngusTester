package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler;


import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.apis.share.ApisShare;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareSearchDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareAddVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareViewVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class ApisShareAssembler {

  public static ApisShare addDtoToDomain(ApisShareAddDto dto) {
    return new ApisShare().setName(dto.getName())
        .setRemark(dto.getRemark()).setExpiredDate(dto.getExpiredDate())
        .setDisplayOptions(dto.getDisplayOptions()).setShareScope(dto.getShareScope())
        .setServicesId(dto.getServicesId()).setApisIds(dto.getApisIds())
        .setBaseUrl(dto.getBaseUrl()).setViewNum(0);
  }

  public static ApisShareAddVo toShareAddVo(ApisShare share) {
    return new ApisShareAddVo().setId(share.getId())
        .setUrl(share.getUrl());
  }

  public static ApisShare updateDtoToDomain(ApisShareUpdateDto dto) {
    return new ApisShare().setId(dto.getId())
        .setName(dto.getName())
        .setRemark(dto.getRemark()).setExpiredDate(dto.getExpiredDate())
        .setDisplayOptions(dto.getDisplayOptions()).setShareScope(dto.getShareScope())
        .setServicesId(dto.getServicesId()).setApisIds(dto.getApisIds())
        .setBaseUrl(dto.getBaseUrl());
  }

  public static ApisShareViewVo toShareDetailVo(ApisShare share) {
    return new ApisShareViewVo()
        .setId(share.getId())
        .setName(share.getName())
        .setRemark(share.getRemark())
        .setExpiredDate(share.getExpiredDate())
        .setDisplayOptions(share.getDisplayOptions())
        .setShareScope(share.getShareScope())
        .setServicesId(share.getServicesId())
        .setApisIds(share.getApisIds())
        .setViewNum(share.getViewNum())
        .setExpired(!share.isNotExpired())
        .setCreatedBy(share.getCreatedBy())
        .setCreatedDate(share.getCreatedDate())
        .setUrl(share.getUrl())
        .setOpenapi(share.getOpenapi())
        .setApiProxy(share.getApiProxy());
  }

  public static ApisShareVo toVo(ApisShare share) {
    return new ApisShareVo()
        .setId(share.getId())
        .setName(share.getName())
        .setRemark(share.getRemark())
        .setExpiredDate(share.getExpiredDate())
        .setDisplayOptions(share.getDisplayOptions())
        .setShareScope(share.getShareScope())
        .setServicesId(share.getServicesId())
        .setApisIds(share.getApisIds())
        .setUrl(share.getUrl())
        .setViewNum(share.getViewNum())
        .setExpired(!share.isNotExpired())
        .setCreatedBy(share.getCreatedBy())
        .setCreatedByName(share.getCreatedByName())
        .setCreatedByAvatar(share.getAvatar())
        .setCreatedDate(share.getCreatedDate())
        .setLastModifiedBy(share.getLastModifiedBy())
        .setLastModifiedDate(share.getLastModifiedDate());
  }

  public static GenericSpecification<ApisShare> getSpecification(ApisShareFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "createdBy", "createdDate", "expiredDate", "name")
        .matchSearchFields("name", "remark")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(ApisShareSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "createdBy", "createdDate", "expiredDate", "name")
        .matchSearchFields("name", "remark")
        .build();
  }
}
