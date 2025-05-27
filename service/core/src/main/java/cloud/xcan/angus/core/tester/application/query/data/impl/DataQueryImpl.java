package cloud.xcan.angus.core.tester.application.query.data.impl;

import static cloud.xcan.angus.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isCloudServiceEdition;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.space.StorageResourcesCount;
import cloud.xcan.angus.api.commonlink.space.StorageResourcesCreationCount;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.UseStatus;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.api.storage.space.SpacePrivRemote;
import cloud.xcan.angus.api.storage.space.SpaceRemote;
import cloud.xcan.angus.api.storage.space.dto.SpaceAssetsCountDto;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.repository.CountSummary;
import cloud.xcan.angus.core.jpa.repository.LongKeyCountSummary;
import cloud.xcan.angus.core.tester.application.query.data.DataQuery;
import cloud.xcan.angus.core.tester.domain.data.DataResourcesCount;
import cloud.xcan.angus.core.tester.domain.data.DataResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetRepo;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTarget;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetTargetRepo;
import cloud.xcan.angus.core.tester.domain.data.datasource.Datasource;
import cloud.xcan.angus.core.tester.domain.data.datasource.DatasourceRepo;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableRepo;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTarget;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTargetRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Biz
public class DataQueryImpl implements DataQuery {

  @Resource
  private VariableRepo variableRepo;

  @Resource
  private VariableTargetRepo variableTargetRepo;

  @Resource
  private DatasetRepo datasetRepo;

  @Resource
  private DatasetTargetRepo datasetTargetRepo;

  @Resource
  private DatasourceRepo datasourceRepo;

  @Resource
  private UserManager userManager;

  @Resource
  private SpaceRemote spaceRemote;

  @Resource
  private SpacePrivRemote spacePrivRemote;

  @Override
  public DataResourcesCount countStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<DataResourcesCount>() {

      final DataResourcesCount result = new DataResourcesCount();

      Set<Long> createdBys = null;


      @Override
      protected DataResourcesCount process() {
        // Find all when condition is null, else find by condition
        if (nonNull(creatorObjectType)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        Set<SearchCriteria> allFilters = getCommonResourcesStatsFilter(
            projectId, createdDateStart, createdDateEnd, createdBys);

        // Number of statistical data
        countVariable(result, allFilters);
        countDataset(result, allFilters);
        countDatasource(result, allFilters);
        countSpaceFile(result, projectId, creatorObjectType, creatorObjectId,
            createdDateStart, createdDateEnd);
        return result;
      }
    }.execute();
  }

