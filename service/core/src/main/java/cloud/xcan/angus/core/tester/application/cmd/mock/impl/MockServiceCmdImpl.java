package cloud.xcan.angus.core.tester.application.cmd.mock.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.MOCK_SERVICE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_MOCK_APIS_FILE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_MOCK_SERVICE_FILE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.MockApisConverter.toAngusMockApis;
import static cloud.xcan.angus.core.tester.application.converter.MockServiceConverter.assembleMockApisAndResponse;
import static cloud.xcan.angus.core.tester.application.converter.MockServiceConverter.toMockServiceApisSyncDto;
import static cloud.xcan.angus.core.tester.application.converter.MockServiceConverter.toMockServiceNodeDomainDns;
import static cloud.xcan.angus.core.tester.application.converter.MockServiceConverter.toMockServiceStartDto;
import static cloud.xcan.angus.core.tester.application.converter.MockServiceConverter.toMockServiceStopDto;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_ASSOC_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MOCK_SERVICE_IMPORT_FILE_OR_TEXT_REQUIRED;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.parseSample;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.readExampleMockApisContent;
import static cloud.xcan.angus.core.tester.infra.util.MockFileUtils.getExportTmpPath;
import static cloud.xcan.angus.core.tester.infra.util.MockFileUtils.getImportTmpPath;
import static cloud.xcan.angus.core.tester.infra.util.MockFileUtils.isAngusFile;
import static cloud.xcan.angus.core.tester.infra.util.ServicesFileUtils.getImportApiFiles;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static cloud.xcan.angus.spec.experimental.StandardCharsets.UTF_8;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.emptySafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singleton;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.aspectj.util.FileUtil.readAsString;

import cloud.xcan.angus.agent.message.mockservice.StartVo;
import cloud.xcan.angus.agent.message.mockservice.StopVo;
import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockApisResponseCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceAuthCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceManageCmd;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeDomainDnsCmd;
import cloud.xcan.angus.core.tester.application.converter.MockApisConverter;
import cloud.xcan.angus.core.tester.application.converter.MockServiceConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeDomainQuery;
import cloud.xcan.angus.core.tester.application.query.node.NodeQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogRepo;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponseRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.core.tester.domain.config.node.domain.NodeDomain;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils.BIDKey;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceApisSyncDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStartDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStopDto;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Task;
import cloud.xcan.angus.parser.ObjectMapperFactory;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.utils.FileUtils;
import cloud.xcan.jmock.api.support.utils.RandomUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command implementation for mock service management.
 * <p>
 * Provides methods for adding, updating, replacing, importing, associating, starting, stopping, and deleting mock services.
 * <p>
 * Ensures permission checks, activity logging, distributed coordination, and batch operations with transaction management.
 */
@Slf4j
@Biz
public class MockServiceCmdImpl extends CommCmd<MockService, Long> implements MockServiceCmd {

  @Resource
  private MockServiceRepo mockServiceRepo;
  @Resource
  private MockServiceCmd mockServiceCmd;
  @Resource
  private MockServiceQuery mockServiceQuery;
  @Resource
  private MockApisRepo mockApisRepo;
  @Resource
  private MockApisCmd mockApisCmd;
  @Resource
  private MockApisLogRepo mockApisLogRepo;
  @Resource
  private MockApisResponseRepo mockApisResponseRepo;
  @Resource
  private MockApisResponseCmd mockApisResponseCmd;
  @Resource
  private MockServiceAuthCmd mockServiceAuthCmd;
  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ApisCmd apisCmd;
  @Resource
  private MockServiceManageCmd mockServiceManageCmd;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ServicesSchemaQuery projectSchemaQuery;
  @Resource
  private ServicesAuthQuery projectAuthQuery;
  @Resource
  private ScriptQuery scriptQuery;
  @Resource
  private NodeQuery nodeQuery;
  @Resource
  private NodeDomainDnsCmd nodeDomainDnsCmd;
  @Resource
  private NodeDomainQuery nodeDomainQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private ActivityCmd activityCmd;
  @Value("${xcan.mockService.domainSuffix:mock.com}")
  private String mockServiceDomainSuffix;
  @Value("${xcan.mockService.testerApisUrlPrefix}")
  private String mockServiceTesterApisUrlPrefix;

