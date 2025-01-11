package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistoryInfo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor.ScenarioMonitorHistoryFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor.ScenarioMonitorHistoryListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class ScenarioMonitorHistoryAssembler {

  public static ScenarioMonitorHistoryDetailVo toDetail(ScenarioMonitorHistory history) {
    return new ScenarioMonitorHistoryDetailVo().setId(history.getId())
        .setProjectId(history.getProjectId()).setMonitorId(history.getMonitorId())
        .setStatus(history.getStatus()).setFailureMessage(history.getFailureMessage())
        .setResponseDelay(history.getResponseDelay()).setExecId(history.getExecId())
        .setExecStartDate(history.getExecStartDate()).setExecEndDate(history.getExecEndDate())
        .setSampleContents(history.getSampleContents())
        .setSchedulingResult(history.getSchedulingResult())
        .setSampleLogContent(history.getSampleLogContent())
        .setCreatedBy(history.getCreatedBy()).setCreatedDate(history.getCreatedDate());
  }

  public static ScenarioMonitorHistoryListVo toListVo(ScenarioMonitorHistoryInfo history) {
    return new ScenarioMonitorHistoryListVo().setId(history.getId())
        .setProjectId(history.getProjectId()).setMonitorId(history.getMonitorId())
        .setStatus(history.getStatus()).setFailureMessage(history.getFailureMessage())
        .setResponseDelay(history.getResponseDelay()).setExecId(history.getExecId())
        .setExecStartDate(history.getExecStartDate()).setExecEndDate(history.getExecEndDate())
        .setCreatedBy(history.getCreatedBy()).setCreatedDate(history.getCreatedDate());
  }

  public static GenericSpecification<ScenarioMonitorHistoryInfo> getSpecification(
      ScenarioMonitorHistoryFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .inAndNotFields("id")
        .rangeSearchFields("id", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }
}
