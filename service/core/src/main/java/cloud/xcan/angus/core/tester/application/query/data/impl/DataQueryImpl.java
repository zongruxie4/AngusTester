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
import org.springframework.stereotype.Service;
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

/**
 * Implementation of data resources query operations for statistics and analytics.
 *
 * <p>This class provides comprehensive functionality for querying and analyzing
 * data resources including variables, datasets, datasources, and storage files.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Data resources count statistics with filtering capabilities</li>
 *   <li>Creation statistics with time-based analysis (7-day, 30-day trends)</li>
 *   <li>Usage statistics for variables and datasets</li>
 *   <li>Database type distribution for datasources</li>
 *   <li>Storage file statistics integration</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Service
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

  /**
   * Generates comprehensive count statistics for data resources.
   *
   * <p>This method provides total counts for various data resources including
   * variables, datasets, datasources, and storage files based on specified filtering criteria.</p>
   *
   * @param projectId         the project ID for filtering
   * @param creatorObjectType the type of creator object (User, Group, Dept)
   * @param creatorObjectId   the ID of the creator object
   * @param createdDateStart  the start date for creation time filtering
   * @param createdDateEnd    the end date for creation time filtering
   * @return comprehensive count statistics for all data resources
   */
  @Override
  public DataResourcesCount countStatistics(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<DataResourcesCount>() {

      final DataResourcesCount result = new DataResourcesCount();

      Set<Long> createdBys = null;

      @Override
      protected DataResourcesCount process() {
        // Determine creator filter: find all when condition is null, else find by condition
        if (nonNull(creatorObjectType)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        // Build common filters for all resource types
        Set<SearchCriteria> allFilters = getCommonResourcesStatsFilter(
            projectId, createdDateStart, createdDateEnd, createdBys);

        // Count statistics for each data resource type
        countVariable(result, allFilters);
        countDataset(result, allFilters);
        countDatasource(result, allFilters);
        countSpaceFile(result, projectId, creatorObjectType, creatorObjectId,
            createdDateStart, createdDateEnd);
        return result;
      }
    }.execute();
  }

  /**
   * Generates comprehensive creation statistics with time-based analysis.
   *
   * <p>This method provides detailed creation statistics including total counts,
   * recent trends (7-day and 30-day), usage statistics, and database type distribution for data
   * resources.</p>
   *
   * @param projectId         the project ID for filtering
   * @param creatorObjectType the type of creator object (User, Group, Dept)
   * @param creatorObjectId   the ID of the creator object
   * @param createdDateStart  the start date for creation time filtering
   * @param createdDateEnd    the end date for creation time filtering
   * @return comprehensive creation statistics with time-based analysis
   */
  @Override
  public DataResourcesCreationCount creationStatistics(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<DataResourcesCreationCount>() {
      final LocalDateTime now = LocalDateTime.now();
      final LocalDateTime last7DayBefore = now.minusDays(7);
      final LocalDateTime last30DayBefore = now.minusDays(30);

      final DataResourcesCreationCount result = new DataResourcesCreationCount();

      Set<Long> createdBys = null;

      @Override
      protected DataResourcesCreationCount process() {
        // Determine creator filter: find all when condition is null, else find by condition
        if (nonNull(creatorObjectType)) {
          createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
        }

        // Build filters for different time periods
        Set<SearchCriteria> commonFilters = getCommonResourcesStatsFilter(
            projectId, createdDateStart, createdDateEnd, createdBys);
        Set<SearchCriteria> last7DayFilters = getCommonResourcesStatsFilter(
            projectId, last7DayBefore, now, createdBys);
        Set<SearchCriteria> last30DayFilters = getCommonResourcesStatsFilter(
            projectId, last30DayBefore, now, createdBys);

        // Count creation statistics for each resource type
        countVariable(result, commonFilters, last7DayFilters, last30DayFilters);
        countDataset(result, commonFilters, last7DayFilters, last30DayFilters);
        countDatasource(result, commonFilters, last7DayFilters, last30DayFilters);

        // Calculate usage statistics for variables and datasets
        setVariableByUse(result, projectId, commonFilters);
        setDatasetByUse(result, projectId, commonFilters);
        // Calculate database type distribution for datasources
        setDatasourceByDb(result, commonFilters);

        // Count storage file statistics
        countSpaceFile(result, projectId, creatorObjectType, creatorObjectId,
            createdDateStart, createdDateEnd);
        return result;
      }
    }.execute();
  }

  /**
   * Counts variables based on specified filters.
   *
   * @param result     the result object to store the count
   * @param allFilters the search criteria filters
   */
  private void countVariable(DataResourcesCount result, Set<SearchCriteria> allFilters) {
    result.setAllVariable(variableRepo.countAllByFilters(allFilters));
  }

  /**
   * Counts datasets based on specified filters.
   *
   * @param result     the result object to store the count
   * @param allFilters the search criteria filters
   */
  private void countDataset(DataResourcesCount result, Set<SearchCriteria> allFilters) {
    result.setAllDataset(datasetRepo.countAllByFilters(allFilters));
  }

  /**
   * Counts datasources based on specified filters.
   *
   * @param result     the result object to store the count
   * @param allFilters the search criteria filters
   */
  private void countDatasource(DataResourcesCount result, Set<SearchCriteria> allFilters) {
    result.setAllVariable(datasourceRepo.countAllByFilters(allFilters));
  }

  /**
   * Counts storage files for DataResourcesCount result.
   *
   * @param result            the result object to store the count
   * @param projectId         the project ID
   * @param creatorObjectType the creator object type
   * @param creatorObjectId   the creator object ID
   * @param createdDateStart  the start date
   * @param createdDateEnd    the end date
   */
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

  /**
   * Counts storage files for DataResourcesCreationCount result.
   *
   * @param result            the result object to store the count
   * @param projectId         the project ID
   * @param creatorObjectType the creator object type
   * @param creatorObjectId   the creator object ID
   * @param createdDateStart  the start date
   * @param createdDateEnd    the end date
   */
  private void countSpaceFile(DataResourcesCreationCount result, Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    SpaceAssetsCountDto dto = new SpaceAssetsCountDto().setProjectId(projectId)
        .setCreatorObjectType(creatorObjectType).setCreatorObjectId(creatorObjectId)
        .setCreatedDateStart(createdDateStart).setCreatedDateEnd(createdDateEnd);
    // Use appropriate remote service based on edition type
    StorageResourcesCreationCount count = isCloudServiceEdition()
        ? spaceRemote.resourcesCreationStatistics(dto).orElseContentThrow()
        : spacePrivRemote.resourcesCreationStatistics(dto).orElseContentThrow();
    result.setAllFile(count.getAllSpaceFile())
        .setFileByLast7Day(count.getSpaceFileByLast7Day())
        .setFileByLast30Day(count.getSpaceFileByLast30Day())
        .setFileByResourceType(count.getSpaceFileByResourceType());
  }

  /**
   * Counts variables with time-based analysis for creation statistics.
   *
   * @param result          the result object to store the counts
   * @param allFilters      the common search criteria filters
   * @param last7DayFilter  the 7-day filter
   * @param last30DayFilter the 30-day filter
   */
  private void countVariable(DataResourcesCreationCount result, Set<SearchCriteria> allFilters,
      Set<SearchCriteria> last7DayFilter, Set<SearchCriteria> last30DayFilter) {
    result.setAllVariable(variableRepo.countAllByFilters(allFilters))
        .setVariableByLast7Day(variableRepo.countAllByFilters(last7DayFilter))
        .setVariableByLast30Day(variableRepo.countAllByFilters(last30DayFilter));
  }

  /**
   * Counts datasets with time-based analysis for creation statistics.
   *
   * @param result          the result object to store the counts
   * @param allFilters      the common search criteria filters
   * @param last7DayFilter  the 7-day filter
   * @param last30DayFilter the 30-day filter
   */
  private void countDataset(DataResourcesCreationCount result, Set<SearchCriteria> allFilters,
      Set<SearchCriteria> last7DayFilter, Set<SearchCriteria> last30DayFilter) {
    result.setAllDataset(datasetRepo.countAllByFilters(allFilters))
        .setDatasetByLast7Day(datasetRepo.countAllByFilters(last7DayFilter))
        .setDatasetByLast30Day(datasetRepo.countAllByFilters(last30DayFilter));
  }

  /**
   * Counts datasources with time-based analysis for creation statistics.
   *
   * @param result          the result object to store the counts
   * @param allFilters      the common search criteria filters
   * @param last7DayFilter  the 7-day filter
   * @param last30DayFilter the 30-day filter
   */
  private void countDatasource(DataResourcesCreationCount result, Set<SearchCriteria> allFilters,
      Set<SearchCriteria> last7DayFilter, Set<SearchCriteria> last30DayFilter) {
    result.setAllDatasource(datasourceRepo.countAllByFilters(allFilters))
        .setDatasourceByLast7Day(datasourceRepo.countAllByFilters(last7DayFilter))
        .setDatasourceByLast30Day(datasourceRepo.countAllByFilters(last30DayFilter));
  }

  /**
   * Calculates usage statistics for variables in a project.
   *
   * <p>This method determines which variables are in use versus not in use
   * based on their association with targets.</p>
   *
   * @param result        the result object to store usage statistics
   * @param projectId     the project ID
   * @param commonFilters the common search criteria filters
   */
  private void setVariableByUse(DataResourcesCreationCount result, Long projectId,
      Set<SearchCriteria> commonFilters) {
    List<Long> allVarIds = variableRepo.findAllIdsByProjectId(projectId);
    // Count variables by usage (grouped by variable ID)
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

  /**
   * Calculates usage statistics for datasets in a project.
   *
   * <p>This method determines which datasets are in use versus not in use
   * based on their association with targets.</p>
   *
   * @param result        the result object to store usage statistics
   * @param projectId     the project ID
   * @param commonFilters the common search criteria filters
   */
  private void setDatasetByUse(DataResourcesCreationCount result, Long projectId,
      Set<SearchCriteria> commonFilters) {
    List<Long> allDsIds = datasetRepo.findAllIdsByProjectId(projectId);
    // Count datasets by usage (grouped by dataset ID)
    Map<Long, Long> dsUseCountMap = datasetTargetRepo.countByFiltersAndGroup(
            DatasetTarget.class, LongKeyCountSummary.class, commonFilters,
            "datasetId", "id").stream()
        .collect(Collectors.toMap(LongKeyCountSummary::getKey, LongKeyCountSummary::getTotal));
    Map<UseStatus, Long> varByUseMap = new HashMap<>();
    varByUseMap.put(UseStatus.IN_USE, (long) dsUseCountMap.keySet().size());
    varByUseMap.put(UseStatus.NOT_IN_USE, (long) (allDsIds.size() - dsUseCountMap.keySet().size()));
    result.setDatasetByUse(varByUseMap);
  }

  /**
   * Calculates database type distribution for datasources.
   *
   * <p>This method groups datasources by their database type and counts
   * the distribution across different database technologies.</p>
   *
   * @param result        the result object to store database distribution
   * @param commonFilters the common search criteria filters
   */
  private void setDatasourceByDb(DataResourcesCreationCount result,
      Set<SearchCriteria> commonFilters) {
    // Count datasources by database type (grouped by database field)
    Map<String, Long> dsUseCountMap = datasourceRepo.countByFiltersAndGroup(
            Datasource.class, CountSummary.class, commonFilters, "database", "id")
        .stream().collect(Collectors.toMap(CountSummary::getKey, CountSummary::getTotal));
    result.setDatasourceByDb(dsUseCountMap);
  }

}