  /**
   * Add a new mock service.
   * <p>
   * Checks member permissions, name uniqueness, node and domain validity, agent status, port availability, and quota.
   * <p>
   * Adds DNS if cloud edition, sets default permissions, and logs creation activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(MockService service) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Node nodeDb;

      @Override
      protected void checkParams() {
        // Check the member permissions
        projectMemberQuery.checkMember(getUserId(), service.getProjectId());

        // Check the name exists
        mockServiceQuery.checkNameExists(service.getName());

        // Check the node information
        // Note: Multi tenant automatic assembly disabled
        nodeDb = nodeQuery.checkRoleAndGetNode(service.getNodeId(), NodeRole.MOCK_SERVICE);

        // Check the domain name valid
        mockServiceQuery.checkValidDomainAndPort(service);

        // Check the node agent status is available
        nodeQuery.checkNodeAgentAvailable(service.getNodeId());

        // Check the port is available on node
        mockServiceQuery.checkNodeAndPortAvailable(nodeDb, service.getServicePort());

        // Check the mock service quota under the tenant
        long existedNodeNum = mockServiceRepo.countAllByTenantId(getOptTenantId());
        commonQuery.checkTenantQuota(QuotaResource.AngusTesterMockService,
            null, existedNodeNum + 1);

        // Limit the maximum number of services that can be created nodes is memory/1GB, but create at least two
        mockServiceQuery.checkNodeServiceNum(nodeDb.getId(), existedNodeNum);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Note: Enable multi tenant automatic assembly
        PrincipalContext.get().setMultiTenantCtrl(true);

        // Add domain name resolution to cloud service edition
        addDnsWhenCloudEditionAndAdd(service, nodeDb);

        service.setId(BIDUtils.getId(BIDKey.mockServiceId));
        service.setNodeIp(nodeDb.getIp());
        IdKey<Long, Object> idKey = insert(service);

        // Generate/openapi2p tokens, with only one generated per tenant -> Noop: Use node access_token to start mock service by agent

        // Add activity
        activityCmd.add(toActivity(MOCK_SERVICE, service, ActivityType.CREATED));

        // Set the default permission to creator
        mockServiceAuthCmd.addCreatorAuth(singleton(idKey.getId()));
        return idKey;
      }
    }.execute();
  }

  /**
   * Update an existing mock service.
   * <p>
   * Checks existence, name uniqueness, permission, and domain validity before updating.
   * <p>
   * Syncs service settings to instance and logs update activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(MockService service) {
    new BizTemplate<Void>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDb = mockServiceQuery.checkAndFind(service.getId());

        // Check the name cannot be duplicate
        if (isNotEmpty(service.getName()) && !serviceDb.getName().equals(service.getName())) {
          mockServiceQuery.checkNameExists(service.getName());
        }

        // Check the to have permission to edit the service
        mockServiceAuthQuery.checkModifyAuth(getUserId(), service.getId());

        // Check the node exists -> NOOP: Modifying nodes not allowed

        // Check the domain name valid
        mockServiceQuery.checkUpdateDomain(service.getServiceDomain(), serviceDb.getNodeId());
      }

      @Override
      protected Void process() {
        mockServiceCmd.submitUpdate(service, serviceDb);

        // Sync mock service setting to instance
        // Fix:: Synchronization will be triggered before the transaction is committed, resulting in old data being queried
        syncServiceToInstance(serviceDb);

        // Update activity
        activityCmd.add(toActivity(MOCK_SERVICE, serviceDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void submitUpdate(MockService service, MockService serviceDb) {
    // Add domain name resolution to cloud service edition
    addDnsWhenCloudEditionAndModify(service, serviceDb);

    mockServiceRepo.save(CoreUtils.copyPropertiesIgnoreNull(service, serviceDb));
  }

  /**
   * Replace (add or update) a mock service.
   * <p>
   * Checks existence, name uniqueness, permission, and domain validity before replacing.
   * <p>
   * Syncs service settings to instance and logs update activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(MockService service) {
    return new BizTemplate<IdKey<Long, Object>>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        if (nonNull(service.getId())) {
          // Check the service exists
          serviceDb = mockServiceQuery.checkAndFind(service.getId());

          // Check the name cannot be duplicate
          if (isNotEmpty(service.getName()) && !serviceDb.getName().equals(service.getName())) {
            mockServiceQuery.checkNameExists(service.getName());
          }

          // Check the to have permission to edit the service
          mockServiceAuthQuery.checkModifyAuth(getUserId(), service.getId());

          // Check the node exists -> NOOP: Modifying nodes not allowed

          // Check the domain name valid
          mockServiceQuery.checkUpdateDomain(service.getServiceDomain(), serviceDb.getNodeId());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(service.getId())) {
          return add(service);
        }

        mockServiceCmd.submitReplace(service, serviceDb);

        // Sync mock service setting to instance
        // Fix:: Synchronization will be triggered before the transaction is committed, resulting in old data being queried
        syncServiceToInstance(serviceDb);

        // Update activity
        activityCmd.add(toActivity(MOCK_SERVICE, serviceDb, ActivityType.UPDATED));
        return new IdKey<Long, Object>().setId(serviceDb.getId()).setKey(serviceDb.getName());
      }
    }.execute();
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void submitReplace(MockService service, MockService serviceDb) {
    addDnsWhenCloudEditionAndModify(service, serviceDb);

    MockServiceConverter.setReplaceInfo(serviceDb, service);
    mockServiceRepo.save(serviceDb);
  }

  @Override
  public void instanceSync(Long id) {
    new BizTemplate<Void>() {
      MockService serviceDb;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDb = mockServiceQuery.checkAndFind(id);
      }

      @Override
      protected Void process() {
        syncAllToInstance0(serviceDb);
        return null;
      }
    }.execute();
  }

  /**
   * Import a mock service from file or text.
   * <p>
   * Checks input validity, parses and adds service, APIs, and responses, and logs import activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> fileImport(MockService service) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the input parameters text and file must have one
        assertTrue(nonNull(service.getImportFile())
            || isNotEmpty(service.getImportText()), MOCK_SERVICE_IMPORT_FILE_OR_TEXT_REQUIRED);
      }

      @SneakyThrows
      @Override
      protected IdKey<Long, Object> process() {
        // Add mock service
        IdKey<Long, Object> idKey = add(service);

        String content;
        // Parse document by file
        if (nonNull(service.getImportFile())) {
          String srcFileName = service.getImportFile().getOriginalFilename();
          assertNotEmpty(srcFileName, "File name is required");
          File tmpPath = getImportTmpPath(null);
          File importFile = new File(tmpPath.getPath() + File.separator + srcFileName);
          service.getImportFile().transferTo(importFile);
          content = readAsString(importFile);
        } else {
          // Parse document by text, High priority.
          content = service.getImportText();
        }

        if (service.getImportSource().isAngus()) {
          // Export Angus format data
          imports(service.getId(), StrategyWhenDuplicated.IGNORE, false, content, null);
        } else {
          List<Apis> openApis = projectSchemaQuery.parseOpenApis(content);
          // Add mock apis and responses
          List<MockApis> mockApis = mockServiceCmd.addMockApisAndResponses(service, openApis);

          // Sync the apis in mock service instance
          mockApisCmd.syncAddedApisToServiceInstance(service, mockApis);
        }

        // Import activity
        activityCmd.add(toActivity(MOCK_SERVICE, service, ActivityType.IMPORT));
        return idKey;
      }
    }.execute();
  }

  /**
   * Associate a mock service with a project/service.
   * <p>
   * Checks existence, permission, and association validity before associating and adding APIs/responses.
   * <p>
   * Logs association activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> servicesAssoc(MockService service) {
    return new BizTemplate<IdKey<Long, Object>>() {
      List<Apis> apisDb;
      Services servicesDb;

      @Override
      protected void checkParams() {
        // Check the sample service exists
        servicesDb = servicesQuery.checkAndFind(service.getAssocServiceId());

        // Check and get apis
        apisDb = apisQuery.findByServiceId(service.getAssocServiceId());

        // Check the view project permission
        projectAuthQuery.checkViewAuth(getUserId(), service.getAssocServiceId());

        // Check if the project is associated with a mock service
        mockServiceQuery.checkAssocProjectExists(servicesDb);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Add mock service
        service.setProjectId(servicesDb.getProjectId());
        IdKey<Long, Object> idKey = add(service);

        // Add mock apis and responses
        parseAndAddMockApisAndResponses(service, apisDb);

        activityCmd.add(toActivity(MOCK_SERVICE, service, ActivityType.ADD_ASSOC_TARGET));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void servicesAssocUpdate(Long id, Long serviceId) {
    new BizTemplate<Void>() {
      MockService mockServiceDb;
      Services servicesDb;

      @Override
      protected void checkParams() {
        // Check the mock service exists
        mockServiceDb = mockServiceQuery.checkAndFind(id);

        // Check the modify permission
        mockServiceAuthQuery.checkModifyAuth(getUserId(), id);

        // Check the mock apis not associated
        assertTrue(isNull(mockServiceDb.getAssocServiceId()),
            MOCK_SERVICE_ASSOC_EXISTED_T, new Object[]{mockServiceDb.getName()});

        // Check the project exists
        servicesDb = servicesQuery.checkAndFind(serviceId);

        // Check the project not associated
        mockServiceQuery.checkAssocProjectExists(servicesDb);
      }

      @Override
      protected Void process() {
        // Association project and mock service
        mockServiceRepo.updateAssocById(id, serviceId);

        // Check and get apis
        List<Apis> apisDb = apisQuery.findByServiceId(serviceId).stream()
            .filter(x -> isNull(x.getMockApisId())).toList();
        parseAndAddMockApisAndResponses(mockServiceDb, apisDb);

        // Save disassociation activity
        activityCmd.add(toActivity(MOCK_SERVICE, mockServiceDb, ActivityType.ASSOC_TARGET));
        return null;
      }
    }.execute();
  }

  /**
   * Start a batch of mock services.
   * <p>
   * Checks existence and permission, starts services, and logs start activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<StartVo> start(HashSet<Long> ids) {
    return new BizTemplate<List<StartVo>>() {
      List<MockService> serviceDbs;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDbs = mockServiceQuery.checkAndFind(ids);
        // Check the modify permission
        mockServiceAuthQuery.batchCheckPermission(ids, MockServicePermission.RUN);
      }

      @Override
      protected List<StartVo> process() {
        // Fix:: Node IP may be updated
        Map<Long, Node> nodeMap = nodeQuery.findNodeMap(serviceDbs.stream()
            .map(MockService::getNodeId).toList());

        // Start mock service
        MockServiceStartDto startDto = toMockServiceStartDto(nodeMap, serviceDbs,
            mockServiceTesterApisUrlPrefix);
        List<StartVo> vos = mockServiceManageCmd.start(startDto);

        // Start activity
        activityCmd.addAll(toActivities(MOCK_SERVICE, serviceDbs, ActivityType.START));
        return vos;
      }
    }.execute();
  }

  /**
   * Stop a batch of mock services.
   * <p>
   * Checks existence and permission, stops services, and logs stop activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<StopVo> stop(HashSet<Long> ids) {
    return new BizTemplate<List<StopVo>>() {
      List<MockServiceInfo> serviceDbs;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDbs = mockServiceQuery.checkAndFindInfo(ids);
        // Check the modify permission
        mockServiceAuthQuery.batchCheckPermission(ids, MockServicePermission.RUN);
      }

      @Override
      protected List<StopVo> process() {
        MockServiceStopDto stopDto = toMockServiceStopDto(serviceDbs);
        List<StopVo> vos = mockServiceManageCmd.stop(stopDto);

        // Stop activity
        activityCmd.addAll(toActivities(MOCK_SERVICE, serviceDbs, ActivityType.STOP));
        return vos;
      }
    }.execute();
  }

  /**
   * Import APIs for a mock service from file or text.
   * <p>
   * Checks existence and permission, parses and adds APIs/responses, and logs import activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void imports(Long id, StrategyWhenDuplicated strategyWhenDuplicated,
      Boolean deleteWhenNotExisted, String content, MultipartFile file) {
    new BizTemplate<Void>() {
      MockService mockServiceDb;

      @Override
      protected void checkParams() {
        // Check the mock service exists
        mockServiceDb = mockServiceQuery.checkAndFind(id);
        // Check the mock apis permission
        mockServiceAuthQuery.checkModifyAuth(getUserId(), id);
      }

      @SneakyThrows
      @Override
      protected Void process() {
        // Import by file
        if (nonNull(file)) {
          // Get import files, If it is a multi file import decompression zip file
          String srcFileName = file.getOriginalFilename();
          assertNotEmpty(srcFileName, "File name is required");
          File tmpPath = getImportTmpPath(null);
          File importFile = new File(tmpPath.getPath() + File.separator + srcFileName);
          file.transferTo(importFile);
          List<File> importFiles = getImportApiFiles(tmpPath, importFile);

          // Save import schema, components and apis
          for (File file : importFiles) {
            try {
              imports(id, strategyWhenDuplicated, deleteWhenNotExisted, readAsString(file), null);
            } catch (IOException e) {
              log.error("Exception reading import file", e);
              throw SysException.of("Exception reading import file, cause: " + e.getMessage(),
                  ExceptionLevel.ERROR);
            }
          }

          // Delete tmp import files
          for (File file : importFiles) {
            FileUtils.deleteQuietly(file);
          }
        }

        // Import by text
        if (isNotEmpty(content)) {
          List<cloud.xcan.angus.model.element.mock.apis.MockApis> angusMockApis;
          if (isAngusFile(content)) {
            // Check and parse script
            AngusScript angusScript = scriptQuery.checkAndParse(content,
                false/*Cannot support necessary parameters plugin and configuration*/);
            assertTrue(angusScript.getType().isMocApis(),
                "Not a mock apis specification file");
            assertNotEmpty(angusScript.getTask().getMockApis(),
                "No mock apis found in the specification file");
            angusMockApis = angusScript.getTask().getMockApis();
          } else {
            // Check and parse OpenAPI script
            List<Apis> openApis = projectSchemaQuery.parseOpenApis(content);
            assertTrue(isNotEmpty(openApis), "Parse OpenAPI specification file is empty");
            List<MockApis> mockApis = new ArrayList<>();
            List<MockApisResponse> mockApisResponses = new ArrayList<>();
            assembleMockApisAndResponse(uidGenerator, mockServiceDb, openApis, mockApis,
                mockApisResponses);
            angusMockApis = MockApisConverter.toAngusMockApis(mockApis, mockApisResponses.stream()
                .collect(Collectors.groupingBy(MockApisResponse::getMockApisId)));
          }

          List<MockApis> mockApisDb = mockApisRepo.findByMockServiceId(id);
          if (isEmpty(mockApisDb)) {
            // Insert mock apis and response
            mockApisCmd.addImportedMockApisAndResponses(mockServiceDb, angusMockApis);
            return null;
          }

          // Update existed mock apis and responses
          if (strategyWhenDuplicated.isCover()) {
            List<MockApis> updateMockApisDb = mockApisDb.stream()
                .filter(x -> angusMockApis.stream().anyMatch(x::sameIdentityAs))
                .toList();
            if (isNotEmpty(updateMockApisDb)) {
              Set<Long> updateApiIds = updateMockApisDb.stream().map(MockApis::getId)
                  .collect(Collectors.toSet());
              mockApisCmd.delete(updateApiIds);

              List<cloud.xcan.angus.model.element.mock.apis.MockApis> updateAngusMockApis
                  = angusMockApis.stream()
                  .filter(x -> mockApisDb.stream().anyMatch(y -> y.sameIdentityAs(x)))
                  .toList();
              mockApisCmd.addImportedMockApisAndResponses(mockServiceDb, updateAngusMockApis);
            }
          }

          // Add new mock apis and responses
          List<cloud.xcan.angus.model.element.mock.apis.MockApis> newAngusMockApis = angusMockApis
              .stream().filter(x -> mockApisDb.stream().noneMatch(y -> y.sameIdentityAs(x)))
              .toList();
          if (isNotEmpty(newAngusMockApis)) {
            // Insert mock apis and response
            mockApisCmd.addImportedMockApisAndResponses(mockServiceDb, newAngusMockApis);
          }

          // Delete not existed in local
          if (deleteWhenNotExisted) {
            List<MockApis> deleteMockApisDb = mockApisDb.stream()
                .filter(x -> angusMockApis.stream().noneMatch(x::sameIdentityAs))
                .toList();
            if (isNotEmpty(deleteMockApisDb)) {
              Set<Long> deleteApiIds = deleteMockApisDb.stream().map(MockApis::getId)
                  .collect(Collectors.toSet());
              mockApisCmd.delete(deleteApiIds);
            }
          }
        }

        // Save import angus activity
        activityCmd.add(toActivity(MOCK_SERVICE, mockServiceDb, ActivityType.IMPORT));
        return null;
      }
    }.execute();
  }

  /**
   * Import an example mock service and its APIs.
   * <p>
   * Adds a sample mock service, sets default permissions, and imports example APIs.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> importExample(Long projectId) {
    return new BizTemplate<IdKey<Long, Object>>() {


      @Override
      protected IdKey<Long, Object> process() {
        URL resourceUrl = this.getClass().getResource("/samples/mock/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_MOCK_SERVICE_FILE);
        MockService service = parseSample(Objects.requireNonNull(resourceUrl),
            new TypeReference<MockService>() {
            }, SAMPLE_MOCK_SERVICE_FILE);

        List<Node> mockNodes = nodeQuery.findByRole(NodeRole.MOCK_SERVICE);
        service.setId(BIDUtils.getId(BIDKey.mockServiceId));
        service.setProjectId(projectId);
        service.setNodeId(isNotEmpty(mockNodes) ? mockNodes.get(0).getId() : -1L);
        service.setNodeIp(isNotEmpty(mockNodes) ? mockNodes.get(0).getIp() : "[MockNodeNotFound]");
        service.setServicePort(RandomUtils.nextInt(10000, 20000));
        IdKey<Long, Object> idKey = insert(service);

        // Set the default permission to creator
        mockServiceAuthCmd.addCreatorAuth(singleton(idKey.getId()));

        // Import mock apis by example
        importApisExample(service.getId());
        return idKey;
      }
    }.execute();
  }

  /**
   * Import example APIs for a mock service.
   * <p>
   * Imports sample APIs and responses for the specified mock service.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void importApisExample(Long id) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        String content = readExampleMockApisContent(this.getClass(), SAMPLE_MOCK_APIS_FILE);
        imports(id, StrategyWhenDuplicated.IGNORE, false, content, null);
        return null;
      }
    }.execute();
  }

  /**
   * Export mock APIs and responses to a file in the specified format.
   * <p>
   * Checks existence and permission, serializes APIs/responses, and logs export activity.
   */
  @Override
  public File export(Long mockServiceId, Set<Long> mockApiIds, SchemaFormat format) {
    return new BizTemplate<File>() {
      MockService mockServiceDb;

      @Override
      protected void checkParams() {
        // Check and find mock services
        mockServiceDb = mockServiceQuery.checkAndFind(mockServiceId);
        // Check the view permission
        mockServiceAuthQuery.checkViewAuth(getUserId(), mockServiceId);
      }

      @SneakyThrows
      @Override
      protected File process() {
        AngusScript angusScript = new AngusScript();
        angusScript.setType(ScriptType.MOCK_APIS);
        angusScript.setTask(new Task());
        List<MockApis> mockApisDb = isEmpty(mockApiIds)
            ? mockApisRepo.findByMockServiceId(mockServiceId)
            : mockApisRepo.findByMockServiceIdAndIdIn(mockServiceId, mockApiIds);
        // When exporting a single apis, the file name is the apis name
        String filename = isNotEmpty(mockApisDb) && mockApisDb.size() == 1
            ? mockApisDb.get(0).getName() : mockServiceDb.getName();
        File exportFile = getExportTmpPath(filename + "." + format.name());
        if (isEmpty(mockApisDb)) {
          String content = format.isYaml()
              ? ObjectMapperFactory.loadYaml().writeValueAsString(angusScript)
              : ObjectMapperFactory.loadJson().writerWithDefaultPrettyPrinter()
                  .writeValueAsString(angusScript);
          FileUtils.writeStringToFile(exportFile, content, UTF_8, false);
          return exportFile;
        }

        List<MockApisResponse> mockApisResponsesDb = isEmpty(mockApiIds)
            ? mockApisResponseRepo.findByMockServiceId(mockServiceId)
            : mockApisResponseRepo.findByMockServiceIdAndMockApisIdIn(mockServiceId, mockApiIds);
        Map<Long, List<MockApisResponse>> mockApisResponsesDbMap =
            isEmpty(mockApisResponsesDb) ? null : mockApisResponsesDb.stream()
                .collect(Collectors.groupingBy(MockApisResponse::getMockApisId));

        List<cloud.xcan.angus.model.element.mock.apis.MockApis> angusMockApis = toAngusMockApis(
            mockApisDb, mockApisResponsesDbMap);
        angusScript.getTask().setMockApis(angusMockApis);

        String content = format.isYaml()
            ? ObjectMapperFactory.loadYaml().writeValueAsString(angusScript)
            : ObjectMapperFactory.loadJson().writerWithDefaultPrettyPrinter()
                .writeValueAsString(angusScript);
        FileUtils.writeStringToFile(exportFile, content, UTF_8, false);

        // Save export angus activity
        activityCmd.add(toActivity(MOCK_SERVICE, mockServiceDb, ActivityType.EXPORT));

        return exportFile;
      }
    }.execute();
  }

  /**
   * Delete association between a mock service and a project/service.
   * <p>
   * Checks existence and permission, removes associations, and logs activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void associationDelete(Long id) {
    new BizTemplate<Void>() {
      MockService mockServiceDb;

      @Override
      protected void checkParams() {
        mockServiceDb = mockServiceQuery.checkAndFind(id);

        // Check the add api permission
        mockServiceAuthQuery.checkModifyAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        // Disassociation mock service and apis
        mockServiceRepo.updateAssocServiceIdToNullByMockServiceId(id);
        mockApisRepo.updateAssocToNullByByMockServiceId(id);

        // Save disassociation activity
        activityCmd.add(toActivity(MOCK_SERVICE, mockServiceDb, ActivityType.DELETE_ASSOC_TARGET));
        return null;
      }
    }.execute();
  }

  /**
   * Delete a batch of mock services.
   * <p>
   * Checks permission, stops services, deletes DNS, and removes all related data.
   */
  @Transactional(rollbackFor = Exception.class, propagation = Propagation.NOT_SUPPORTED)
  @Override
  public void delete(HashSet<Long> ids, Boolean force) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // Check the to have permission to delete the service
        mockServiceAuthQuery.batchCheckPermission(ids, MockServicePermission.DELETE);
      }

      @Override
      protected Void process() {
        if (isNull(force) || !force) {
          // Stop mock service
          stopMockService(ids);

          // Delete domain dns when cloud service edition
          deleteDndWhenCloudEdition(ids);
        } else {
          try {
            stopMockService(ids);
          } catch (Exception e) {
            log.error(e.getMessage());
          }

          try {
            // Delete domain dns when cloud service edition
            deleteDndWhenCloudEdition(ids);
          } catch (Exception e) {
            log.error(e.getMessage());
          }
        }

        // Delete domain dns when cloud service edition
        deleteMockService(ids);
        return null;
      }
    }.execute();
  }

  /**
   * Add mock APIs and responses for a mock service.
   * <p>
   * Assembles and inserts mock APIs and responses in batch.
   */
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public List<MockApis> addMockApisAndResponses(MockService service, List<Apis> apis) {
    List<MockApis> mockApis = new ArrayList<>();
    List<MockApisResponse> mockApisResponses = new ArrayList<>();
    assembleMockApisAndResponse(uidGenerator, service, apis, mockApis, mockApisResponses);

    mockApisCmd.add0(mockApis);
    mockApisResponseCmd.add0(mockApisResponses);
    return mockApis;
  }

  private void parseAndAddMockApisAndResponses(MockService service, List<Apis> apisDb) {
    if (isNotEmpty(apisDb)) {
      for (Apis apis : apisDb) {
        Map<String, String> allRefModels = apisQuery.findApisAllRef(apis);
        apis.setResolvedRefModels(allRefModels);
      }
      List<MockApis> mockApis = mockServiceCmd.addMockApisAndResponses(service, apisDb);

      // Save association with Mock API
      apisCmd.update0(apisDb);

      // Sync the apis in mock service instance
      mockApisCmd.syncAddedApisToServiceInstance(service, mockApis);
    }
  }

  private void addDnsWhenCloudEditionAndAdd(MockService service, Node nodeDb) {
    if (isNotEmpty(service.getServiceDomain()) && isCloudServiceEdition()) {
      NodeDomain domain = nodeDomainQuery.checkAndFind(mockServiceDomainSuffix);
      IdKey<Long, Object> dnsIdKey = nodeDomainDnsCmd.add(toMockServiceNodeDomainDns(service,
          domain, nodeDb, mockServiceDomainSuffix));
      service.setServiceDnsId(dnsIdKey.getId());
    }
  }

  private void addDnsWhenCloudEditionAndModify(MockService service, MockService serviceDb) {
    if (isNotEmpty(service.getServiceDomain()) && isEmpty(serviceDb.getServiceDomain())
        && isCloudServiceEdition()) {
      NodeDomain domain = nodeDomainQuery.checkAndFind(mockServiceDomainSuffix);
      Node nodeDb = nodeQuery.checkRoleAndGetNode(service.getNodeId(), NodeRole.MOCK_SERVICE);
      IdKey<Long, Object> dnsIdKey = nodeDomainDnsCmd.add(toMockServiceNodeDomainDns(service,
          domain, nodeDb, mockServiceDomainSuffix));
      service.setServiceDnsId(dnsIdKey.getId());
    }
  }

  private void deleteDndWhenCloudEdition(HashSet<Long> ids) {
    if (isCloudServiceEdition()) {
      Set<Long> dnsIds = mockServiceRepo.findDnsIdsByIdIn(ids);
      if (isNotEmpty(dnsIds)) {
        for (Long dnsId : dnsIds) {
          nodeDomainDnsCmd.delete(dnsId);
        }
      }
    }
  }

  private void syncServiceToInstance(MockService service) {
    try {
      mockServiceQuery.setNodeInfo(List.of(service));

      MockServiceApisSyncDto syncDto = toMockServiceApisSyncDto(service, false);
      mockServiceManageCmd.syncApis(syncDto);
    } catch (Exception e) {
      log.error("Sync the service setting and apis to mock service instance exception: ", e);
    }
  }

  private void syncAllToInstance0(MockService service) {
    mockServiceQuery.setNodeInfo(List.of(service));
    MockServiceApisSyncDto syncDto = toMockServiceApisSyncDto(service, true);
    mockServiceManageCmd.syncApis(syncDto);
  }

  private void stopMockService(HashSet<Long> ids) {
    List<StopVo> vos = stop(new HashSet<>(ids));
    for (StopVo vo : vos) {
      if (!vo.success) {
        throw SysException.of(String.format("Stop mock service fail, exitCode:%s, cause:%s",
            vo.getExitCode(), String.join("\n", emptySafe(vo.getConsole(),
                List.of("Stop service failed")))));
      }
    }
  }

  public void deleteMockService(HashSet<Long> ids) {
    // Delete mock service apis
    mockApisRepo.deleteByMockServiceIdIn(ids);

    // Delete mock service
    mockServiceRepo.deleteByIdIn(ids);

    // Delete mock apis log
    mockApisLogRepo.deleteAllByServiceIdIn(ids);

    // Delete mock apis response
    mockApisResponseRepo.deleteAllByMockServiceIdIn(ids);
  }

  /**
   * Get the repository for mock services.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<MockService, Long> getRepository() {
    return mockServiceRepo;
  }
}
