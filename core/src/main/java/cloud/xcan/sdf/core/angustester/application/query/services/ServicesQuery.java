package cloud.xcan.sdf.core.angustester.application.query.services;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockService;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.model.services.ApisTestCount;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ServicesQuery {

  Services detail(Long id, Boolean joinSchemaFlag);

  Page<Services> list(GenericSpecification<Services> spec, PageRequest pageRequest);

  MockService associationMockService(Long id);

  ApisTestCount countServiceTestApis(Long serviceId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  ApisTestCount countProjectTestApis(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  Services find0(Long id);

  List<Long> hasApisServiceIds(Collection<Long> serviceIds);

  Services checkAndFind(Long id);

  List<Services> checkAndFind(Set<Long> serviceIds);

  Boolean isAuthCtrl(Long id);

  void check(Long id);

  void checkQuota(long incr);

  List<Apis> checkApisBelongService(Collection<Long> apiIds, Long serviceId);

  void checkNameExists(long projectId, String name);

  void checkPublishStatus(Services serviceDb);

  void setApisNum(List<Services> services, Set<SearchCriteria> filters);

  void setSafeCloneName(Services service);


}




