package cloud.xcan.sdf.core.angustester.application.query.kanban;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsCategory;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsLabel;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsRanking;
import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsTimeSeries;
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
