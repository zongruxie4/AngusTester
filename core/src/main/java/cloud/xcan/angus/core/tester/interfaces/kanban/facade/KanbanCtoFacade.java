package cloud.xcan.angus.core.tester.interfaces.kanban.facade;

import cloud.xcan.angus.core.tester.domain.kanban.CtoCaseOverview;
import cloud.xcan.angus.core.tester.domain.kanban.CtoTaskOverview;
import cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto.KanbanEfficiencyFindDto;

public interface KanbanCtoFacade {

  CtoTaskOverview taskCtoOverview(KanbanEfficiencyFindDto dto);

  CtoCaseOverview caseEfficiencyOverview(KanbanEfficiencyFindDto dto);

}
