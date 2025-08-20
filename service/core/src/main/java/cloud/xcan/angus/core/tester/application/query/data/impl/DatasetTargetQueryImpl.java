package cloud.xcan.angus.core.tester.application.query.data.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API_CASE;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.data.DatasetQuery;
import cloud.xcan.angus.core.tester.application.query.data.DatasetTargetQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableTargetQuery;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetRepo;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTarget;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTargetRepo;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of dataset target query operations for dataset-target associations.
 *
 * <p>This class provides functionality for managing relationships between
 * datasets and their target entities, including API cases and other target types.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Dataset retrieval by target associations</li>
 *   <li>Target retrieval by dataset associations</li>
 *   <li>Value preview for target-associated datasets</li>
 *   <li>Target quota validation for dataset associations</li>
 *   <li>Support for API case to API dataset inheritance</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Biz
public class DatasetTargetQueryImpl implements DatasetTargetQuery {

  @Resource
  private DatasetTargetRepo datasetTargetRepo;
  @Resource
  private DatasetRepo datasetRepo;
  @Resource
  private DatasetQuery datasetQuery;
  @Resource
  private VariableTargetQuery variableTargetQuery;
  @Resource
  private CommonQuery commonQuery;

  /**
   * Finds datasets associated with a specific target.
   *
   * <p>This method retrieves all datasets that are associated with the
   * specified target ID and type.</p>
   *
   * @param targetId the target entity ID
   * @param targetType the target entity type
   * @return list of datasets associated with the target
   */
  @Override
  public List<Dataset> findDatasets(Long targetId, String targetType) {
    return new BizTemplate<List<Dataset>>() {

      @Override
      protected List<Dataset> process() {
        // Retrieve dataset IDs associated with the target
        List<Long> datasetIds = datasetTargetRepo.findByDatasetIdByTargetIdAndType(
            targetId, targetType);
        return datasetIds.isEmpty() ? emptyList() : datasetRepo.findAllById(datasetIds);
      }
    }.execute();
  }

  /**
   * Finds datasets associated with multiple targets, including API case inheritance.
   *
   * <p>This method retrieves datasets for multiple targets, with special handling
   * for API cases that inherit datasets from their associated APIs.</p>
   *
   * @param targetIds collection of target IDs
   * @param targetType the target entity type
   * @param caseApiMap mapping of case IDs to API IDs for inheritance
   * @return map of target IDs to their associated datasets
   */
  @Override
  public Map<Long, List<Dataset>> findDatasets(Collection<Long> targetIds, String targetType,
      Map<Long, Long> caseApiMap) {
    return new BizTemplate<Map<Long, List<Dataset>>>() {

      @Override
      protected Map<Long, List<Dataset>> process() {
        Map<Long, List<Dataset>> targetDatasetsMap = new HashMap<>();
        List<DatasetTarget> datasetTargets = datasetTargetRepo.findByTargetIdInAndType(
            targetIds, targetType);

        // Handle API case inheritance from associated APIs
        if (API_CASE.getValue().equals(targetType) && isNotEmpty(caseApiMap)) {
          // Retrieve datasets associated with the APIs
          Map<Long, List<DatasetTarget>> apisDatsetTargetMap = datasetTargetRepo.findByTargetIdInAndType(
                  new HashSet<>(caseApiMap.values()), CombinedTargetType.API.getValue()).stream()
              .collect(Collectors.groupingBy(DatasetTarget::getTargetId));

          if (isNotEmpty(apisDatsetTargetMap)) {
            // Create inherited dataset targets for cases
            for (Long caseId : targetIds) {
              if (caseApiMap.containsKey(caseId)) {
                List<DatasetTarget> targets = apisDatsetTargetMap.get(caseApiMap.get(caseId));
                datasetTargets.addAll(targets.stream().map(x -> {
                  DatasetTarget target = new DatasetTarget();
                  target.setTargetId(caseId);
                  target.setTargetType(API_CASE);
                  target.setDatasetId(x.getTargetId());
                  return target;
                }).toList());
              }
            }
          }
        }

        if (datasetTargets.isEmpty()) {
          return targetDatasetsMap;
        }

        // Retrieve all datasets and build the result map
        List<Dataset> datasets = datasetRepo.findAllById(
            datasetTargets.stream().map(DatasetTarget::getDatasetId)
                .collect(Collectors.toSet()));
        if (datasets.isEmpty()) {
          return targetDatasetsMap;
        }

        // Group dataset IDs by target ID
        Map<Long, List<Long>> caseDatasetIdsMap = datasetTargets.stream()
            .collect(Collectors.groupingBy(DatasetTarget::getTargetId,
                Collectors.mapping(DatasetTarget::getDatasetId, Collectors.toList())));

        // Build final result map
        for (DatasetTarget vt : datasetTargets) {
          List<Long> datasetIds = caseDatasetIdsMap.get(vt.getTargetId());
          if (isEmpty(datasetIds)) {
            targetDatasetsMap.put(vt.getTargetId(),
                datasets.stream().filter(x -> datasetIds.contains(x.getId())).collect(
                    Collectors.toList()));
          }
        }
        return targetDatasetsMap;
      }
    }.execute();
  }

  /**
   * Finds targets associated with a specific dataset.
   *
   * <p>This method retrieves all targets that are associated with the
   * specified dataset, including target name enrichment.</p>
   *
   * @param datasetId the dataset ID
   * @return list of targets associated with the dataset
   */
  @Override
  public List<DatasetTarget> findTargets(Long datasetId) {
    return new BizTemplate<List<DatasetTarget>>() {

      @Override
      protected List<DatasetTarget> process() {
        List<DatasetTarget> targets = datasetTargetRepo.findByDatasetId(datasetId);
        // Enrich targets with target names
        variableTargetQuery.setTargetName(targets);
        return targets;
      }
    }.execute();
  }

  /**
   * Generates value preview for datasets associated with a target.
   *
   * <p>This method retrieves datasets associated with a target and generates
   * a preview of their values for validation and testing purposes.</p>
   *
   * @param targetId the target entity ID
   * @param targetType the target entity type
   * @return map of parameter names to preview values
   */
  @Override
  public Map<String, String> valuePreview(Long targetId, String targetType) {
    return new BizTemplate<Map<String, String>>() {

      @Override
      protected Map<String, String> process() {
        // Retrieve dataset IDs associated with the target
        List<Long> datasetIds = datasetTargetRepo.findByDatasetIdByTargetIdAndType(
            targetId, targetType);
        return datasetIds.isEmpty() ? emptyMap() : datasetQuery.valuePreview(datasetIds);
      }
    }.execute();
  }

  /**
   * Validates target quota for dataset associations.
   *
   * <p>This method checks if the target has sufficient quota to create
   * additional dataset associations.</p>
   *
   * @param inc the increment amount to check
   * @param targetId the target ID
   * @param targetType the target type
   * @throws QuotaException if the quota limit would be exceeded
   */
  @Override
  public void checkTargetQuota(int inc, Long targetId, CombinedTargetType targetType) {
    long count = datasetTargetRepo.countByTargetIdAndTargetType(targetId, targetType);
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterTargetDataset, singleton(targetId),
        count + inc);
  }

}
