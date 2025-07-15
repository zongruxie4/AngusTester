package cloud.xcan.angus.core.tester.application.cmd.services.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_SERVICES_FILES;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ApisConverter.cloneApis;
import static cloud.xcan.angus.core.tester.application.converter.ServicesConverter.cloneService;
import static cloud.xcan.angus.core.tester.application.converter.ServicesConverter.toNewImportService;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_IMPORT_NEW_NAME_EMPTY;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.readExampleServicesContent;
import static cloud.xcan.angus.core.tester.infra.util.ServicesFileUtils.getExportTmpPath;
import static cloud.xcan.angus.core.tester.infra.util.ServicesFileUtils.getImportApiFiles;
import static cloud.xcan.angus.core.tester.infra.util.ServicesFileUtils.getImportTmpPath;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_MS_FORMAT;
import static cloud.xcan.angus.spec.experimental.StandardCharsets.UTF_8;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisAuthCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisDesignCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisTrashCmd;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorPerfCmd;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorStabilityCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesAuthCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCompCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesSchemaCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesSyncCmd;
import cloud.xcan.angus.core.tester.application.converter.ServicesConverter;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceRepo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesExportScope;
import cloud.xcan.angus.core.tester.domain.services.ServicesRepo;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.utils.FileUtils;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.FileUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Biz
@Slf4j
public class ServicesCmdImpl extends CommCmd<Services, Long> implements ServicesCmd {

  @Resource
  private ServicesRepo servicesRepo;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Resource
  private ServicesAuthCmd servicesAuthCmd;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ApisAuthCmd apisAuthCmd;

  @Resource
  private ServicesSyncCmd servicesSyncCmd;

  @Resource
  private ServicesSchemaCmd servicesSchemaCmd;

  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;

  @Resource
  private ServicesCompCmd servicesCompCmd;

  @Resource
  private IndicatorPerfCmd indicatorPerfCmd;

  @Resource
  private IndicatorStabilityCmd indicatorStabilityCmd;

  @Resource
  private ApisDesignCmd apisDesignCmd;

  @Resource
  private ApisTrashCmd trashApisCmd;

  @Resource
  private ApisRepo apisRepo;

  @Resource
  private ApisCmd apisCmd;

  @Resource
  private ActivityCmd activityCmd;

  @Resource
  private MockServiceRepo mockServiceRepo;

