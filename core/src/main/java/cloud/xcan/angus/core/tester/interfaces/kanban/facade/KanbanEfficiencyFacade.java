package cloud.xcan.angus.core.tester.interfaces.kanban.facade;

import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.angus.core.tester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanEfficiencyFindDto;

public interface KanbanEfficiencyFacade {

  EfficiencyTaskOverview taskEfficiencyOverview(KanbanEfficiencyFindDto dto);

  EfficiencyCaseOverview caseEfficiencyOverview(KanbanEfficiencyFindDto dto);

}
