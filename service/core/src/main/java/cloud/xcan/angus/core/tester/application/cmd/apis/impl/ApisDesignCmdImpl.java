package cloud.xcan.angus.core.tester.application.cmd.apis.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API_DESIGN;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ApisDesignConverter.assocToDomain;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_DESIGN_SERVICE_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_OAS_DESIGN_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignSource.SYNCHRONOUS_SERVICE;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.convertImportFile;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.writeExportFile;
import static cloud.xcan.angus.spec.experimental.StandardCharsets.UTF_8;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isBlank;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisDesignCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesSchemaCmd;
import cloud.xcan.angus.core.tester.application.converter.ApisDesignConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisDesignQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSchemaQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesign;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignRepo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignSource;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.utils.FileUtils;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command implementation for managing API design documents.
 * <p>
 * Provides methods for adding, updating, cloning, associating, generating, importing, exporting, and deleting API designs.
 * Handles permission checks, content validation, and activity logging.
 */
@Biz
public class ApisDesignCmdImpl extends CommCmd<ApisDesign, Long> implements ApisDesignCmd {

  @Resource
  private ApisDesignRepo apisDesignRepo;
  @Resource
  private ApisDesignInfoRepo apisDesignInfoRepo;
  @Resource
  private ApisDesignQuery apisDesignQuery;
  @Resource
  private ServicesCmd servicesCmd;
  @Resource
  private ServicesSchemaCmd servicesSchemaCmd;
  @Resource
  private ServicesSchemaQuery servicesSchemaQuery;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ActivityCmd activityCmd;

  /**
   * Add a new API design document.
   * <p>
   * Validates project membership, inserts the design, and logs the creation activity.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> add(ApisDesign design) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(getUserId(), design.getProjectId());
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKeys = add0(design);
        activityCmd.add(toActivity(API_DESIGN, design, ActivityType.CREATED));
        return idKeys;
      }
    }.execute();
  }

  /**
   * Add a new API design document without activity logging.
   * <p>
   * Directly inserts the design.
   */
  @Override
  public IdKey<Long, Object> add0(ApisDesign design) {
    return insert(design);
  }

