package cloud.xcan.angus.core.tester.application.cmd.mock.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_APIS_RESPONSE_ID_INCONSISTENT_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisResponseCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockApisResponseQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author XiaoLong Liu
 */
@Biz
@Slf4j
public class MockApisResponseCmdImpl extends CommCmd<MockApisResponse, Long> implements
    MockApisResponseCmd {

  @Resource
  private MockApisQuery mockApisQuery;

  @Resource
  private MockApisCmd mockApisCmd;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Resource
  private MockApisResponseRepo mockApisResponseRepo;

  @Resource
  private MockApisResponseCmd mockApisResponseCmd;

  @Resource
  private MockApisResponseQuery mockApisResponseQuery;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Resource
  private CommonQuery commonQuery;

  /**
   * Add a single apis response
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(Long apisId, List<MockApisResponse> apisResponses) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      MockApis apisDb;
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Check the mock apis exists
        apisDb = mockApisQuery.checkAndFind(apisId);

        // Check the mock service exists
        serviceDb = mockServiceQuery.checkAndFind(apisDb.getMockServiceId());

        // Check the add api to mock service permission
        mockServiceAuthQuery.checkAddAuth(getUserId(), apisDb.getMockServiceId());

        // Check the apisId in the parameter apisResponses is consistent with the current parameter apisId
        List<MockApisResponse> subApisResponses = apisResponses.stream()
            .filter(r -> nonNull(r.getMockApisId()) && !apisId.equals(r.getMockApisId()))
            .collect(Collectors.toList());
        ProtocolAssert.assertTrue(isEmpty(subApisResponses), MOCK_APIS_RESPONSE_ID_INCONSISTENT_T,
            new Object[]{apisId});

        // Check the name cannot be duplicated
        mockApisResponseQuery.checkAddResponseNameExists(apisResponses);

        commonQuery.checkTenantQuota(QuotaResource.AngusTesterMockApisResponse,
            Collections.singleton(apisId),
            mockApisResponseRepo.countAllByMockApisId(apisId) + apisResponses.size());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        // Add the response associated with the apis
        List<IdKey<Long, Object>> idKeys = mockApisResponseCmd.submitModify(apisDb, apisResponses);

        // Sync the apis in mock service instance
        // Fix:: Synchronization will be triggered before the transaction is committed, resulting in old data being queried
        mockApisCmd.syncAddedApisToServiceInstance(serviceDb, List.of(apisDb));
        return idKeys;
      }
    }.execute();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public List<IdKey<Long, Object>> submitModify(MockApis apisDb,
      List<MockApisResponse> apisResponses) {
    return batchInsert(apisResponses.stream()
        .map(o -> o.setProjectId(apisDb.getProjectId()).setMockServiceId(apisDb.getMockServiceId()))
        .collect(Collectors.toList()), "mockApisId");
  }

  /**
   * Add multiple apis responses
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void add0(List<MockApisResponse> apisResponses) {
    if (isNotEmpty(apisResponses)) {
      // Check the name cannot be duplicated
      mockApisResponseQuery.checkAddResponseNameExists(apisResponses);

      // Delete other assoc.
      mockApisResponseRepo.deleteByMockApisIdIn(
          apisResponses.stream().map(MockApisResponse::getMockApisId).collect(Collectors.toSet()));

      batchInsert(apisResponses);
    }
  }

  /**
   * Note: It is a full replacement.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(Long apisId, List<MockApisResponse> responses) {
    new BizTemplate<Void>() {
      MockApis apisDb;
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Check the fault type and the corresponding status code
        // checkContentStatus(responses);

        // Check the interface exists
        apisDb = mockApisQuery.checkAndFind(apisId);

        // Check the mock service exists
        serviceDb = mockServiceQuery.checkAndFind(apisDb.getMockServiceId());

        // Check the add api to mock service permission
        mockServiceAuthQuery.checkAddAuth(getUserId(), apisDb.getMockServiceId());

        // Check the apisId in the parameter apisResponses is consistent with the current parameter apisId
        List<MockApisResponse> subApisResponses = responses.stream()
            .filter(r -> nonNull(r.getMockServiceId()) && !apisId.equals(r.getMockServiceId()))
            .collect(Collectors.toList());
        if (isNotEmpty(subApisResponses)) {
          throw ProtocolException.of(MOCK_APIS_RESPONSE_ID_INCONSISTENT_T);
        }

        // Check the quota limit
        commonQuery.checkTenantQuota(QuotaResource.AngusTesterMockApisResponse, Set.of(apisId),
            /*mockApisResponseRepo.countAllByMockApisId(apisId) +*/ responses.stream()
                .filter(r -> Objects.isNull(r.getId())).count());
      }

      @Override
      protected Void process() {
        mockApisResponseCmd.submitModify(apisId, apisDb, responses);

        // Sync the apis in mock service instance
        // Fix:: Synchronization will be triggered before the transaction is committed, resulting in old data being queried
        mockApisCmd.syncAddedApisToServiceInstance(serviceDb, List.of(apisDb));
        return null;
      }
    }.execute();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void submitModify(Long apisId, MockApis apisDb, List<MockApisResponse> responses) {
    // Note: It is a full replacement
    // Delete if it doesn't exist in param
    List<Long> exitedIds = mockApisResponseRepo.findIdsByApisId(apisId);
    if (isNotEmpty(exitedIds)) {
      List<Long> requestIds = responses.stream().map(MockApisResponse::getId)
          .collect(Collectors.toList());
      exitedIds.removeAll(requestIds);
      if (isNotEmpty(exitedIds)) {
        mockApisResponseRepo.deleteByIdIn(exitedIds);
      }
    }

    // Modify if it exists, note: You must add logic before it below, otherwise the id will be set to a value after saving it.
    List<MockApisResponse> updateMockApisResponses =
        responses.stream().filter(r -> nonNull(r.getId()))
            .map(o -> o.setProjectId(apisDb.getProjectId())
                .setMockServiceId(apisDb.getMockServiceId()))
            .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(updateMockApisResponses)) {
      // Check the name cannot be duplicated
      mockApisResponseQuery.checkAddResponseNameExists(updateMockApisResponses);
      batchUpdateOrNotFound0(updateMockApisResponses);
    }

    // Add if it doesn't exist
    List<MockApisResponse> addMockApisResponses =
        responses.stream().filter(r -> Objects.isNull(r.getId()))
            .map(o -> o.setProjectId(apisDb.getProjectId())
                .setMockServiceId(apisDb.getMockServiceId()))
            .collect(Collectors.toList());
    if (isNotEmpty(addMockApisResponses)) {
      // Check the name cannot be duplicated
      mockApisResponseQuery.checkAddResponseNameExists(addMockApisResponses);
      batchInsert0(addMockApisResponses);
    }
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long apisId, HashSet<Long> responseIds) {
    new BizTemplate<Void>() {
      MockApis apisDb;
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Check the interface exists
        apisDb = mockApisQuery.checkAndFind(apisId);

        // Check the mock service exists
        serviceDb = mockServiceQuery.checkAndFind(apisDb.getMockServiceId());

        // Check the add api to mock service permission
        mockServiceAuthQuery.checkAddAuth(getUserId(), apisDb.getMockServiceId());
      }

      @Override
      protected Void process() {
        mockApisResponseCmd.submitModify(apisId, responseIds);

        // Sync the apis in mock service instance
        // Fix:: Synchronization will be triggered before the transaction is committed, resulting in old data being queried
        mockApisCmd.syncDeletedApisToServiceInstance(serviceDb, List.of(apisDb));
        return null;
      }
    }.execute();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void submitModify(Long apisId, HashSet<Long> responseIds) {
    mockApisResponseRepo.deleteAllByMockApisIdAndIdIn(apisId, responseIds);
  }

  @Override
  protected BaseRepository<MockApisResponse, Long> getRepository() {
    return this.mockApisResponseRepo;
  }
}
