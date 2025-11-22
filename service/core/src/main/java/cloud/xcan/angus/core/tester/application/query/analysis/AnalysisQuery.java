package cloud.xcan.angus.core.tester.application.query.analysis;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.analysis.Analysis;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisResource;
import cloud.xcan.angus.core.tester.domain.issue.count.AbstractOverview;
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

  void checkExits(Long projectId, String name, AnalysisResource resource);

  Analysis checkAndFind(Long id);

  List<Analysis> checkAndFind(Collection<Long> ids);

  AbstractOverview getSnapshotData(Analysis analysis);

  AbstractOverview assembleTaskAnalysisSnapshot(Analysis analysis);

  AbstractOverview assembleCaseAnalysisSnapshot(Analysis analysis);

  String getSnapshotDataString(Analysis analysis);

  Object parseSnapshotDataString(Analysis analysis, String snapshot);

  AbstractOverview parseTaskAnalysisSnapshot(String template0, String snapshot);

  AbstractOverview parseCaseAnalysisSnapshot(String template0, String snapshot);

  List<Analysis> getAnalysisCreatedSummaries(AnalysisResource resource, Long projectId,
      Long sprintId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd,
      AuthObjectType creatorOrgType, Long creatorOrgId);
}
