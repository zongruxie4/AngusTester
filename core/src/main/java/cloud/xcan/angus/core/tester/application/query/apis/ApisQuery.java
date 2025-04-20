package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.SimpleActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.count.ApisResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.services.ServiceApisScope;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisQuery {

  Apis detail(Long id, Boolean resolveRef);

  MockApis findMockApis(Long id);

  String openapiDetail(Long id, SchemaFormat format, Boolean gzipCompression, boolean checkExport);

  void check(Long apisId);

  List<Apis> listDetail(HashSet<Long> ids, Boolean resolveRef);

  Page<ApisBasicInfo> findByServiceId(Long serviceId, GenericSpecification<ApisBasicInfo> spec,
      PageRequest pageable, Class<ApisBasicInfo> clz);

  Page<ApisBasicInfo> list(GenericSpecification<ApisBasicInfo> spec, PageRequest pageable,
      Class<ApisBasicInfo> clz);

  List<Server> serverList(Long id);

  ApisResourcesCreationCount creationStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd);

  Page<ApisBasicInfo> find0(Long serviceId, PageRequest pageable, Class<ApisBasicInfo> clz);

  List<Apis> findByServiceId(Long serviceId);

  List<Apis> findByServiceAndIds(Long serviceId, Collection<Long> ids);

  ApisBaseInfo findBase0ById(Long id);

  List<ApisBaseInfo> findBase0ByIdIn(Collection<Long> ids);

  ApisBaseInfo findBaseByIdIn(Long id);

  ApisBaseInfo findLeastByProjectId(Long projectId);

  List<Apis> findAllByServiceIdAndIdIn(Long serviceId, Collection<Long> apiIds);

  Apis findDeRefById(Long id);

  Map<String, String> findApisAllRef(Apis apisDb);

  Map<Long, SimpleActivityResource> getCaseSimpleActivityResourceMap(
      Collection<Long> caseIds);

  Boolean isAuthCtrl(Long id);

  Long countByServiceId(Long id);

  Apis checkAndFind(Long id);

  List<Apis> checkAndFind(Collection<Long> ids);

  ApisBaseInfo checkAndFindBaseInfo(Long id);

  ApisBasicInfo checkAndFindBasicInfo(Long id);

  void checkExists(Long parentId, Collection<Long> apisIds);

  void checkAddServiceApisExisted(Collection<Apis> apis);

  void checkServiceApisOperationNotExisted(Collection<Apis> apis, Collection<Apis> apisDb,
      Long serviceId, boolean replace);

  void checkServiceApisQuota(List<Apis> apis);

  List<Apis> checkAndFindServiceApis(Long serviceId, ServiceApisScope modifyScope,
      String matchEndpointRegex, HttpMethod matchMethod, Set<Long> selectedApisIds,
      Set<String> filterTags);

  void checkOwnerExist(Collection<Apis> apis);

  List<ApisBaseInfo> checkAndFindBaseInfos(Collection<Long> apisIds);

  void checkReleasedStatus(ApisBasicInfo apisInfoDb);

  void checkReleasedStatus(Apis apisDb);

  void checkReleasedStatus(Collection<Apis> apisDbs);

  void checkApisBaseReleasedStatus(ApisBaseInfo apisBasesDb);

  void checkApisBaseReleasedStatus(Collection<ApisBaseInfo> apisBasesDb);

  Set<Long> findParentIds(Collection<Long> apiIds);

  void setFavourite(List<? extends ResourceFavouriteAndFollow<?, ?>> apis);

  void setFollow(List<? extends ResourceFavouriteAndFollow<?, ?>> apis);

  void setInfoAssocMockApis(List<ApisBasicInfo> apis);

  void setAssocMockApis(List<Apis> apis);

  void setSafeCloneName(Apis apis);

  List<Server> setAndGetAvailableServers(Apis apis);

  void setAvailableServers(Apis apis, List<Server> parentServers);

  void setAndGetRefAuthentication(Apis apis);

  void setTagSchemas(Apis apisDb, List<Tag> tagSchemas);

  List<Server> getParentServiceServers(Long apiId);

  void assembleAndSendModifyNoticeEvent(List<ApisBasicInfo> apisDb, List<Activity> activities);

  void assembleAndSendModifyNoticeEvent(ApisBasicInfo apisDb, Activity activity);

  void setOpenApiPathRefModels(Apis apisDb);

}




