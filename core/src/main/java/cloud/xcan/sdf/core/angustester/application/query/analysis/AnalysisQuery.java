package cloud.xcan.sdf.core.angustester.application.query.analysis;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisResource;
import cloud.xcan.sdf.core.angustester.domain.task.count.AbstractOverview;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface AnalysisQuery {

  Analysis detail(Long id);

  Page<Analysis> find(GenericSpecification<Analysis> spec, PageRequest pageable);

  File overviewExport(Long id);

  void checkExits(Long projectId, String name);

  Analysis checkAndFind(Long id);

  List<Analysis> checkAndFind(Collection<Long> ids);

  AbstractOverview getSnapshotData(Analysis analysis);

  String getSnapshotDataString(Analysis analysis);

  Object parseSnapshotDataString(Analysis analysis, String snapshot);

  List<Analysis> getAnalysisCreatedSummaries(AnalysisResource resource, Long projectId,
      Long sprintId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      AuthObjectType creatorOrgType, Long creatorOrgId);
}
