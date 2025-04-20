package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler;

import static java.util.Objects.isNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorSearchDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorDetailVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorListVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class ScenarioMonitorAssembler {

  public static ScenarioMonitor toAddDomain(ScenarioMonitorAddDto dto) {
    return new ScenarioMonitor()
        .setScenarioId(dto.getScenarioId())
        .setName(dto.getName()).setDescription(dto.getDescription())
        .setTimeSetting(dto.getTimeSetting()).setCreatedAt(dto.getTimeSetting().getCreatedAt())
        .setServerSetting(dto.getServerSetting())
        .setNoticeSetting(dto.getNoticeSetting());
  }

  public static ScenarioMonitor toUpdateDomain(ScenarioMonitorUpdateDto dto) {
    return new ScenarioMonitor().setId(dto.getId())
        .setName(dto.getName()).setDescription(dto.getDescription())
        .setTimeSetting(dto.getTimeSetting()).setCreatedAt(dto.getTimeSetting().getCreatedAt())
        .setServerSetting(dto.getServerSetting())
        .setNoticeSetting(dto.getNoticeSetting());
  }

  public static ScenarioMonitor toReplaceDomain(ScenarioMonitorReplaceDto dto) {
    return new ScenarioMonitor().setId(dto.getId())
        .setScenarioId(isNull(dto.getId()) ? dto.getScenarioId() : null)
        .setName(dto.getName()).setDescription(dto.getDescription())
        .setTimeSetting(dto.getTimeSetting()).setCreatedAt(dto.getTimeSetting().getCreatedAt())
        .setServerSetting(dto.getServerSetting())
        .setNoticeSetting(dto.getNoticeSetting());
  }

  public static ScenarioMonitorDetailVo toDetailVo(ScenarioMonitor monitor) {
    return new ScenarioMonitorDetailVo().setId(monitor.getId())
        .setProjectId(monitor.getProjectId())
        .setScenarioId(monitor.getScenarioId())
        .setName(monitor.getName()).setDescription(monitor.getDescription())
        .setStatus(monitor.getStatus()).setFailureMessage(monitor.getFailureMessage())
        .setNextExecDate(monitor.getNextExecDate())
        .setLastMonitorHistoryId(monitor.getLastMonitorHistoryId())
        .setLastMonitorDate(monitor.getLastMonitorDate())
        .setTimeSetting(monitor.getTimeSetting())
        .setServerSetting(monitor.getServerSetting())
        .setNoticeSetting(monitor.getNoticeSetting())
        .setCount(monitor.getCount())
        .setTenantId(monitor.getTenantId())
        .setCreatedBy(monitor.getCreatedBy())
        .setCreatedDate(monitor.getCreatedDate())
        .setLastModifiedBy(monitor.getLastModifiedBy())
        .setLastModifiedDate(monitor.getLastModifiedDate());
  }

  public static ScenarioMonitorListVo toListVo(ScenarioMonitor monitor) {
    return new ScenarioMonitorListVo().setId(monitor.getId())
        .setProjectId(monitor.getProjectId())
        .setScenarioId(monitor.getScenarioId())
        .setName(monitor.getName()).setDescription(monitor.getDescription())
        .setStatus(monitor.getStatus()).setFailureMessage(monitor.getFailureMessage())
        .setNextExecDate(monitor.getNextExecDate())
        .setLastMonitorHistoryId(monitor.getLastMonitorHistoryId())
        .setLastMonitorDate(monitor.getLastMonitorDate())
        .setCount(monitor.getCount())
        .setTenantId(monitor.getTenantId())
        .setCreatedBy(monitor.getCreatedBy())
        .setCreatedDate(monitor.getCreatedDate())
        .setLastModifiedBy(monitor.getLastModifiedBy())
        .setLastModifiedDate(monitor.getLastModifiedDate());
  }

  public static GenericSpecification<ScenarioMonitor> getSpecification(ScenarioMonitorFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("id")
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("name", "description")
        .orderByFields("id", "name", "status", "createdDate", "createdBy", "lastModifiedBy")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(ScenarioMonitorSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("id")
        .rangeSearchFields("id", "createdDate")
        .matchSearchFields("name", "description")
        .orderByFields("id", "name", "status", "createdDate", "createdBy", "lastModifiedBy")
        .build();
  }

}