  @Resource
  private MockApisRepo mockApisRepo;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Services services, boolean initSchema) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the project member
        projectMemberQuery.checkMember(getUserId(), services.getProjectId());

        // Check the service quota
        servicesQuery.checkQuota(1);

        // Check the service repeated name
        servicesQuery.checkNameExists(services.getProjectId(), services.getName());
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKey = insert(services);

        // Init creator auth
        servicesAuthCmd.addCreatorAuth(idKey.getId(), getAuthCreatorIds());

        // Init schema
        if (initSchema){
          servicesSchemaCmd.init(services);
        }

        // Add service activity
        activityCmd.add(toActivity(SERVICE, services, ActivityType.CREATED));
        return idKey;
      }

      @NotNull
      private Set<Long> getAuthCreatorIds() {
        Set<Long> createdIds = new HashSet<>();
        createdIds.add(getUserId());
        if (nonNull(serviceDb)) {
          createdIds.add(serviceDb.getCreatedBy());
        }
        return createdIds;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void rename(Long id, String name) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDb = servicesQuery.checkAndFind(id);

        if (!serviceDb.getName().equals(name)) {
          // Published api are not allowed to be modified
          servicesQuery.checkPublishStatus(serviceDb);
          // Check the service modification permissions
          servicesAuthQuery.checkModifyAuth(getUserId(), id);
        }
      }

      @Override
      protected Void process() {
        if (!serviceDb.getName().equals(name)) {
          servicesRepo.updateNameById(id, name);

          // Add service name updated activity
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.NAME_UPDATED, name));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Publishing a service will publish all services and apis under the project.
   * <p>
   * Services allow duplicate publications
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void statusUpdate(Long serviceId, ApiStatus status) {
    new BizTemplate<Void>() {
      Services serviceDb = null;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);

        // Check the to have permission to release
        if (status.isReleased() || serviceDb.isReleased()) {
          servicesAuthQuery.checkReleaseAuth(getUserId(), serviceId);
        } else {
          servicesAuthQuery.checkModifyAuth(getUserId(), serviceId);
        }
      }

      @Override
      protected Void process() {
        servicesRepo.updateStatusById(serviceId, status.getValue());
        apisRepo.updateStatusByServiceId(serviceId, status.getValue());

        // Add publish services activity
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.STATUS_UPDATE, status));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clone(Long id) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        serviceDb = servicesQuery.checkAndFind(id);
        servicesAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        // Clone current project
        cloneServiceAndApis(serviceDb);

        // Add clone service activity information
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.CLONE));
        return null;
      }

      private void cloneServiceAndApis(Services serviceDb) {
        // Copy project
        Services clonedService = cloneService(uidGenerator.getUID(), serviceDb);
        servicesQuery.setSafeCloneName(clonedService);

        // NOOP: Query service and copy sync and schemas

        // Query service auth apis
        Set<Long> creatorIds = new HashSet<>();
        creatorIds.add(getUserId());
        creatorIds.add(serviceDb.getCreatedBy());

        List<Apis> apisDb = apisRepo.findByServiceId(serviceDb.getId());
        if (isNotEmpty(apisDb)) {
          List<Apis> cloneApis = cloneApis(uidGenerator, apisDb, clonedService);
          apisRepo.batchInsert0(cloneApis);

          // Initialize service auth
          apisAuthCmd.addCreatorAuth(cloneApis.stream().map(Apis::getId)
              .collect(Collectors.toSet()), creatorIds);
        }

        // Initialize service auth
        servicesAuthCmd.addCreatorAuth(clonedService.getId(), creatorIds);

        // Save service and sync
        insert0(clonedService);

        // Clone service schema
        servicesSchemaCmd.clone(serviceDb.getId(), clonedService.getId());

        // Clone service comps
        servicesCompCmd.clone(serviceDb.getId(), clonedService.getId());

        // projectSyncRepo.batchInsert0(Collections.singleton(cloneConfig));
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> imports(Long projectId, Long serviceId, String serviceName,
      ApiImportSource importSource, StrategyWhenDuplicated strategyWhenDuplicated,
      Boolean deleteWhenNotExisted, MultipartFile file, String content) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        assertTrue(nonNull(file) || isNotEmpty(content),
            "Parameter file or content cannot both be empty");
        assertTrue(nonNull(serviceId) || nonNull(projectId),
            "Parameter projectId is required when serviceId is empty");

        // If the ID is empty, a new service will be imported
        if (nonNull(serviceId)) {
          // Check the service exists
          serviceDb = servicesQuery.checkAndFind(serviceId);

          // Services permission: When the service ID is not empty, judge the service (archive) permission: judge whether it has the permission to save the api in the project
          // (except for projects that are only allowed to add interfaces to the self-created and authorized projects, and projects without permission restrictions), Which interface to call
          servicesAuthQuery.checkAddAuth(getUserId(), serviceId);
        } else {
          assertNotEmpty(serviceName, SERVICE_IMPORT_NEW_NAME_EMPTY);
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        // If no import service is specified, create a new service
        if (isNull(serviceDb)) {
          serviceDb = toNewImportService(projectId, serviceName, importSource);
          add(serviceDb, true);
        }

        // Import by file
        if (nonNull(file)) {
          // Get import files, If it is a multi file import decompression zip file
          String srcFileName = file.getOriginalFilename();
          assertNotEmpty(srcFileName, "File name is required");
          File tmpPath = getImportTmpPath(importSource, null);
          File importFile = new File(tmpPath.getPath() + File.separator + srcFileName);
          try {
            file.transferTo(importFile);
          } catch (IOException e) {
            log.error("Transfer import file exception", e);
            throw SysException.of("Transfer import file exception, cause: "
                + e.getMessage(), ExceptionLevel.ERROR);
          }
          List<File> importFiles = getImportApiFiles(tmpPath, importFile);
          // Save import schema, components and apis
          for (File file : importFiles) {
            try {
              servicesSchemaCmd.openapiReplace(serviceDb.getId(), true, false,
                  FileUtil.readAsString(file), strategyWhenDuplicated, deleteWhenNotExisted,
                  ApiSource.IMPORT, importSource, true, null);
            } catch (IOException e) {
              log.error("Reading import file exception", e);
              throw SysException.of("Reading import file exception, cause: "
                  + e.getMessage(), ExceptionLevel.ERROR);
            }
          }

          // Delete tmp import files
          for (File file : importFiles) {
            FileUtils.deleteQuietly(file);
          }
        }

        // Import by text
        if (isNotEmpty(content)) {
          servicesSchemaCmd.openapiReplace(serviceDb.getId(), true, false,
              content, strategyWhenDuplicated, deleteWhenNotExisted,
              ApiSource.IMPORT, importSource, true, null);
        }

        // Save activity -> add activity by projectSchemaCmd.openapiReplace()
        // activityCmd.add(toActivity(CaseRefType.PROJECT, projectDb, ActivityType.IMPORT));
        return new IdKey<Long, Object>().setId(serviceDb.getId()).setKey(serviceDb.getName());
      }
    }.execute();
  }

  /**
   * Note: When API calls that are not user-action, tenant and user information must be injected
   * into the PrincipalContext.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = new ArrayList<>();
        for (String servicesFile : SAMPLE_SERVICES_FILES) {
          String content = readExampleServicesContent(this.getClass(), servicesFile);
          IdKey<Long, Object> idKey = imports(projectId, null, servicesFile.split("\\.")[0],
              ApiImportSource.OPENAPI, StrategyWhenDuplicated.IGNORE, false, null, content);
          idKeys.add(idKey);
        }
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public File exportProject(ServicesExportScope exportScope, Set<Long> serviceIds,
      Set<Long> apisId, SchemaFormat format, boolean onlyApisComponents) {
    return new BizTemplate<File>() {
      List<Services> servicesDb;

      @Override
      protected void checkParams() {
        // Check the there can only be one service exported by APIS
        assertTrue(exportScope.equals(ServicesExportScope.SERVICE) ||
            serviceIds.size() == 1, "Export by APIS can only have one project");

        //// NOOP:: Check in projectSchemaQuery.schemaDetail()
        servicesAuthQuery.batchCheckPermission(serviceIds, ServicesPermission.EXPORT);

        // Check and find project
        servicesDb = servicesQuery.checkAndFind(serviceIds);
      }

      @Override
      protected File process() {
        // Warn:: Exporting the specified apis by `PROJECT` scope is not supported
        // if (exportScope.equals(MockServicesExportScope.PROJECT)) {
        //  apisId = null;
        // }

        // Write exported OpenAPI to file
        File tmpPath = getExportTmpPath(null);
        List<File> allExportedFiles = new ArrayList<>();
        for (Services service : servicesDb) {
          File exportFile = new File(tmpPath.getPath() + File.separator
              + service.getName() + "." + format.name());
          String content = servicesSchemaQuery.openapiDetail(service.getId(),
              exportScope.equals(ServicesExportScope.SERVICE) ? null : apisId, format,
              false, onlyApisComponents);
          try {
            FileUtils.writeStringToFile(exportFile, content, UTF_8);
            allExportedFiles.add(exportFile);
          } catch (IOException e) {
            throw SysException.of("Exception write export file, cause: "
                + e.getMessage(), ExceptionLevel.URGENT);
          }
        }

        // Get download file
        File finalFile;
        if (allExportedFiles.size() > 1) {
          // Compress downloads when exporting multiple files
          finalFile = new File(tmpPath + File.separator + "Export-All-"
              + format(new Date(), DEFAULT_DATE_TIME_MS_FORMAT) + ".zip");
          try {
            FileUtils.compress(allExportedFiles.stream().map(File::getPath)
                    .collect(Collectors.toList()).toArray(new String[]{}),
                allExportedFiles.stream().map(File::getName)
                    .collect(Collectors.toList()).toArray(new String[]{}), finalFile.getPath());
          } catch (Exception e) {
            throw SysException.of("Exception compress export file, cause: "
                + e.getMessage(), ExceptionLevel.URGENT);
          }
        } else {
          finalFile = allExportedFiles.get(0);
        }

        // Add export service pref activity information
        activityCmd.addAll(toActivities(SERVICE, servicesDb, ActivityType.EXPORT, servicesDb
            .stream().map(Services::getName).toList()
            .stream().map(str -> new Object[]{str}).collect(Collectors.toList())));
        return finalFile;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        serviceDb = servicesRepo.findById(id).orElse(null);
        if (isNull(serviceDb)) {
          return;
        }
        servicesAuthQuery.checkDeleteAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        if (isNull(serviceDb)) {
          return null;
        }

        // Logic delete
        // After the service is deleted, filter the associated service and apis by service deleted
        ServicesConverter.completeDeleteInfo(serviceDb);
        servicesRepo.save(serviceDb);

        // Update apis deletion status
        List<Long> serviceIds = Lists.newArrayList(id);

        // Fix:: Do not delete the apis after deleting the project.
        apisRepo.updateServiceDeleteStatusByService(serviceIds, true);

        // Save to trash
        ApisTrash trash = ServicesConverter.toTrash(serviceDb);
        trashApisCmd.add0(Collections.singletonList(trash));

        // Add delete service activity
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Physically delete, External calling biz must ensure data authed and secured!
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void delete0(List<Long> ids) {
    // Include logic deleted project
    // Find all service and sub service ids
    List<Long> allIds = servicesRepo.findIds0ByIdIn(ids);
    if (isEmpty(allIds)) {
      return;
    }

    // Set to empty if the service is associated
    mockServiceRepo.updateAssocServiceIdToNull(allIds);
    // Set to empty if the apis is associated
    mockApisRepo.updateServiceIdAndApisIdToNullByServiceIdIn(allIds);
    // Delete service and service, Include logic deleted project
    servicesRepo.deleteAllByIdIn(allIds);
    // Delete service schema
    servicesSchemaCmd.deleteByServiceIdIn(allIds);
    // Delete service Components
    servicesCompCmd.deleteByServiceIdIn(allIds);
    // Delete service and service auth
    servicesAuthCmd.deleteAllByProject(allIds);
    // Delete service sync sync
    servicesSyncCmd.deleteAllByServices(allIds);
    // Delete service indicator
    indicatorPerfCmd.deleteAllByTarget(allIds, SERVICE);
    indicatorStabilityCmd.deleteAllByTarget(allIds, SERVICE);
    // NOOP:: Do not delete associated test tasks
    List<Long> apisIds = apisRepo.findAll0IdByServiceIdIn(allIds);
    if (isNotEmpty(apisIds)) {
      apisCmd.delete0(apisIds);
    }
    // NOOP:: Delete projects and components ref -> Deleted in apisCmd.delete0(apisIds)
    // Delete apis design
    apisDesignCmd.deleteByServiceIdIn(apisIds);
  }

  @Override
  protected BaseRepository<Services, Long> getRepository() {
    return this.servicesRepo;
  }

}
