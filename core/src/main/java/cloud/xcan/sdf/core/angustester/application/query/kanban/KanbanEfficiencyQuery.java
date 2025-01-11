package cloud.xcan.sdf.core.angustester.application.query.kanban;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.EfficiencyTaskOverview;
import java.time.LocalDateTime;

public interface KanbanEfficiencyQuery {

  EfficiencyTaskOverview taskEfficiencyOverview(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId, Long planId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinTaskList,
      boolean joinAssigneeOverview, boolean joinRanking, boolean joinBurnDownChar);

  EfficiencyCaseOverview caseEfficiencyOverview(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId, Long planId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinTestHit, boolean joinCaseList,
      boolean joinTesterOverview, boolean joinRanking, boolean joinBurnDownChar);

}
