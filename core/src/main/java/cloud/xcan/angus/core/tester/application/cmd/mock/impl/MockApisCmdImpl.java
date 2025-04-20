package cloud.xcan.angus.core.tester.application.cmd.mock.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.MOCK_APIS;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.MockApisConverter.assembleMockApisResponse;
import static cloud.xcan.angus.core.tester.application.converter.MockApisConverter.toAssocOrCopeMockApis;
import static cloud.xcan.angus.core.tester.application.converter.MockApisConverter.toImportedMockApis;
import static cloud.xcan.angus.core.tester.application.converter.MockApisConverter.toImportedMockApisResponse;
import static cloud.xcan.angus.core.tester.application.converter.MockServiceConverter.toMockServiceApisDeleteDto;
import static cloud.xcan.angus.core.tester.application.converter.MockServiceConverter.toMockServiceApisSyncDto;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_APIS_ASSOC_EXISTED_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.ctrl.mockservice.MockServiceManageRemote;
import cloud.xcan.angus.api.ctrl.mockservice.dto.MockServiceApisSyncDto;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisResponseCmd;
import cloud.xcan.angus.core.tester.application.converter.MockApisConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisSource;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.model.element.mock.apis.MockResponse;
import cloud.xcan.angus.model.remoting.dto.MockApisRequestCountDto.Counter;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Biz
public class MockApisCmdImpl extends CommCmd<MockApis, Long> implements MockApisCmd {

  @Resource
  private MockApisRepo mockApisRepo;

  @Resource
  private MockApisQuery mockApisQuery;

  @Resource
  private MockApisCmd mockApisCmd;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Resource
  private MockApisResponseRepo mockApisResponseRepo;

  @Resource
  private MockApisResponseCmd mockApisResponseCmd;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ApisAuthQuery apisAuthQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Resource
  private MockServiceManageRemote mockServiceManageRemote;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(List<MockApis> mockApis, boolean saveActivity) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Check the multiple mock apis can only be added for one mock service
        Set<Long> serviceIds = mockApis.stream()
            .map(MockApis::getMockServiceId).collect(Collectors.toSet());
        assertTrue(serviceIds.size() == 1,
            "Multiple mock apis can only be added for one mock service");

        // Check the mock service exists
        serviceDb = mockServiceQuery.checkAndFind(serviceIds.iterator().next());

        // Check the add api permission
        mockServiceAuthQuery.checkAddAuth(getUserId(), serviceDb.getId());

        // Check the name is not existed
        mockApisQuery.checkAddNameExists(serviceDb.getId(), mockApis);

        // Check the uri and method is not existed
        mockApisQuery.checkAddOperationExists(serviceDb.getId(), mockApis);

        // Check the only a maximum of MAX_PROJECT_OR_SERVICE_APIS_NUM apis can be added to a mock service
        mockApisQuery.checkServiceApisQuota(serviceDb, mockApis.size());

        // Check the if the apis is associated with a mock apis
        mockApisQuery.checkAssocApissExists(mockApis);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Save mock apis
        mockApis.forEach(mockApi -> mockApi.setProjectId(serviceDb.getProjectId()));
        List<IdKey<Long, Object>> idKeys = mockApisCmd.submitModify(mockApis);

        // Sync the apis in mock service instance
        // Fix:: Synchronization will be triggered before the transaction is committed, resulting in old data being queried
        syncAddedApisToServiceInstance(serviceDb, mockApis);

