package cloud.xcan.angus.core.tester.application.query.services;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.model.services.ApisTestCount;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ServicesQuery {

  Services detail(Long id, Boolean joinSchema);

  Page<Services> list(GenericSpecification<Services> spec, PageRequest pageRequest);

  MockService associationMockService(Long id);

  ApisTestCount countServiceTestApis(Long serviceId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  ApisTestCount countProjectTestApis(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  Services find0(Long id);

  List<Services> find0ByIds(Collection<Long> ids);

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

