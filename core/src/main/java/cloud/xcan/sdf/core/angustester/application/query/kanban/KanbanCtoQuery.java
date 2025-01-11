package cloud.xcan.sdf.core.angustester.application.query.kanban;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.kanban.CtoCaseOverview;
import cloud.xcan.sdf.core.angustester.domain.kanban.CtoTaskOverview;
import java.time.LocalDateTime;

public interface KanbanCtoQuery {

  CtoTaskOverview taskCtoOverview(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId, Long planId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinTaskList,
      boolean joinAssigneeOverview);

  CtoCaseOverview caseCtoOverview(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId, Long planId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd, boolean joinCaseList,
      boolean joinTesterOverview);

}
