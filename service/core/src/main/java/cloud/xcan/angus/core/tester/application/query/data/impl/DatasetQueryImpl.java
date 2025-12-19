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
import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.data.DatasetQuery;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetRepo;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetSearchRepo;
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
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of dataset query operations for data management and validation.
 *
 * <p>This class provides comprehensive functionality for querying, validating,
 * and managing datasets including preview generation, quota checking, and name validation.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Dataset detail retrieval and validation</li>
 *   <li>Value preview generation with parameter extraction</li>
 *   <li>Dataset listing with search and pagination</li>
 *   <li>Name existence validation and safe cloning</li>
 *   <li>Tenant quota management and validation</li>
 *   <li>Required parameter validation</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Service
public class DatasetQueryImpl implements DatasetQuery {

  @Resource
  private DatasetRepo datasetRepo;
  @Resource
  private DatasetSearchRepo datasetSearchRepo;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private DatasetExtractor defaultDatasetExtractor;

  /**
   * Retrieves detailed dataset information by ID.
   *
   * <p>This method fetches a dataset by its ID, performing validation
   * to ensure the dataset exists.</p>
   *
   * @param id the dataset ID to retrieve
   * @return the detailed dataset information
   */
  @Override
  public Dataset detail(Long id) {
    return new BizTemplate<Dataset>() {

      @Override
      protected Dataset process() {
        return checkAndFind(id);
      }
    }.execute();
  }

