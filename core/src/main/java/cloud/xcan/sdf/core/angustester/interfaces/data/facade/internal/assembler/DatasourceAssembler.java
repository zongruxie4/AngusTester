package cloud.xcan.sdf.core.angustester.interfaces.data.facade.internal.assembler;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.data.datasource.Datasource;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.datasource.DatasourceTestDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.datasource.DatasourceDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.datasource.DatasourceVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.sdf.spec.experimental.AESValue;
import java.util.Set;

public class DatasourceAssembler {

  public static Datasource addDtoToDomain(DatasourceAddDto dto) {
    return new Datasource()
        .setProjectId(dto.getProjectId())
        .setName(dto.getName())
        .setDatabase(dto.getDatabase())
        .setDriverClassName(dto.getDriverClassName())
        .setUsername(dto.getUsername())
        .setPassd(new AESValue(dto.getPassd())
            .encrypt(String.valueOf(getOptTenantId())))
        .setJdbcUrl(dto.getJdbcUrl());
  }

  public static Datasource replaceDtoToDomain(DatasourceReplaceDto dto) {
    return new Datasource()
        .setId(dto.getId())
        .setProjectId(isNull(dto.getId()) ? dto.getProjectId() : null)
        .setName(dto.getName())
        .setDatabase(dto.getDatabase())
        .setDriverClassName(dto.getDriverClassName())
        .setUsername(dto.getUsername())
        .setPassd(isEmpty(dto.getPassd()) ? null
            : new AESValue(dto.getPassd())
                .encrypt(String.valueOf(getOptTenantId())))
        .setJdbcUrl(dto.getJdbcUrl());
  }

  public static Datasource testDtoToDatasource(DatasourceTestDto dto) {
    return new Datasource()
        .setDatabase(dto.getDatabase())
        .setDriverClassName(dto.getDriverClassName())
        .setUsername(dto.getUsername())
        .setPassd(new AESValue(dto.getPassd()))
        .setJdbcUrl(dto.getJdbcUrl());
  }

  public static DatasourceVo toVo(Datasource datasource) {
    return new DatasourceVo()
        .setId(datasource.getId())
        .setProjectId(datasource.getProjectId())
        .setDatabase(datasource.getDatabase())
        .setDriverClassName(datasource.getDriverClassName())
        .setUsername(datasource.getUsername())
        .setName(datasource.getName())
        // Unrestricted implementation, decoding password here as mock datasource script
        .setPassd(nonNull(datasource.getPassd())
            ? datasource.getPassd().decrypt(String.valueOf(getOptTenantId())) : null)
        .setJdbcUrl(datasource.getJdbcUrl())
        .setTenantId(datasource.getTenantId())
        .setCreatedBy(datasource.getCreatedBy())
        .setCreatedDate(datasource.getCreatedDate())
        .setLastModifiedBy(datasource.getLastModifiedBy())
        .setLastModifiedByName(datasource.getLastModifiedByName())
        .setAvatar(datasource.getAvatar())
        .setLastModifiedDate(datasource.getLastModifiedDate());
  }

  public static DatasourceDetailVo toDetailVo(Datasource datasource) {
    return new DatasourceDetailVo()
        .setId(datasource.getId())
        .setProjectId(datasource.getProjectId())
        .setDatabase(datasource.getDatabase())
        .setDriverClassName(datasource.getDriverClassName())
        .setUsername(datasource.getUsername())
        .setName(datasource.getName())
        // Unrestricted implementation, decoding password here as mock datasource script
        .setPassd(nonNull(datasource.getPassd())
            ? datasource.getPassd().decrypt(String.valueOf(getOptTenantId())) : null)
        .setJdbcUrl(datasource.getJdbcUrl())
        .setTenantId(datasource.getTenantId())
        .setCreatedBy(datasource.getCreatedBy())
        .setCreatedDate(datasource.getCreatedDate())
        .setLastModifiedBy(datasource.getLastModifiedBy())
        .setLastModifiedDate(datasource.getLastModifiedDate());
  }

  public static GenericSpecification<Datasource> getSpecification(DatasourceFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "lastModifiedDate")
        .orderByFields("id", "lastModifiedDate")
        .matchSearchFields("name")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(DatasourceSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "lastModifiedDate")
        .orderByFields("id", "lastModifiedDate")
        .matchSearchFields("name")
        .build();
  }
}