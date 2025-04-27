package cloud.xcan.angus.core.tester.application.cmd.data.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.DATASET;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_DATASET_FILE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.DatasetConverter.toDataset;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.DATASET_IS_NOT_VALID;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.CLONE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.IMPORT;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.readSample;
import static cloud.xcan.angus.core.tester.infra.util.ServicesFileUtils.getImportTmpPath;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.JsonUtils.isJson;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.data.DatasetCmd;
import cloud.xcan.angus.core.tester.application.converter.DatasetConverter;
import cloud.xcan.angus.core.tester.application.query.data.DatasetQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetRepo;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTargetRepo;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.parser.AngusParser;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.spec.experimental.IdKey;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Dataset dataset) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected void checkParams() {
        // Check the member permissions
        projectMemberQuery.checkMember(getUserId(), dataset.getProjectId());
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
        activityCmd.addAll(toActivities(DATASET, clonedDatasets, CLONE,
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
        projectMemberQuery.checkMember(getUserId(), projectId);
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
            throw SysException.of("Exception reading import file, cause: "
                + e.getMessage(), ExceptionLevel.ERROR);
          }
        }

        List<Dataset> datasets = parseVariablesFromScript(projectId,
            strategyWhenDuplicated, finalContent);
        List<IdKey<Long, Object>> idKeys = batchInsert(datasets, "name");

        // Save import dataset activities
        activityCmd.addAll(toActivities(DATASET, datasets, IMPORT,
            datasets.stream().map(s -> new Object[]{s.getName()}).collect(Collectors.toList())));
        return idKeys;
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
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        URL resourceUrl = this.getClass().getResource("/samples/data/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_DATASET_FILE);
        String content = readSample(Objects.requireNonNull(resourceUrl), SAMPLE_DATASET_FILE);
        List<Dataset> datasets = parseVariablesFromScript(projectId,
            StrategyWhenDuplicated.IGNORE, content);
        for (Dataset dataset : datasets) {
          dataset.setProjectId(projectId);
        }
        return batchInsert(datasets, "name");
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

        activityCmd.addAll(toActivities(DATASET, datasetsDb, ActivityType.DELETED));
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
      throw ProtocolException.of("Parsing uploaded content failed, cause: {}",
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