  /**
   * Generates value preview for dataset parameters.
   *
   * <p>This method extracts values from a dataset using the specified parameters
   * and extraction method, returning a preview of the data for validation and testing
   * purposes.</p>
   *
   * @param id         the dataset ID (optional if name and parameters are provided)
   * @param name       the dataset name (used if ID is not provided)
   * @param parameters the dataset parameters
   * @param extraction the extraction method configuration
   * @param rowNum     the number of rows to preview
   * @return map of parameter names to preview values
   */
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
        // Convert to Angus dataset model for extraction
        cloud.xcan.angus.model.element.dataset.Dataset angusDataset = nonNull(datasetDb)
            ? toAngusDataset(datasetDb) : toAngusDataset(name, parameters, extraction);
        LinkedHashMap<String, List<String>> previewValues = new LinkedHashMap<>();
        try {
          // Extract dynamic values from dataset
          Map<String, ReadDynamicValue> dynamicValueMap
              = defaultDatasetExtractor.extract(angusDataset, ActionOnEOF.STOP_THREAD);
          Long finalRowNum = nullSafe(rowNum, DEFAULT_DATASET_REVIEW_ROWS);

          // Generate preview values for each parameter
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
   * Generates value preview for multiple datasets.
   *
   * <p>This method reads one value from each dataset parameter for multiple
   * datasets, providing a quick preview of available data. Note that values are read only once per
   * parameter.</p>
   *
   * @param ids list of dataset IDs to preview
   * @return map of parameter names to single preview values
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
            // Convert to Angus dataset model and extract values
            cloud.xcan.angus.model.element.dataset.Dataset angusDataset = toAngusDataset(datasetDb);
            Map<String, ReadDynamicValue> dynamicValueMap
                = defaultDatasetExtractor.extract(angusDataset, ActionOnEOF.STOP_THREAD);

            // Read one value from each parameter
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

  /**
   * Lists datasets with optional search and pagination.
   *
   * <p>This method retrieves datasets based on specification criteria,
   * supporting both full-text search and standard filtering.</p>
   *
   * @param spec           the specification for filtering datasets
   * @param pageable       the pagination parameters
   * @param fullTextSearch whether to use full-text search
   * @param match          the match fields for full-text search
   * @return paginated list of datasets
   */
  @Override
  public Page<Dataset> list(GenericSpecification<Dataset> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<Dataset>>() {

      @Override
      protected Page<Dataset> process() {
        return fullTextSearch
            ? datasetSearchRepo.find(spec.getCriteria(), pageable, Dataset.class, match)
            : datasetRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * Finds datasets by project ID and optional dataset IDs.
   *
   * <p>This method retrieves datasets for a specific project, optionally
   * filtered by a set of dataset IDs.</p>
   *
   * @param projectId the project ID
   * @param ids       optional set of dataset IDs to filter by
   * @return list of datasets matching the criteria
   */
  @Override
  public List<Dataset> findByProjectAndIds(Long projectId, @Nullable LinkedHashSet<Long> ids) {
    return new BizTemplate<List<Dataset>>() {

      @Override
      protected List<Dataset> process() {
        return isEmpty(ids) ? datasetRepo.findByProjectId(projectId)
            : datasetRepo.findByProjectIdAndIdIn(projectId, ids);
      }
    }.execute();
  }

  /**
   * Validates and retrieves a dataset by ID.
   *
   * <p>This method fetches a dataset by its ID, throwing a ResourceNotFound
   * exception if the dataset does not exist.</p>
   *
   * @param id the dataset ID to check and retrieve
   * @return the dataset if found
   * @throws ResourceNotFound if the dataset is not found
   */
  @Override
  public Dataset checkAndFind(Long id) {
    return datasetRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Dataset"));
  }

  /**
   * Validates and retrieves multiple datasets by IDs.
   *
   * <p>This method fetches multiple datasets by their IDs, ensuring all
   * requested datasets exist and are valid.</p>
   *
   * @param ids collection of dataset IDs to check and retrieve
   * @return list of datasets if all found
   * @throws ResourceNotFound if any dataset is not found
   */
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

  /**
   * Validates and retrieves datasets by project ID and names.
   *
   * <p>This method fetches datasets by their names within a specific project,
   * ensuring all requested datasets exist.</p>
   *
   * @param projectId the project ID
   * @param names     list of dataset names to check and retrieve
   * @return list of datasets if all found
   * @throws ResourceNotFound if any dataset is not found
   */
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

  /**
   * Validates tenant quota for dataset creation.
   *
   * <p>This method checks if the current tenant has sufficient quota
   * to create additional datasets.</p>
   *
   * @param inc the increment amount to check
   * @throws QuotaException if the quota limit would be exceeded
   */
  @Override
  public void checkTenantQuota(int inc) {
    long count = datasetRepo.countByTenantId(getTenantId());
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterDataset,
        Collections.singleton(getTenantId()), count + inc);
  }

  /**
   * Validates required parameters for a dataset.
   *
   * <p>This method checks if the dataset has all required parameters
   * based on its extraction configuration.</p>
   *
   * @param dataset the dataset to validate
   * @throws ProtocolException if required parameters are missing
   */
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

  /**
   * Validates that a dataset name does not already exist in the project.
   *
   * <p>This method checks if a dataset with the same name already exists
   * in the specified project for add operations.</p>
   *
   * @param dataset the dataset to check
   * @throws ResourceExisted if a dataset with the same name already exists
   */
  @Override
  public void checkAddNameExists(Dataset dataset) {
    if (datasetRepo.existsByProjectIdAndName(dataset.getProjectId(), dataset.getName())) {
      throw ResourceExisted.of(VARIABLE_NAME_REPEATED_T, new Object[]{dataset.getName()});
    }
  }

  /**
   * Validates that a dataset name does not already exist in the project for updates.
   *
   * <p>This method checks if a dataset with the same name already exists
   * in the specified project, excluding the current dataset for update operations.</p>
   *
   * @param dataset the dataset to check
   * @throws ResourceExisted if a dataset with the same name already exists
   */
  @Override
  public void checkUpdateNameExists(Dataset dataset) {
    if (datasetRepo.existsByProjectIdAndNameAndIdNot(dataset.getProjectId(),
        dataset.getName(), dataset.getId())) {
      throw ResourceExisted.of(VARIABLE_NAME_REPEATED_T, new Object[]{dataset.getName()});
    }
  }

  /**
   * Sets a safe clone name for a dataset.
   *
   * <p>This method generates a unique name for dataset cloning by appending
   * "-Copy" and optionally a random suffix to ensure uniqueness.</p>
   *
   * @param dataset the dataset to set the clone name for
   */
  @Override
  public void setSafeCloneName(Dataset dataset) {
    String saltName = randomAlphanumeric(3);
    String clonedName = datasetRepo.existsByName(dataset.getName() + "-Copy")
        ? dataset.getName() + "-Copy." + saltName : dataset.getName() + "-Copy";
    // Ensure name length does not exceed maximum limit
    clonedName = clonedName.length() > MAX_NAME_LENGTH ? clonedName.substring(0,
        MAX_NAME_LENGTH - 3) + saltName : clonedName;
    dataset.setName(clonedName);
  }

}