  @Override
  public DataResourcesCreationCount creationStatistics(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<DataResourcesCreationCount>() {
      final LocalDateTime now = LocalDateTime.now();
      //final LocalDateTime dayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
      //final LocalDateTime dayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
      final LocalDateTime last7DayBefore = now.minusDays(7);
      final LocalDateTime last30DayBefore = now.minusDays(30);

      final DataResourcesCreationCount result = new DataResourcesCreationCount();

      Set<Long> createdBys = null;


      @Override
      protected DataResourcesCreationCount process() {
        // Find all when condition is null, else find by condition
        if (nonNull(creatorObjectType)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(
            projectId, createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> last7DayFilters = getCommonResourcesStatsFilter(
            projectId, last7DayBefore, now, createdBys);
        Set<SearchCriteria> last30DayFilters = getCommonResourcesStatsFilter(
            projectId, last30DayBefore, now, createdBys);

        // Number of statistical variables
        countVariable(result, commonFilters, last7DayFilters, last30DayFilters);
        // Number of statistical datasets
        countDataset(result, commonFilters, last7DayFilters, last30DayFilters);
        // Number of statistical datasource
        countDatasource(result, commonFilters, last7DayFilters, last30DayFilters);

        // Number of statistical variables use
        setVariableByUse(result, projectId, commonFilters);
        // Number of statistical variables use
        setDatasetByUse(result, projectId, commonFilters);
        // Number of statistical datasource database
        setDatasourceByDb(result, commonFilters);

        countSpaceFile(result, projectId, creatorObjectType, creatorObjectId,
            createdDateStart, createdDateEnd);
        return result;
      }
    }.execute();
  }

  private void countVariable(DataResourcesCount result, Set<SearchCriteria> allFilters) {
    result.setAllVariable(variableRepo.countAllByFilters(allFilters));
  }

  private void countDataset(DataResourcesCount result, Set<SearchCriteria> allFilters) {
    result.setAllDataset(datasetRepo.countAllByFilters(allFilters));
  }

  private void countDatasource(DataResourcesCount result, Set<SearchCriteria> allFilters) {
    result.setAllVariable(datasourceRepo.countAllByFilters(allFilters));
  }

  private void countSpaceFile(DataResourcesCount result, Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    StorageResourcesCount count = spaceRemote.resourcesStatistics(
            new SpaceAssetsCountDto().setProjectId(projectId)
                .setCreatorObjectType(creatorObjectType).setCreatorObjectId(creatorObjectId)
                .setCreatedDateStart(createdDateStart).setCreatedDateEnd(createdDateEnd))
        .orElseContentThrow();
    result.setAllFile(count.getAllSpaceFiles());
  }

  private void countSpaceFile(DataResourcesCreationCount result, Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    SpaceAssetsCountDto dto = new SpaceAssetsCountDto().setProjectId(projectId)
        .setCreatorObjectType(creatorObjectType).setCreatorObjectId(creatorObjectId)
        .setCreatedDateStart(createdDateStart).setCreatedDateEnd(createdDateEnd);
    StorageResourcesCreationCount count = isCloudServiceEdition()
        ? spaceRemote.resourcesCreationStatistics(dto).orElseContentThrow()
        : spacePrivRemote.resourcesCreationStatistics(dto).orElseContentThrow();
    result.setAllFile(count.getAllSpaceFile())
        .setFileByLast7Day(count.getSpaceFileByLast7Day())
        .setFileByLast30Day(count.getSpaceFileByLast30Day())
        .setFileByResourceType(count.getSpaceFileByResourceType());
  }

  private void countVariable(DataResourcesCreationCount result, Set<SearchCriteria> allFilters,
      Set<SearchCriteria> last7DayFilter, Set<SearchCriteria> last30DayFilter) {
    result.setAllVariable(variableRepo.countAllByFilters(allFilters))
        .setVariableByLast7Day(variableRepo.countAllByFilters(last7DayFilter))
        .setVariableByLast30Day(variableRepo.countAllByFilters(last30DayFilter));
  }

  private void countDataset(DataResourcesCreationCount result, Set<SearchCriteria> allFilters,
      Set<SearchCriteria> last7DayFilter, Set<SearchCriteria> last30DayFilter) {
    result.setAllDataset(datasetRepo.countAllByFilters(allFilters))
        .setDatasetByLast7Day(datasetRepo.countAllByFilters(last7DayFilter))
        .setDatasetByLast30Day(datasetRepo.countAllByFilters(last30DayFilter));
  }

  private void countDatasource(DataResourcesCreationCount result, Set<SearchCriteria> allFilters,
      Set<SearchCriteria> last7DayFilter, Set<SearchCriteria> last30DayFilter) {
    result.setAllDatasource(datasourceRepo.countAllByFilters(allFilters))
        .setDatasourceByLast7Day(datasourceRepo.countAllByFilters(last7DayFilter))
        .setDatasourceByLast30Day(datasourceRepo.countAllByFilters(last30DayFilter));
  }

  private void setVariableByUse(DataResourcesCreationCount result, Long projectId,
      Set<SearchCriteria> commonFilters) {
    List<Long> allVarIds = variableRepo.findAllIdsByProjectId(projectId);
    Map<Long, Long> varUseCountMap = variableTargetRepo.countByFiltersAndGroup(
            VariableTarget.class, LongKeyCountSummary.class, commonFilters,
            "variableId", "id").stream()
        .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
    Map<UseStatus, Long> varByUseMap = new HashMap<>();
    varByUseMap.put(UseStatus.IN_USE, (long) varUseCountMap.keySet().size());
    varByUseMap.put(UseStatus.NOT_IN_USE,
        (long) (allVarIds.size() - varUseCountMap.keySet().size()));
    result.setVariableByUse(varByUseMap);
  }

  private void setDatasetByUse(DataResourcesCreationCount result, Long projectId,
      Set<SearchCriteria> commonFilters) {
    List<Long> allDsIds = datasetRepo.findAllIdsByProjectId(projectId);
    Map<Long, Long> dsUseCountMap = datasetTargetRepo.countByFiltersAndGroup(
            DatasetTarget.class, LongKeyCountSummary.class, commonFilters,
            "datasetId", "id").stream()
        .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
    Map<UseStatus, Long> varByUseMap = new HashMap<>();
    varByUseMap.put(UseStatus.IN_USE, (long) dsUseCountMap.keySet().size());
    varByUseMap.put(UseStatus.NOT_IN_USE, (long) (allDsIds.size() - dsUseCountMap.keySet().size()));
    result.setDatasetByUse(varByUseMap);
  }

  private void setDatasourceByDb(DataResourcesCreationCount result,
      Set<SearchCriteria> commonFilters) {
    Map<String, Long> dsUseCountMap = datasourceRepo.countByFiltersAndGroup(
            Datasource.class, CountSummary.class, commonFilters, "database", "id")
        .stream().collect(Collectors.toMap(CountSummary::getKey, CountSummary::getTotal));
    result.setDatasourceByDb(dsUseCountMap);
  }

}
