package cloud.xcan.angus.core.tester.application.query.kanban;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.kanban.CtoCaseOverview;
import cloud.xcan.angus.core.tester.domain.kanban.CtoTaskOverview;
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
