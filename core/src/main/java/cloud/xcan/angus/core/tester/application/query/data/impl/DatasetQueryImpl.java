package cloud.xcan.angus.core.tester.application.query.data.impl;

import static cloud.xcan.angus.api.commonlink.TesterConstant.DEFAULT_DATASET_REVIEW_ROWS;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.DatasetConverter.toAngusDataset;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.VARIABLE_NAME_REPEATED_T;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.data.DatasetQuery;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetRepo;
import cloud.xcan.angus.extraction.DatasetExtractor;
import cloud.xcan.angus.model.element.ActionOnEOF;
import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.CircularList;
import cloud.xcan.angus.spec.utils.ReadDynamicValue;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class DatasetQueryImpl implements DatasetQuery {

  @Resource
  private DatasetRepo datasetRepo;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private DatasetExtractor defaultDatasetExtractor;

  @Override
  public Dataset detail(Long id) {
    return new BizTemplate<Dataset>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Dataset process() {
        return checkAndFind(id);
      }
    }.execute();
  }

  @Override
  public LinkedHashMap<String, List<String>> valuePreview(Long id, String name,
      List<DatasetParameter> parameters, DefaultExtraction extraction, Long rowNum) {
    return new BizTemplate<LinkedHashMap<String, List<String>>>() {
      Dataset datasetDb;

      @Override
      protected void checkParams() {
        if (nonNull(id)) {
          datasetDb = checkAndFind(id);
        }
      }

      @Override
      protected LinkedHashMap<String, List<String>> process() {
        cloud.xcan.angus.model.element.dataset.Dataset angusDataset = nonNull(datasetDb)
            ? toAngusDataset(datasetDb) : toAngusDataset(name, parameters, extraction);
        LinkedHashMap<String, List<String>> previewValues = new LinkedHashMap<>();
        try {
          Map<String, ReadDynamicValue> dynamicValueMap
              = defaultDatasetExtractor.extract(angusDataset, ActionOnEOF.STOP_THREAD);
          Long finalRowNum = nullSafe(rowNum, DEFAULT_DATASET_REVIEW_ROWS);
          for (DatasetParameter parameter : datasetDb.getParameters()) {
            List<String> values = new ArrayList<>();
            ReadDynamicValue readDynamicValue = dynamicValueMap.get(parameter.getName());
            if (nonNull(readDynamicValue)) {
              for (long i = 0; i < finalRowNum; i++) {
                String value = readDynamicValue.readNext();
                if (!CircularList.END_CHARS.equals(value)) {
                  values.add(value);
                }
              }
            }
            previewValues.put(parameter.getName(), values);
          }
          return previewValues;
        } catch (Exception e) {
          throw ProtocolException.of(e.getMessage());
        }
      }
    }.execute();
  }

  /**
   * Note: Read the value only once.
   */
  @Override
  public Map<String, String> valuePreview(List<Long> ids) {
    return new BizTemplate<Map<String, String>>() {
      List<Dataset> datasetsDb;

      @Override
      protected void checkParams() {
        datasetsDb = checkAndFind(ids);
      }

      @Override
      protected Map<String, String> process() {
        Map<String, String> valueMap = new LinkedHashMap<>();
        for (Dataset datasetDb : datasetsDb) {
          try {
            cloud.xcan.angus.model.element.dataset.Dataset angusDataset = toAngusDataset(datasetDb);
            Map<String, ReadDynamicValue> dynamicValueMap
                = defaultDatasetExtractor.extract(angusDataset, ActionOnEOF.STOP_THREAD);
            for (DatasetParameter parameter : datasetDb.getParameters()) {
              ReadDynamicValue readDynamicValue = dynamicValueMap.get(parameter.getName());
              if (nonNull(readDynamicValue)) {
                String value = readDynamicValue.readNext();
                if (!CircularList.END_CHARS.equals(value)) {
                  valueMap.put(parameter.getName(), value);
                }
              }
            }
          } catch (Exception e) {
            throw ProtocolException.of(e.getMessage());
          }
        }
        return valueMap;
      }
    }.execute();
  }

  @Override
  public Page<Dataset> find(GenericSpecification<Dataset> spec, Pageable pageable) {
    return new BizTemplate<Page<Dataset>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Dataset> process() {
        return datasetRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public List<Dataset> findByProjectAndIds(Long projectId, @Nullable LinkedHashSet<Long> ids) {
    return new BizTemplate<List<Dataset>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<Dataset> process() {
        return isEmpty(ids) ? datasetRepo.findByProjectId(projectId)
            : datasetRepo.findByProjectIdAndIdIn(projectId, ids);
      }
    }.execute();
  }

  @Override
  public Dataset checkAndFind(Long id) {
    return datasetRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Dataset"));
  }

  @Override
  public List<Dataset> checkAndFind(Collection<Long> ids) {
    List<Dataset> datasets = datasetRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(datasets), ids.iterator().next(), "Dataset");
    if (ids.size() != datasets.size()) {
      for (Dataset dataset : datasets) {
        assertResourceNotFound(ids.contains(dataset.getId()), dataset.getId(), "Dataset");
      }
    }
    return datasets;
  }

  @Override
  public List<Dataset> checkAndFindByName(Long projectId, List<String> names) {
    List<Dataset> datasets = datasetRepo.findByProjectIdAndNameIn(projectId, names);
    assertResourceNotFound(isNotEmpty(datasets), names.iterator().next(), "Dataset");
    if (names.size() != datasets.size()) {
      for (Dataset dataset : datasets) {
        assertResourceNotFound(names.contains(dataset.getName()), dataset.getName(), "Dataset");
      }
    }
    return datasets;
  }

  @Override
  public void checkTenantQuota(int inc) {
    long count = datasetRepo.countByTenantId(getTenantId());
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterDataset,
        Collections.singleton(getTenantId()), count + inc);
  }

  @Override
  public void checkRequiredParam(Dataset dataset) {
    if (dataset.getExtracted() && isNull(dataset.getExtraction())) {
      throw ProtocolException.of(PARAM_MISSING_T, new Object[]{"extraction"});
    }

    if (dataset.getExtracted() && dataset.getExtraction().getMethod().needExpression()
        && isEmpty(dataset.getExtraction().getExpression())) {
      throw ProtocolException.of(PARAM_MISSING_T, new Object[]{"expression"});
    }
  }

  @Override
  public void checkAddNameExists(Dataset dataset) {
    if (datasetRepo.existsByProjectIdAndName(dataset.getProjectId(), dataset.getName())) {
      throw ResourceExisted.of(VARIABLE_NAME_REPEATED_T, new Object[]{dataset.getName()});
    }
  }

  @Override
  public void checkUpdateNameExists(Dataset dataset) {
    if (datasetRepo.existsByProjectIdAndNameAndIdNot(dataset.getProjectId(),
        dataset.getName(), dataset.getId())) {
      throw ResourceExisted.of(VARIABLE_NAME_REPEATED_T, new Object[]{dataset.getName()});
    }
  }

  @Override
  public void setSafeCloneName(Dataset dataset) {
    String saltName = randomAlphanumeric(3);
    String clonedName = datasetRepo.existsByName(dataset.getName() + "-Copy")
        ? dataset.getName() + "-Copy." + saltName : dataset.getName() + "-Copy";
    clonedName = clonedName.length() > MAX_NAME_LENGTH ? clonedName.substring(0,
        MAX_NAME_LENGTH - 3) + saltName : clonedName;
    dataset.setName(clonedName);
  }

}
