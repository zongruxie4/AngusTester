package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.UNARCHIVED_API_NAME_PREFIX;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUnarchivedAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUnarchivedFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUnarchivedSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUnarchivedUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisUnarchivedDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisUnarchivedListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author xiaolong.liu
 */
public class ApisUnarchivedAssembler {

  public static ApisUnarchived addDtoToDomain(ApisUnarchivedAddDto dto) {
    return new ApisUnarchived()
        .setProjectId(dto.getProjectId())
        .setProtocol(dto.getProtocol())
        .setMethod(dto.getMethod())
        .setEndpoint(dto.getEndpoint())
        /////// OpenAPI document start
        //.setTags(dto.getTags())
        .setSummary(getDefaultName(dto))
        .setDescription(dto.getDescription())
        //.setOperationId(stringSafe(dto.getOperationId()))
        .setParameters(dto.getParameters())
        .setRequestBody(dto.getRequestBody())
        .setResponses(dto.getResponses())
        //.setDeprecated(dto.getDeprecated())
        //.setSecurity(dto.getSecurity())
        .setCurrentServer(dto.getCurrentServer())
        .setExtensions(dto.getExtensions())
        /////// OpenAPI document end
        .setAuthentication(dto.getAuthentication())
        .setAssertions(dto.getAssertions())
        .setSecurityFlag(nonNull(dto.getAuthentication()));
  }

  private static String getDefaultName(ApisUnarchivedAddDto dto) {
    return ObjectUtils.isEmpty(dto.getSummary()) ? UNARCHIVED_API_NAME_PREFIX
        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"))
        + randomAlphanumeric(2) : dto.getSummary();
  }

  public static ApisUnarchived updateDtoToDomain(ApisUnarchivedUpdateDto dto) {
    return new ApisUnarchived().setId(dto.getId())
        .setProtocol(dto.getProtocol())
        .setMethod(dto.getMethod())
        .setEndpoint(dto.getEndpoint())
        /////// OpenAPI document start
        //.setTags(dto.getTags())
        .setSummary(dto.getSummary())
        .setDescription(dto.getDescription())
        //.setOperationId(dto.getOperationId())
        .setParameters(dto.getParameters())
        .setRequestBody(dto.getRequestBody())
        .setResponses(dto.getResponses())
        //.setDeprecated(dto.getDeprecated())
        //.setSecurity(dto.getSecurity())
        .setCurrentServer(dto.getCurrentServer())
        .setExtensions(dto.getExtensions())
        /////// OpenAPI document end
        .setAuthentication(dto.getAuthentication())
        .setAssertions(dto.getAssertions())
        .setSecurityFlag(nonNull(dto.getAuthentication()) ? true : null);
  }

  public static ApisUnarchivedDetailVo toApisUnarchivedDetailVo(ApisUnarchived api) {
    return new ApisUnarchivedDetailVo().setId(api.getId())
        .setProjectId(api.getProjectId())
        .setProtocol(api.getProtocol())
        .setMethod(nonNull(api.getMethod()) ? api.getMethod().getValue() : null)
        .setEndpoint(api.getEndpoint())
        /////// OpenAPI document start
        //.setTags(dto.getTags())
        .setSummary(api.getSummary())
        .setDescription(api.getDescription())
        //.setOperationId(api.getOperationId())
        .setParameters(api.getParameters())
        .setRequestBody(api.getRequestBody())
        .setResponses(api.getResponses())
        //.setDeprecated(dto.getDeprecated())
        //.setSecurity(dto.getSecurity())
        .setCurrentServer(api.getCurrentServer())
        .setExtensions(api.getExtensions())
        /////// OpenAPI document end
        .setAuthentication(api.getAuthentication())
        .setAssertions(api.getAssertions())
        .setSecurityFlag(api.getSecurityFlag())
        .setCreatedDate(api.getCreatedDate())
        .setCreatedBy(api.getCreatedBy())
        .setTenantId(api.getTenantId())
        .setSecurityFlag(api.getSecurityFlag())
        .setLastModifiedDate(api.getLastModifiedDate());
  }

  public static ApisUnarchivedListVo toApisUnarchivedListVo(ApisUnarchived api) {
    return new ApisUnarchivedListVo().setId(api.getId())
        .setProjectId(api.getProjectId())
        .setProtocol(api.getProtocol())
        .setMethod(nonNull(api.getMethod()) ? api.getMethod().getValue() : null)
        .setEndpoint(api.getEndpoint())
        .setSummary(api.getSummary())
        .setCreatedDate(api.getCreatedDate())
        .setLastModifiedDate(api.getLastModifiedDate());
  }

  public static GenericSpecification<ApisUnarchived> getSpecification(ApisUnarchivedFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("summary", "endpoint")
        .orderByFields("endpoint", "createdDate", "createdBy", "id")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(ApisUnarchivedSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id")
        .matchSearchFields("summary", "endpoint")
        .orderByFields("endpoint", "createdDate", "createdBy", "id")
        .build();
  }

}
