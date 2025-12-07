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
import cloud.xcan.angus.core.tester.application.cmd.config.IndicatorPerfCmd;
import cloud.xcan.angus.core.tester.application.cmd.config.IndicatorStabilityCmd;
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
import cloud.xcan.angus.core.tester.infra.util.BIDUtils;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils.BIDKey;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.spec.experimental.IdKey;
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

/**
 * Implementation of service command operations for comprehensive service management.
 *
 * <p>This class provides extensive functionality for managing services, including
 * creation, modification, cloning, import/export, and lifecycle management.</p>
 *
 * <p>It handles the complete lifecycle of services from creation to deletion, including
 * authorization management, schema management, component management, and activity logging for audit
 * purposes.</p>
 *
 * <p>The implementation supports various import sources (OpenAPI, Swagger) and provides
 * comprehensive export functionality with multiple format options.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Service CRUD operations with comprehensive validation</li>
 *   <li>Import/export functionality with multiple format support</li>
 *   <li>Service cloning with complete data replication</li>
 *   <li>Creator authorization and permission management</li>
 *   <li>Activity logging for audit trails</li>
 *   <li>Schema and component synchronization</li>
 * </ul></p>
 */
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

  /**
   * Adds a new service with comprehensive validation and setup.
   *
   * <p>This method performs extensive validation including project membership,
   * service quota checks, and name uniqueness validation.</p>
   *
   * <p>It automatically sets up creator authorization and optionally initializes
   * the service schema, and logs the creation activity for audit purposes.</p>
   *
   * @param services   the service to add
   * @param initSchema whether to initialize the service schema
   * @return the ID key of the created service
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Services services, boolean initSchema) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify project membership for the current user
        projectMemberQuery.checkMember(getUserId(), services.getProjectId());

        // Check service quota availability
        servicesQuery.checkQuota(1);

        // Verify service name uniqueness within the project
        servicesQuery.checkNameExists(services.getProjectId(), services.getName());
      }

      @Override
      protected IdKey<Long, Object> process() {
        services.setId(BIDUtils.getId(BIDKey.serviceId));
        IdKey<Long, Object> idKey = insert(services);

        // Initialize creator authorization
        servicesAuthCmd.addCreatorAuth(idKey.getId(), getAuthCreatorIds());

        // Initialize service schema if requested
        if (initSchema) {
          servicesSchemaCmd.init(services);
        }

        // Log service creation activity
        activityCmd.add(toActivity(SERVICE, services, ActivityType.CREATED));
        return idKey;
      }

      /**
       * Gets the set of creator IDs for authorization setup.
       *
       * <p>This method collects the current user ID and the original service creator ID
       * to establish proper authorization for the newly created service.</p>
       *
       * @return set of user IDs to be granted creator permissions
       */
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

  /**
   * Renames a service with validation and permission checks.
   *
   * <p>This method allows renaming a service while ensuring proper validation
   * including service existence, publication status, and modification permissions.</p>
   *
   * <p>Published services have additional restrictions on modifications,
   * and the method logs the rename activity for audit purposes.</p>
   *
   * @param id   the ID of the service to rename
   * @param name the new name for the service
   * @throws IllegalArgumentException if validation fails or service not found
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void rename(Long id, String name) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify the service exists
        serviceDb = servicesQuery.checkAndFind(id);

        if (!serviceDb.getName().equals(name)) {
          // Verify published services can be modified
          servicesQuery.checkPublishStatus(serviceDb);
          // Verify user has modification permissions
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
   * Updates the status of a service and all its associated APIs.
   *
   * <p>This method controls the publication status of a service. When a service
   * is published, all APIs under that service are also published. Services allow duplicate
   * publications for flexibility.</p>
   *
   * <p>The method performs appropriate permission validation based on the
   * target status and logs the status change activity.</p>
   *
   * @param serviceId the ID of the service to update
   * @param status    the new API status to set
   * @throws IllegalArgumentException if validation fails or service not found
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void statusUpdate(Long serviceId, ApiStatus status) {
    new BizTemplate<Void>() {
      Services serviceDb = null;

      @Override
      protected void checkParams() {
        // Verify the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);

        // Verify user has appropriate permissions based on status change
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

  /**
   * Clones a service with all its associated data.
   *
   * <p>This method creates a complete copy of a service including all APIs,
   * schemas, components, and authorizations. It performs comprehensive data replication while
   * maintaining proper relationships.</p>
   *
   * <p>The cloned service gets a new unique name and maintains all
   * the original service's functionality and structure.</p>
   *
   * @param id the ID of the service to clone
   * @throws IllegalArgumentException if validation fails or service not found
   */
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

      /**
       * Performs the actual service and API cloning operation.
       *
       * <p>This method handles the complete cloning process including service creation,
       * API replication, authorization setup, and schema/component copying.</p>
       *
       * @param serviceDb the original service to clone from
       */
      private void cloneServiceAndApis(Services serviceDb) {
        // Create cloned service with new ID
        Services clonedService = cloneService(uidGenerator.getUID(), serviceDb);
        servicesQuery.setSafeCloneName(clonedService);

        // TODO: Query service and copy sync and schemas

        // Set up creator authorization IDs
        Set<Long> creatorIds = new HashSet<>();
        creatorIds.add(getUserId());
        creatorIds.add(serviceDb.getCreatedBy());

        // Clone associated APIs
        List<Apis> apisDb = apisRepo.findByServiceId(serviceDb.getId());
        if (isNotEmpty(apisDb)) {
          List<Apis> cloneApis = cloneApis(uidGenerator, apisDb, clonedService);
          apisRepo.batchInsert0(cloneApis);

          // Initialize API authorizations
          apisAuthCmd.addCreatorAuth(cloneApis.stream().map(Apis::getId)
              .collect(Collectors.toSet()), creatorIds);
        }

        // Initialize service authorization
        servicesAuthCmd.addCreatorAuth(clonedService.getId(), creatorIds);

        // Save the cloned service
        insert0(clonedService);

        // Clone service schema
        servicesSchemaCmd.clone(serviceDb.getId(), clonedService.getId());

        // Clone service components
        servicesCompCmd.clone(serviceDb.getId(), clonedService.getId());

        // TODO: Clone project sync configuration
        // projectSyncRepo.batchInsert0(Collections.singleton(cloneConfig));
      }
    }.execute();
  }

  /**
   * Imports services from files or content with comprehensive validation.
   *
   * <p>This method supports importing services from various sources including
   * file uploads and direct content input. It handles both new service creation and existing
   * service updates based on the provided parameters.</p>
   *
   * <p>The method supports different import strategies for handling duplicates
   * and optional cleanup of unreferenced components.</p>
   *
   * @param projectId              the ID of the project to import into
   * @param serviceId              the ID of existing service to update, null for new service
   * @param serviceName            the name for the new service (required when serviceId is null)
   * @param importSource           the source type for the import
   * @param strategyWhenDuplicated strategy for handling duplicate components
   * @param deleteWhenNotExisted   whether to delete components not in the import
   * @param file                   the uploaded file to import from
   * @param content                the direct content to import from
   * @return the ID key of the imported/updated service
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> imports(Long projectId, Long serviceId, String serviceName,
      ApiImportSource importSource, StrategyWhenDuplicated strategyWhenDuplicated,
      Boolean deleteWhenNotExisted, MultipartFile file, String content) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify either file or content is provided
        assertTrue(nonNull(file) || isNotEmpty(content),
            "Parameter file or content cannot both be empty");

        // Verify project ID is provided when service ID is empty
        assertTrue(nonNull(serviceId) || nonNull(projectId),
            "Parameter projectId is required when serviceId is empty");

        // Handle existing service update
        if (nonNull(serviceId)) {
          // Verify the service exists
          serviceDb = servicesQuery.checkAndFind(serviceId);

          // Verify user has permission to add APIs to the service
          // This applies to services that allow API additions to self-created
          // and authorized projects, and projects without permission restrictions
          servicesAuthQuery.checkAddAuth(getUserId(), serviceId);
        } else {
          // Verify service name is provided for new service creation
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
   * Imports example services into a project.
   *
   * <p>This method imports predefined example services from sample files
   * to help users get started with the system.</p>
   *
   * <p>Note: When API calls are not user-initiated, tenant and user information
   * must be injected into the PrincipalContext for proper authorization.</p>
   *
   * @param projectId the ID of the project to import examples into
   * @return list of ID keys for the imported services
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

  /**
   * Exports services in various formats with comprehensive options.
   *
   * <p>This method supports exporting services in different scopes (service-level
   * or API-level) and multiple formats. It can export all components or only API-specific
   * components based on the configuration.</p>
   *
   * <p>The method handles single file exports and multi-file compression
   * for bulk exports, with proper permission validation.</p>
   *
   * @param exportScope        the scope of the export (service or API level)
   * @param serviceIds         set of service IDs to export
   * @param apisId             set of specific API IDs to export (for API-level scope)
   * @param format             the export format (JSON, YAML, etc.)
   * @param onlyApisComponents whether to export only API components
   * @return the exported file (single file or compressed archive)
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public File exportProject(ServicesExportScope exportScope, Set<Long> serviceIds,
      Set<Long> apisId, SchemaFormat format, boolean onlyApisComponents) {
    return new BizTemplate<File>() {
      List<Services> servicesDb;

      @Override
      protected void checkParams() {
        // Verify API-level export is limited to single service
        assertTrue(exportScope.equals(ServicesExportScope.SERVICE) ||
            serviceIds.size() == 1, "Export by APIS can only have one project");

        // TODO: Add permission check in projectSchemaQuery.schemaDetail()
        servicesAuthQuery.batchCheckPermission(serviceIds, ServicesPermission.EXPORT);

        // Verify services exist and retrieve them
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
                    .toList().toArray(new String[]{}),
                allExportedFiles.stream().map(File::getName)
                    .toList().toArray(new String[]{}), finalFile.getPath());
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
            .stream().map(str -> new Object[]{str}).toList()));
        return finalFile;
      }
    }.execute();
  }

  /**
   * Logically deletes a service with comprehensive cleanup.
   *
   * <p>This method performs a logical deletion of a service, marking it as deleted
   * while preserving the data for potential recovery. It updates the deletion status of associated
   * APIs and moves the service to trash.</p>
   *
   * <p>The method logs the deletion activity for audit purposes and ensures
   * proper cleanup of related data.</p>
   *
   * @param id the ID of the service to delete
   * @throws IllegalArgumentException if validation fails or service not found
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify service exists
        serviceDb = servicesRepo.findById(id).orElse(null);
        if (isNull(serviceDb)) {
          return;
        }
        // Verify user has deletion permissions
        servicesAuthQuery.checkDeleteAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        if (isNull(serviceDb)) {
          return null;
        }

        // Perform logical deletion
        // Mark the service as deleted and update associated APIs accordingly
        ServicesConverter.completeDeleteInfo(serviceDb);
        servicesRepo.save(serviceDb);

        // Update API deletion status
        List<Long> serviceIds = Lists.newArrayList(id);

        // Note: APIs are not deleted when the service is deleted
        apisRepo.updateServiceDeleteStatusByService(serviceIds, true);

        // Move service to trash for potential recovery
        ApisTrash trash = ServicesConverter.toTrash(serviceDb);
        trashApisCmd.add0(Collections.singletonList(trash));

        // Log service deletion activity
        activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Physically deletes services and all associated data.
   *
   * <p>This method performs a complete physical deletion of services and all
   * related data including APIs, schemas, components, authorizations, and performance
   * indicators.</p>
   *
   * <p>Warning: This is a destructive operation that cannot be undone.
   * External calling business logic must ensure proper authorization and data security before
   * invoking this method.</p>
   *
   * @param ids list of service IDs to physically delete
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

  /**
   * Returns the repository instance for this command.
   *
   * @return the services repository
   */
  @Override
  protected BaseRepository<Services, Long> getRepository() {
    return this.servicesRepo;
  }

}
