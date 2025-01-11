package cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.internal;

import cloud.xcan.sdf.core.angustester.application.query.kanban.KanbanCtoQuery;
import cloud.xcan.sdf.core.angustester.domain.kanban.CtoCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.CtoTaskOverview;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.KanbanCtoFacade;
import cloud.xcan.sdf.core.angustester.interfaces.kanban.facade.dto.KanbanEfficiencyFindDto;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class KanbanCtoFacadeImpl implements KanbanCtoFacade {

  @Resource
  private KanbanCtoQuery kanbanCtoQuery;

  @Override
  public CtoTaskOverview taskCtoOverview(KanbanEfficiencyFindDto dto) {
    return kanbanCtoQuery.taskCtoOverview(dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getProjectId(), dto.getPlanId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd(), false, false);
  }

  @Override
  public CtoCaseOverview caseEfficiencyOverview(KanbanEfficiencyFindDto dto) {
    return kanbanCtoQuery.caseCtoOverview(dto.getCreatorObjectType(),
        dto.getCreatorObjectId(), dto.getProjectId(), dto.getPlanId(), dto.getCreatedDateStart(),
        dto.getCreatedDateEnd(), false, false);
  }

}