        // Save add activity
        activityCmd.addAll(toActivities(MOCK_APIS, mockApis, ActivityType.CREATED));
        return idKeys;
      }
    }.execute();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public List<IdKey<Long, Object>> submitModify(List<MockApis> mockApis) {
    return batchInsert(mockApis, "summary");
  }

  @Override
  public void add0(List<MockApis> mockApis) {
    if (isNotEmpty(mockApis)) {
      // Check the multiple mock apis can only be added for one mock service
      Set<Long> serviceIds = mockApis.stream()
          .map(MockApis::getMockServiceId).collect(Collectors.toSet());
      assertTrue(serviceIds.size() == 1,
          "Multiple mock apis can only be added for one mock service");

      // Delete other assoc.
      mockApisRepo.deleteByApisIdIn(
          mockApis.stream().map(MockApis::getAssocApisId).collect(Collectors.toSet()));

      batchInsert(mockApis);
    }
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<MockApis> mockApis) {
    new BizTemplate<Void>() {
      List<MockApis> apisDb;
      MockService serviceDb;
      Long mockServiceId;

      @Override
      protected void checkParams() {
        // Check the mock apis exists
        apisDb = mockApisQuery.checkAndFind(mockApis.stream().map(MockApis::getId)
            .collect(Collectors.toSet()));

        // Check the multiple mock apis can only be added for one mock service
        Set<Long> serviceIds = apisDb.stream()
            .map(MockApis::getMockServiceId).collect(Collectors.toSet());
        assertTrue(serviceIds.size() == 1,
            "Multiple mock apis can only be updated for one mock service");

        // Check the mock service exists
        mockServiceId = apisDb.get(0).getMockServiceId();
        serviceDb = mockServiceQuery.checkAndFind(mockServiceId);

        // Check the update api permission
        mockServiceAuthQuery.checkModifyAuth(getUserId(), mockServiceId);

        // Check the mock service exists -> NOOP: Modifications are not allowed
        // serviceDb = mockServiceQuery.checkAndFind(mockServiceId);

        // Check the name is not existed
        mockApisQuery.checkUpdateNameExists(mockServiceId, mockApis);

        // Check the uri and method is not existed
        mockApisQuery.checkUpdateOperationExists(mockServiceId, mockApis);

        // @DoInFuture("The associated apis uri and method is not allowed to be modified") -> Do In Web
      }

      @Override
      protected Void process() {
        // Update mock apis
        mockApisCmd.submitUpdate(mockApis, apisDb);

        // Sync the apis in mock service instance
        // Fix:: Synchronization will be triggered before the transaction is committed, resulting in old data being queried
        syncAddedApisToServiceInstance(serviceDb, mockApis);

        // Save update activity
        activityCmd.addAll(toActivities(MOCK_APIS, apisDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void submitUpdate(List<MockApis> mockApis, List<MockApis> apisDb) {
    mockApisRepo.saveAll(CoreUtils.batchCopyPropertiesIgnoreNull(mockApis, apisDb));
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> replace(List<MockApis> mockApis) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      List<MockApis> updatedApis;
      List<MockApis> updatedApisDb;
      MockService serviceDb;
      Long mockServiceId;

      @Override
      protected void checkParams() {
        updatedApis = mockApis.stream().filter(x -> nonNull(x.getId()))
            .collect(Collectors.toList());
        if (isNotEmpty(updatedApis)) {
          // Check the mock apis exists
          updatedApisDb = mockApisQuery.checkAndFind(mockApis.stream().map(MockApis::getId)
              .collect(Collectors.toSet()));

          // Check the multiple mock apis can only be added for one mock service
          Set<Long> serviceIds = updatedApisDb.stream()
              .map(MockApis::getMockServiceId).collect(Collectors.toSet());
          assertTrue(serviceIds.size() == 1,
              "Multiple mock apis can only be replaced for one mock service");

          // Check the mock service exists
          mockServiceId = updatedApisDb.get(0).getMockServiceId();
          serviceDb = mockServiceQuery.checkAndFind(mockServiceId);

          // Check the add api permission
          mockServiceAuthQuery.checkModifyAuth(getUserId(), mockServiceId);

          // Check the mock service exists -> NOOP: Modifications are not allowed
          // serviceDb = mockServiceQuery.checkAndFind(serviceIds.iterator().next());

          // Check the name is not existed
          mockApisQuery.checkUpdateNameExists(mockServiceId, mockApis);

          // Check the uri and method is not existed
          mockApisQuery.checkUpdateOperationExists(mockServiceId, mockApis);

          // @DoInFuture("The associated apis uri and method is not allowed to be modified") -> Do In Web
        }
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = new ArrayList<>();
        List<MockApis> addApis = mockApis.stream().filter(x -> isNull(x.getId()))
            .collect(Collectors.toList());
        if (isNotEmpty(addApis)) {
          idKeys.addAll(add(addApis, true));
        }

        if (isNotEmpty(updatedApis) && isNotEmpty(updatedApisDb)) {
          mockApisCmd.submitReplace(updatedApis, updatedApisDb);

          // Sync the apis in mock service instance
          // Fix:: Synchronization will be triggered before the transaction is committed, resulting in old data being queried
          syncAddedApisToServiceInstance(serviceDb, mockApis);

          // Save replace activity
          activityCmd.addAll(toActivities(MOCK_APIS, updatedApisDb, ActivityType.UPDATED));

          idKeys.addAll(updatedApisDb.stream()
              .map(x -> new IdKey<Long, Object>().setId(x.getId()).setKey(x.getName()))
              .collect(Collectors.toList()));
        }
        return idKeys;
      }
    }.execute();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void submitReplace(List<MockApis> updatedApis, List<MockApis> updatedApisDb) {
    // Replace mock apis
    Map<Long, MockApis> updatedApisMap = updatedApis.stream()
        .collect(Collectors.toMap(MockApis::getId, x -> x));
    for (MockApis apisDb : updatedApisDb) {
      MockApis updatedApi = updatedApisMap.get(apisDb.getId());
      apisDb.setSummary(updatedApi.getName())
          .setDescription(updatedApi.getDescription())
          .setMethod(updatedApi.getMethod())
          .setEndpoint(updatedApi.getEndpoint());
    }
    mockApisRepo.saveAll(updatedApisDb);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> clone(Long id) {
    return new BizTemplate<IdKey<Long, Object>>() {
      MockApis apisDb = null;

      @Override
      protected void checkParams() {
        // Check the mock apis exists
        apisDb = mockApisQuery.checkAndFind(id);

        // Check the add permission
        mockServiceAuthQuery.checkAddAuth(getUserId(), apisDb.getMockServiceId());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Save cloned apis
        MockApis apis = MockApisConverter.toCloneApis(apisDb);
        mockApisQuery.setSafeCloneName(apis);
        IdKey<Long, Object> idKey = add(List.of(apis), false).get(0);

        // Save cloned api responses
        List<MockApisResponse> responses = mockApisResponseRepo.findAllByMockApisId(id);
        if (isNotEmpty(responses)) {
          mockApisResponseCmd.add0(responses.stream().map(MockApisResponse::copy)
              .map(o -> o.setMockApisId(idKey.getId()).setId(null)).collect(Collectors.toList()));
        }

        // Save clone activity
        activityCmd.add(toActivity(MOCK_APIS, apisDb, ActivityType.CLONE));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void move(HashSet<Long> ids, Long targetServiceId) {
    new BizTemplate<Void>() {
      MockService targetServiceDb;
      List<MockApis> apisDb;
      //FuncDir dirDb;

      @Override
      protected void checkParams() {
        // Check the moc apis exists
        apisDb = mockApisQuery.checkAndFind(ids);

        // Check the target service exists
        targetServiceDb = mockServiceQuery.checkAndFind(targetServiceId);

        // Check the only batch move mock apis with one service is allowed
        Set<Long> serviceIds = apisDb.stream().map(MockApis::getMockServiceId)
            .collect(Collectors.toSet());
        assertTrue(serviceIds.size() == 1,
            "Only batch move mock apis with one service is allowed");

        // Check the if the movement position has changed
        assertTrue(!apisDb.get(0).getMockServiceId().equals(targetServiceId),
            "The moving position has not changed");

        // NOOP: Check association synchronization apis does not allow movement

        // Check the to have permission to modify the mock apis
        mockServiceAuthQuery.batchCheckPermission(serviceIds, MockServicePermission.MODIFY);

        // Check the to have permission to add target service
        mockServiceAuthQuery.checkAddAuth(getUserId(), targetServiceId);
      }

      @Override
      protected Void process() {
        // Note: After moving to the target directory, you have resource permissions under the directory.
        // Unlike scenarios and apis, you do not need to authorize the resource's permissions to target service creators.

        // Bug:: Grant permission
        // NOOP

        // Modify mock service
        mockApisRepo.updateMockServiceById(ids, targetServiceId);
        mockApisResponseRepo.updateMockServiceById(ids, targetServiceId);

        // Add move mock apis activity
        activityCmd.addAll(toActivities(MOCK_APIS, apisDb, ActivityType.MOVED_TO,
            cloneActivityParams(targetServiceDb, apisDb.size())));
        return null;
      }

      private List<String[]> cloneActivityParams(MockService service, int size) {
        List<String[]> params = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
          params.add(new String[]{service.getName()});
        }
        return params;
      }
    }.execute();
  }

  @Override
  public void instanceSync(Long id) {
    new BizTemplate<Void>() {
      MockApis apisDb;
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Check the mock apis exists
        apisDb = mockApisQuery.checkAndFind(id);

        // Check the mock service exists
        serviceDb = mockServiceQuery.checkAndFind(apisDb.getMockServiceId());
      }

      @Override
      protected Void process() {
        syncAddedApisToServiceInstance0(serviceDb, List.of(apisDb));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> copyApisAdd(Long mockServiceId, Long apisId) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Check the apis view permission
        apisAuthQuery.checkViewAuth(getUserId(), apisId);

        // Check the apis exists and get apis
        apisDb = apisQuery.checkAndFind(apisId);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Convert apis to mock apis
        MockApis mockApis = toAssocOrCopeMockApis(apisDb, mockServiceId, MockApisSource.COPY_APIS);

        // Add mock apis
        IdKey<Long, Object> idKey = add(singletonList(mockApis), false).get(0);

        // Add mocks apis responses
        Map<String, String> allRefModels = apisQuery.findApisAllRef(apisDb);
        apisDb.setResolvedRefModels(allRefModels);
        addMockApisResponses(mockApis, apisDb, mockServiceId);

        // Save add activity
        activityCmd.add(toActivity(MOCK_APIS, mockApis, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Note: Each mock API can only be associated with one apis.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> assocApisAdd(Long mockServiceId, Long apisId) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Check the apis view permission
        apisAuthQuery.checkViewAuth(getUserId(), apisId);

        // Check the apis exists and get apis
        apisDb = apisQuery.checkAndFind(apisId);

        // Check if the apis is associated with a mock apis
        mockApisQuery.checkAssocApisExists(apisDb);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Convert apis to mock apis
        MockApis mockApis = toAssocOrCopeMockApis(apisDb, mockServiceId, MockApisSource.ASSOC_APIS);

        // Add mock apis
        IdKey<Long, Object> idKey = add(singletonList(mockApis), false).get(0);

        // Add mocks apis responses
        Map<String, String> allRefModels = apisQuery.findApisAllRef(apisDb);
        apisDb.setResolvedRefModels(allRefModels);
        addMockApisResponses(mockApis, apisDb, mockServiceId);

        // Save add activity
        activityCmd.add(toActivity(MOCK_APIS, mockApis, ActivityType.ADD_ASSOC_TARGET));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void assocApisUpdate(Long mockApisId, Long apisId) {
    new BizTemplate<Void>() {
      MockApis mockApisDb;
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Check the mock apis exists
        mockApisDb = mockApisQuery.checkAndFind(mockApisId);

        // Check the modify permission
        mockServiceAuthQuery.checkModifyAuth(getUserId(), mockApisDb.getMockServiceId());

        // Check the mock apis not associated
        assertTrue(isNull(mockApisDb.getAssocApisId()), MOCK_APIS_ASSOC_EXISTED_T,
            new Object[]{mockApisDb.getName()});

        // Check the apis exists
        apisDb = apisQuery.checkAndFindBaseInfo(apisId);

        // Check the apis not associated
        mockApisQuery.checkAssocApisExists(apisDb);
      }

      @Override
      protected Void process() {
        // Association apis and mock apis
        mockApisRepo.updateAssocById(mockApisId, apisId, apisDb.getServiceId());

        // Save disassociation activity
        activityCmd.add(toActivity(MOCK_APIS, mockApisDb, ActivityType.ASSOC_TARGET));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void assocDelete(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<MockApis> apisDb;

      @Override
      protected void checkParams() {
        apisDb = mockApisRepo.findAllById(ids);
        if (isEmpty(apisDb)) {
          return;
        }

        // Check the add api permission
        mockServiceAuthQuery.batchCheckPermission(apisDb.stream().map(MockApis::getMockServiceId)
            .collect(Collectors.toSet()), MockServicePermission.MODIFY);
      }

      @Override
      protected Void process() {
        // Disassociation apis and mock apis
        mockApisRepo.updateAssocToNullByIdIn(ids);

        // Save disassociation activity
        activityCmd.addAll(toActivities(MOCK_APIS, apisDb, ActivityType.DELETE_ASSOC_TARGET));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<MockApis> apisDb;
      MockService serviceDb;

      @Override
      protected void checkParams() {
        apisDb = mockApisRepo.findAllById(ids);
        if (isEmpty(apisDb)) {
          return;
        }

        // Check the only batch move mock apis with one service is allowed
        Set<Long> serviceIds = apisDb.stream().map(MockApis::getMockServiceId)
            .collect(Collectors.toSet());
        assertTrue(serviceIds.size() == 1,
            "Only batch deletion mock apis with one service is allowed");

        // Check the mock service exists
        Long mockServiceId = apisDb.get(0).getMockServiceId();
        serviceDb = mockServiceQuery.checkAndFind(mockServiceId);

        // Check the delete permission
        mockServiceAuthQuery.batchCheckPermission(apisDb.stream().map(MockApis::getMockServiceId)
            .collect(Collectors.toSet()), MockServicePermission.DELETE);
      }

      @Override
      protected Void process() {
        mockApisCmd.submitModify(ids);

        // Sync the apis in mock service instance
        syncDeletedApisToServiceInstance(serviceDb, apisDb);

        // Add delete mock apis activity
        activityCmd.addAll(toActivities(MOCK_APIS, apisDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void submitModify(Collection<Long> ids) {
    // Delete mock apis
    mockApisRepo.deleteByIdIn(ids);

    // Delete mock apis responses
    mockApisResponseRepo.deleteAllByMockApisIdIn(ids);
  }

  @Override
  public void counterUpdate(Map<Long, Counter> hasValueApisCounter) {
    if (ObjectUtils.isNotEmpty(hasValueApisCounter)) {
      for (Entry<Long, Counter> entry : hasValueApisCounter.entrySet()) {
        //    long pushbackNum = counter.pushbackNum.longValue();
        //    long simulateErrorNum = HttpStatus.resolve(log.getResponseStatus()).isError() ? 1 : 0;
        //    long successNum = HttpStatus.resolve(log.getResponseStatus()).is2xxSuccessful() ? 1 : 0;
        //    long exceptionNum = isNotEmpty(log.getExceptionMessage()) ? 1 : 0;
        Counter counter = entry.getValue();
        mockApisRepo.updateCountById(entry.getKey(), counter.requestNum0,
            counter.pushbackNum0, counter.simulateErrorNum0,
            counter.successNum0, counter.exceptionNum0);
      }
    }
  }

  @Override
  public void addMockApisResponses(MockApis mockApi, Apis apis, Long mockServiceId) {
    if (isNotEmpty(apis.getResponses())) {
      // Convert apis response to mock apis responses
      List<MockApisResponse> responses = new ArrayList<>();
      assembleMockApisResponse(uidGenerator, mockApi, apis, mockServiceId, responses);
      // Add mock apis response
      if (isNotEmpty(responses)) {
        mockApisResponseCmd.add0(responses);
      }
    }
  }

  @Override
  public void addImportedMockApisAndResponses(MockService mockServiceDb,
      List<cloud.xcan.angus.model.element.mock.apis.MockApis> angusMockApis) {
    if (isNotEmpty(angusMockApis)) {
      List<MockApis> mockApis = new ArrayList<>();
      List<MockApisResponse> responses = new ArrayList<>();

      for (cloud.xcan.angus.model.element.mock.apis.MockApis angusMockApi : angusMockApis) {
        MockApis mockApi = toImportedMockApis(uidGenerator, mockServiceDb, angusMockApi);
        mockApis.add(mockApi);

        if (isNotEmpty(angusMockApi.getResponses())) {
          for (MockResponse response : angusMockApi.getResponses()) {
            responses.add(toImportedMockApisResponse(uidGenerator, mockApi, response));
          }
        }
      }

      // Add mock apis
      if (isNotEmpty(mockApis)) {
        mockApisCmd.add(mockApis, false);
      }

      // Add mock apis response
      if (isNotEmpty(responses)) {
        mockApisResponseCmd.add0(responses);
      }
    }
  }

  @Override
  public void syncAddedApisToServiceInstance(MockService service, List<MockApis> apis) {
    if (isNotEmpty(apis)) {
      try {
        syncAddedApisToServiceInstance0(service, apis);
      } catch (Exception e) {
        log.error("Sync the apis of mock service instance exception: ", e);
      }
    }
  }

  @Override
  public void syncAddedApisToServiceInstance0(MockService service, List<MockApis> apis) {
    mockServiceQuery.setNodeInfo(List.of(service));
    MockServiceApisSyncDto syncDto = toMockServiceApisSyncDto(service, apis);
    mockServiceManageRemote.syncApis(syncDto).orElseContentThrow();
  }

  @Override
  public void syncDeletedApisToServiceInstance(MockService service, List<MockApis> apis) {
    if (isNotEmpty(apis)) {
      try {
        mockServiceQuery.setNodeInfo(List.of(service));
        mockServiceManageRemote.deleteApis(toMockServiceApisDeleteDto(service, apis));
      } catch (Exception e) {
        log.error("Sync the apis of mock service instance exception: ", e);
      }
    }
  }

  @Override
  protected BaseRepository<MockApis, Long> getRepository() {
    return this.mockApisRepo;
  }
}
