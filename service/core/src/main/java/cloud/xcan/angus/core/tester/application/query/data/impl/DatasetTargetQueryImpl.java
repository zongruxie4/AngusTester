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

  @Override
  public List<Dataset> findDatasets(Long targetId, String targetType) {
    return new BizTemplate<List<Dataset>>() {

      @Override
      protected List<Dataset> process() {
        List<Long> datasetIds = datasetTargetRepo.findByDatasetIdByTargetIdAndType(
            targetId, targetType);
        return datasetIds.isEmpty() ? emptyList() : datasetRepo.findAllById(datasetIds);
      }
    }.execute();
  }

  @Override
  public Map<Long, List<Dataset>> findDatasets(Collection<Long> targetIds, String targetType,
      Map<Long, Long> caseApiMap) {
    return new BizTemplate<Map<Long, List<Dataset>>>() {

      @Override
      protected Map<Long, List<Dataset>> process() {
        Map<Long, List<Dataset>> targetDatasetsMap = new HashMap<>();
        List<DatasetTarget> datasetTargets = datasetTargetRepo.findByTargetIdInAndType(
            targetIds, targetType);
        // Set apis variable to cases
        if (API_CASE.getValue().equals(targetType) && isNotEmpty(caseApiMap)) {
          Map<Long, List<DatasetTarget>> apisDatsetTargetMap = datasetTargetRepo.findByTargetIdInAndType(
                  new HashSet<>(caseApiMap.values()), CombinedTargetType.API.getValue()).stream()
              .collect(Collectors.groupingBy(DatasetTarget::getTargetId));
          if (isNotEmpty(apisDatsetTargetMap)) {
            for (Long caseId : targetIds) {
              if (caseApiMap.containsKey(caseId)) {
                List<DatasetTarget> targets = apisDatsetTargetMap.get(caseApiMap.get(caseId));
                datasetTargets.addAll(targets.stream().map(x -> {
                  DatasetTarget target = new DatasetTarget();
                  target.setTargetId(caseId);
                  target.setTargetType(API_CASE);
                  target.setDatasetId(x.getTargetId());
                  return target;
                }).collect(Collectors.toList()));
              }
            }
          }
        }
        if (datasetTargets.isEmpty()) {
          return targetDatasetsMap;
        }
        List<Dataset> datasets = datasetRepo.findAllById(
            datasetTargets.stream().map(DatasetTarget::getDatasetId)
                .collect(Collectors.toSet()));
        if (datasets.isEmpty()) {
          return targetDatasetsMap;
        }
        Map<Long, List<Long>> caseDatasetIdsMap = datasetTargets.stream()
            .collect(Collectors.groupingBy(DatasetTarget::getTargetId,
                Collectors.mapping(DatasetTarget::getDatasetId, Collectors.toList())));
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

  @Override
  public List<DatasetTarget> findTargets(Long datasetId) {
    return new BizTemplate<List<DatasetTarget>>() {

      @Override
      protected List<DatasetTarget> process() {
        List<DatasetTarget> targets = datasetTargetRepo.findByDatasetId(datasetId);
        variableTargetQuery.setTargetName(targets);
        return targets;
      }
    }.execute();
  }

  @Override
  public Map<String, String> valuePreview(Long targetId, String targetType) {
    return new BizTemplate<Map<String, String>>() {

      @Override
      protected Map<String, String> process() {
        List<Long> datasetIds = datasetTargetRepo.findByDatasetIdByTargetIdAndType(
            targetId, targetType);
        return datasetIds.isEmpty() ? emptyMap() : datasetQuery.valuePreview(datasetIds);
      }
    }.execute();
  }

  @Override
  public void checkTargetQuota(int inc, Long targetId, CombinedTargetType targetType) {
    long count = datasetTargetRepo.countByTargetIdAndTargetType(targetId, targetType);
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterTargetDataset, singleton(targetId),
        count + inc);
  }

}
