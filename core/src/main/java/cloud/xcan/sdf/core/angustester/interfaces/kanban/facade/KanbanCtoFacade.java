package cloud.xcan.sdf.core.angustester.interfaces.kanban.facade;

import cloud.xcan.sdf.core.angustester.domain.kanban.CtoCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.CtoTaskOverview;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanEfficiencyFindDto;

public interface KanbanCtoFacade {

  CtoTaskOverview taskCtoOverview(KanbanEfficiencyFindDto dto);

  CtoCaseOverview caseEfficiencyOverview(KanbanEfficiencyFindDto dto);

}
