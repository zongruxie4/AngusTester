package cloud.xcan.angus.core.tester.application.query.kanban;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsCategory;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsLabel;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsRanking;
import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface KanbanDataAssetsQuery {

  Map<DataAssetsLabel, List<DataAssetsTimeSeries>> growthTrend(
      AuthObjectType creatorObjectType, Long creatorObjectId, Long projectId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, DataAssetsCategory category);

  DataAssetsRanking ranking(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd);

}