  /**
   * Update the name of an API design document.
   * <p>
   * Validates permission, updates the name, and logs the activity.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void updateName(Long id, String name) {
    new BizTemplate<Void>() {
      ApisDesignInfo designDb;

      @Override
      protected void checkParams() {
        // Check the design exists
        designDb = apisDesignQuery.checkAndFindInfo(id);
        // Check the project member permission
        projectMemberQuery.checkMember(getUserId(), designDb.getProjectId());
      }

      @Override
      protected Void process() {
        if (!designDb.getName().equals(name)) {
          designDb.setName(name);
          apisDesignInfoRepo.save(designDb);

          activityCmd.add(toActivity(API_DESIGN, designDb, ActivityType.NAME_UPDATED));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Replace the OpenAPI content of a design document.
   * <p>
   * Validates permission, updates content, and logs the activity.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void replaceContent(Long id, String openapi) {
    new BizTemplate<Void>() {
      ApisDesign designDb;

      @Override
      protected void checkParams() {
        // Check the design exists
        designDb = apisDesignQuery.checkAndFind(id);
        // Check the project member permission
        projectMemberQuery.checkMember(getUserId(), designDb.getProjectId());
      }

      @Override
      protected Void process() {
        designDb.setReleased(false);
        OpenAPI osa = servicesSchemaQuery.checkAndGetApisParser(ApiImportSource.OPENAPI)
            .parse(openapi);
        designDb.setOpenapi(Json31.pretty(osa));
        apisDesignRepo.save(designDb);

        activityCmd.add(toActivity(API_DESIGN, designDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Release an API design document.
   * <p>
   * Validates permission and content, releases the design, and logs the activity.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void release(Long id) {
    new BizTemplate<Void>() {
      ApisDesign designDb;

      @Override
      protected void checkParams() {
        // Check the design exists
        designDb = apisDesignQuery.checkAndFind(id);
        // Check the project member permission
        projectMemberQuery.checkMember(getUserId(), designDb.getProjectId());
        // Check the released content is not empty
        assertNotEmpty(designDb.getOpenapi(), APIS_OAS_DESIGN_NOT_FOUND);
      }

      @Override
      protected Void process() {
        if (designDb.hasLatestContent()) {
          servicesSchemaCmd.openapiReplace(designDb.getDesignSourceId(), false, false,
              designDb.getOpenapi(), StrategyWhenDuplicated.COVER, true, ApiSource.EDITOR,
              ApiImportSource.OPENAPI, true, null);
        }
        designDb.setReleased(true);
        apisDesignRepo.save(designDb);

        activityCmd.add(toActivity(API_DESIGN, designDb, ActivityType.RELEASE));
        return null;
      }
    }.execute();
  }

  /**
   * Clone an API design document.
   * <p>
   * Validates permission, clones the design, and logs the activity.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> clone(Long id) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ApisDesign designDb;

      @Override
      protected void checkParams() {
        // Check the design exists
        designDb = apisDesignQuery.checkAndFind(id);
        // Check the project member permission
        projectMemberQuery.checkMember(getUserId(), designDb.getProjectId());
        // Check the view services permission
        if (designDb.getDesignSource().isSynchronousService()) {
          servicesAuthQuery.checkViewAuth(getUserId(), designDb.getDesignSourceId());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        // The service is not cloned
        ApisDesign newDesign = ApisDesignConverter.toClone(designDb);
        IdKey<Long, Object> idKey = insert(newDesign);

        activityCmd.add(toActivity(API_DESIGN, designDb, ActivityType.CLONE));
        return idKey;
      }
    }.execute();
  }

  /**
   * Associate a service with an API design document.
   * <p>
   * Validates permission, checks for duplicates, associates the service, and logs the activity.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void servicesAssociate(Long serviceId) {
    new BizTemplate<Void>() {
      Services servicesDb;

      @Override
      protected void checkParams() {
        // Check and Find
        servicesDb = servicesQuery.checkAndFind(serviceId);
        // Check duplicate association
        apisDesignQuery.checkServiceExisted(serviceId);
        // Check the view services permission
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected Void process() {
        ApisDesign design = assocToDomain(servicesDb);
        add0(design);
        activityCmd.add(toActivity(API_DESIGN, design, ActivityType.CREATED));
        return null;
      }
    }.execute();
  }

  /**
   * Generate a service from an API design document.
   * <p>
   * Validates permission, generates the service, and logs the activity.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void servicesGenerate(Long id) {
    new BizTemplate<Void>() {
      ApisDesign designDb;

      @Override
      protected void checkParams() {
        // Check the design exists
        designDb = apisDesignQuery.checkAndFind(id);
        // Check duplicate generate
        if (nonNull(designDb.getDesignSourceId())) {
          Services servicesDb = servicesQuery.checkAndFind(designDb.getDesignSourceId());
          assertResourceExisted(designDb.getDesignSourceId(), APIS_DESIGN_SERVICE_EXISTED_T,
              new Object[]{servicesDb.getName()});
        }
        // Check the project member permission
        projectMemberQuery.checkMember(getUserId(), designDb.getProjectId());
      }

      @Override
      protected Void process() {
        if (designDb.getDesignSource().isSynchronousService()) {
          return null;
        }

        Services services = new Services()
            .setProjectId(designDb.getProjectId()).setName(designDb.getName())
            .setAuth(false).setStatus(ApiStatus.UNKNOWN).setSource(ApiSource.EDITOR);
        servicesCmd.add(services, false);

        if (isEmpty(designDb.getOpenapi())) {
          servicesSchemaCmd.init(services);
        } else {
          OpenAPI openAPI =
              servicesSchemaQuery.checkAndGetApisParser(ApiImportSource.OPENAPI)
                  .parse(designDb.getOpenapi());
          servicesSchemaCmd.init(services, openAPI);
          if (isNotEmpty(openAPI.getPaths())) {
            servicesSchemaCmd.openapiReplace(services.getId(), true, false,
                designDb.getOpenapi(), StrategyWhenDuplicated.COVER, true,
                ApiSource.EDITOR, ApiImportSource.OPENAPI, true, null);
          }
        }

        designDb.setDesignSource(SYNCHRONOUS_SERVICE).setDesignSourceId(services.getId());
        apisDesignRepo.save(designDb);

        activityCmd.add(toActivity(SERVICE, services, ActivityType.CREATED));
        return null;
      }
    }.execute();
  }

  /**
   * Import an API design document from content or file.
   * <p>
   * Validates permission, parses content, inserts the design, and logs the import activity.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public IdKey<Long, Object> imports(Long projectId, String name, String content,
      MultipartFile file) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(projectId, getUserId());
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNotEmpty(content)) {
          OpenAPI openApi = servicesSchemaQuery.checkAndGetApisParser(
              ApiImportSource.OPENAPI).parse(content);
          return addClone(openApi, projectId, name);
        }

        if (nonNull(file)) {
          try {
            File importFile = convertImportFile(file);
            OpenAPI openApi = servicesSchemaQuery.checkAndGetApisParser(
                ApiImportSource.OPENAPI).parse(FileUtils.readFileToString(importFile, UTF_8));
            return addClone(openApi, projectId, name);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
        return null;
      }
    }.execute();
  }

  /**
   * Delete a batch of API design documents.
   * <p>
   * Validates permission, deletes designs, and logs the delete activity.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public void delete(HashSet<Long> ids) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        List<ApisDesignInfo> designs = apisDesignQuery.findByIds(ids);
        for (ApisDesignInfo design : designs) {
          if (design.getDesignSource().isSynchronousService()
              && nonNull(design.getDesignSourceId())) {
            servicesAuthQuery.checkDeleteAuth(getUserId(), design.getDesignSourceId());
          } else {
            projectMemberQuery.checkMember(design.getProjectId(), getUserId());
          }
        }
        apisDesignInfoRepo.deleteAll(designs);

        activityCmd.addAll(toActivities(API_DESIGN, designs, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Export an API design document to a file.
   * <p>
   * Validates permission, writes content to file, and returns the file.
   */
  @Transactional(rollbackOn = Exception.class)
  @Override
  public File export(Long id, SchemaFormat format) {
    return new BizTemplate<File>() {
      ApisDesign designDb;

      @Override
      protected void checkParams() {
        // Check the design exists
        designDb = apisDesignQuery.checkAndFind(id);
        // Check the project member permission
        projectMemberQuery.checkMember(getUserId(), designDb.getProjectId());
      }

      @Override
      protected File process() {
        // Write exported OpenAPI to file
        String content = "";
        if (isBlank(designDb.getOpenapi()) && designDb.getDesignSource().isSynchronousService()) {
          content = servicesSchemaQuery.openapiDetail(designDb.getDesignSourceId(), null, format,
              false, false);
        } else {
          if (isNotBlank(designDb.getOpenapi())) {
            content = isNotEmpty(designDb.getOpenapi()) && format.isYaml()
                ? Yaml31.pretty(servicesSchemaQuery.checkAndGetApisParser(
                ApiImportSource.OPENAPI).parse(designDb.getOpenapi()))
                : designDb.getOpenapi();
          }
        }
        return writeExportFile(designDb.getName() + "." + format.name(), content);
      }
    }.execute();
  }

  /**
   * Delete all API design documents by service IDs.
   * <p>
   * Physically deletes all designs for the given services.
   */
  @Override
  public void deleteByServiceIdIn(Collection<Long> serviceIds) {
    apisDesignRepo.deleteByDesignSourceIdIn(
        SYNCHRONOUS_SERVICE.getValue(), serviceIds);
  }

  private IdKey<Long, Object> addClone(OpenAPI openApi, Long projectId, String name) {
    ApisDesign design = new ApisDesign().setProjectId(projectId)
        .setName(name).setReleased(false).setOpenapiSpecVersion(openApi.getOpenapi())
        .setOpenapi(Json31.pretty(openApi))
        .setDesignSource(ApisDesignSource.FILE_IMPORT);

    IdKey<Long, Object> idKey = add0(design);
    activityCmd.add(toActivity(API_DESIGN, design, ActivityType.IMPORT));
    return idKey;
  }

  /**
   * Get the repository for ApisDesign entity.
   * <p>
   * @return the ApisDesignRepo instance
   */
  @Override
  protected BaseRepository<ApisDesign, Long> getRepository() {
    return apisDesignRepo;
  }
}
