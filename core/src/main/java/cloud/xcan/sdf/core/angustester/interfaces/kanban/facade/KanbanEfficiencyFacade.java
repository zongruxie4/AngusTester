package cloud.xcan.sdf.core.angustester.interfaces.kanban.facade;

import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyTaskOverview;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanEfficiencyFindDto;

public interface KanbanEfficiencyFacade {

  EfficiencyTaskOverview taskEfficiencyOverview(KanbanEfficiencyFindDto dto);

  EfficiencyCaseOverview caseEfficiencyOverview(KanbanEfficiencyFindDto dto);

}
