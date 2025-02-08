package cloud.xcan.sdf.core.angustester.application.cmd.data.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.DATASET;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.SAMPLE_DATASET_FILE;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.SAMPLE_VARIABLE_FILE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.DatasetConverter.toDataset;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.DATASET_IS_NOT_VALID;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.CLONE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.IMPORT;
import static cloud.xcan.sdf.core.angustester.infra.util.ServicesFileUtils.getImportTmpPath;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.sdf.spec.utils.JsonUtils.isJson;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.StreamUtils.copyToString;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.parser.AngusParser;
import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.commonlink.user.User;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.api.message.CommSysException;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.data.DatasetCmd;
import cloud.xcan.sdf.core.angustester.application.converter.DatasetConverter;
import cloud.xcan.sdf.core.angustester.application.query.data.DatasetQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetRepo;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetTargetRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.Assert;
import cloud.xcan.sdf.spec.experimental.IdKey;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Biz
public class DatasetCmdImpl extends CommCmd<Dataset, Long> implements DatasetCmd {

  @Resource
  private DatasetRepo datasetRepo;

  @Resource
  private DatasetTargetRepo datasetTargetRepo;

  @Resource
  private DatasetQuery datasetQuery;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Resource
  private UserManager userManager;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Dataset dataset) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected void checkParams() {
        // Check the member permissions
        projectMemberQuery.checkMember(dataset.getProjectId(), getUserId());
        // Check the required parameters
        datasetQuery.checkRequiredParam(dataset);
        // Check the name is not repeated
        datasetQuery.checkAddNameExists(dataset);
        // Check the tenant dataset quota
        datasetQuery.checkTenantQuota(1);
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKeys = insert(dataset, "name");

        activityCmd.add(toActivity(DATASET, dataset, ActivityType.CREATED));
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Dataset dataset) {
    new BizTemplate<Void>() {
      Dataset datasetDb;

      @Override
      protected void checkParams() {
        // Check and find dataset
        datasetDb = datasetQuery.checkAndFind(dataset.getId());
        // Safe project id
        dataset.setProjectId(datasetDb.getProjectId());
        // Check the name is not repeated
        if (nonNull(dataset.getName())) {
          datasetQuery.checkUpdateNameExists(dataset);
        }
      }

      @Override
      protected Void process() {
        datasetRepo.save(copyPropertiesIgnoreNull(dataset, datasetDb, "projectId"));

        activityCmd.add(toActivity(DATASET, datasetDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Dataset dataset) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(dataset.getId())) {
          return add(dataset);
        }

        update(dataset);
        return new IdKey<Long, Object>().setId(dataset.getId());
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> clone(HashSet<Long> ids) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      List<Dataset> datasetsDb;

      @Override
      protected void checkParams() {
        // Check the datasets exists
        datasetsDb = datasetQuery.checkAndFind(ids);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<Dataset> clonedDatasets = new ArrayList<>();
        for (Dataset datasetDb : datasetsDb) {
          Dataset clonedDataset = DatasetConverter.toCloneDataset(datasetDb);
          datasetQuery.setSafeCloneName(clonedDataset);
          clonedDatasets.add(clonedDataset);
        }

        List<IdKey<Long, Object>> idKeys = batchInsert(clonedDatasets, "name");

        // Add clone activities
        activityCmd.batchAdd(toActivities(DATASET, clonedDatasets, CLONE,
            datasetsDb.stream().map(s -> new Object[]{s.getName()}).collect(Collectors.toList())));
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> imports(Long projectId,
      StrategyWhenDuplicated strategyWhenDuplicated, String content, MultipartFile file) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // Check the member permissions
        projectMemberQuery.checkMember(projectId, getUserId());
        // Check the upload content is required
        assertTrue(isNotEmpty(content) || nonNull(file), "Upload file is required");
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        String finalContent = content;

        // Read from upload file
        if (isEmpty(content)) {
          String srcFileName = file.getOriginalFilename();
          File tmpPath = getImportTmpPath(ApiImportSource.ANGUS, srcFileName);
          File importFile = new File(tmpPath.getPath() + File.separator + srcFileName);
          try {
            file.transferTo(importFile);
            finalContent = FileUtil.readAsString(importFile);
          } catch (IOException e) {
            log.error("Exception reading import file", e);
            throw CommSysException.of("Exception reading import file, cause: " + e.getMessage(),
                ExceptionLevel.ERROR);
          }
        }

        List<Dataset> datasets = parseVariablesFromScript(projectId,
            strategyWhenDuplicated, finalContent);
        List<IdKey<Long, Object>> idKeys = batchInsert(datasets, "name");

        // Save import dataset activities
        activityCmd.batchAdd(toActivities(DATASET, datasets, IMPORT,
            datasets.stream().map(s -> new Object[]{s.getName()}).collect(Collectors.toList())));
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Check the project exists
        projectDb = projectQuery.checkAndFind(projectId);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        String content = parseSampleDataset();
        List<Dataset> datasets = parseVariablesFromScript(projectId,
            StrategyWhenDuplicated.IGNORE, content);

        if (!isUserAction()){
          List<User> users = userManager.findByTenantId(getOptTenantId());
          Assert.assertNotEmpty(users, "Tenant users are empty");
          for (Dataset dataset : datasets) {
            dataset.setTenantId(projectDb.getTenantId())
                .setCreatedBy(users.get(0).getId()).setLastModifiedBy(users.get(0).getId());
          }
        }

        return batchInsert(datasets, "name");
      }

      private String parseSampleDataset() {
        try {
          URL resourceUrl = this.getClass().getResource("/samples/dataset/"
              + getDefaultLanguage().getValue() + "/" + SAMPLE_DATASET_FILE);
          assert resourceUrl != null;
          return copyToString(resourceUrl.openStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
          throw CommSysException.of("Couldn't read sample file " + SAMPLE_DATASET_FILE,
              e.getMessage());
        }
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        List<Dataset> datasetsDb = datasetRepo.findAllById(ids);

        if (isEmpty(datasetsDb)) {
          return null;
        }

        datasetRepo.deleteByIdIn(ids);
        datasetTargetRepo.deleteByDatasetIdIn(ids);

        activityCmd.batchAdd(toActivities(DATASET, datasetsDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  private @NotNull List<Dataset> parseVariablesFromScript(Long projectId,
      StrategyWhenDuplicated strategyWhenDuplicated, String finalContent) {
    // Parse angus datasets script
    List<cloud.xcan.angus.model.element.dataset.Dataset> validDatasets = null;
    List<cloud.xcan.angus.model.element.dataset.Dataset> parsedVariables = null;
    try {
      parsedVariables = isJson(finalContent) ? AngusParser.JSON_MAPPER.readValue(finalContent,
          new TypeReference<List<cloud.xcan.angus.model.element.dataset.Dataset>>() {
          }) : AngusParser.YAML_MAPPER.readValue(finalContent,
          new TypeReference<List<cloud.xcan.angus.model.element.dataset.Dataset>>() {
          });
    } catch (Exception e) {
      throw CommProtocolException.of("Parsing uploaded content failed, cause: {}",
          e.getMessage());
    }

    validDatasets = parsedVariables.stream().filter(x -> isNotEmpty(x.getName())
        && isNotEmpty(x.getParameters())).collect(Collectors.toList());
    assertNotEmpty(validDatasets, DATASET_IS_NOT_VALID);

    Set<String> names = validDatasets.stream()
        .map(cloud.xcan.angus.model.element.dataset.Dataset::getName)
        .collect(Collectors.toSet());
    Set<String> existedNames = datasetRepo.findNamesByProjectIdAndNameIn(projectId, names);
    if (strategyWhenDuplicated.isCover() && isNotEmpty(existedNames)) {
      datasetRepo.deleteByProjectIdAndNameIn(projectId, existedNames);
    } else {
      validDatasets = validDatasets.stream().filter(x -> !existedNames.contains(x.getName()))
          .collect(Collectors.toList());
    }

    assertNotEmpty(validDatasets, DATASET_IS_NOT_VALID);

    // Save final valid datasets
    return validDatasets.stream().map(
        x -> toDataset(projectId, x)).collect(Collectors.toList());
  }

  @Override
  protected BaseRepository<Dataset, Long> getRepository() {
    return this.datasetRepo;
  }
}
