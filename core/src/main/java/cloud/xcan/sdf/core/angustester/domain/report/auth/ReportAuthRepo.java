package cloud.xcan.sdf.core.angustester.domain.report.auth;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ReportAuthRepo extends BaseRepository<ReportAuth, Long> {

  List<ReportAuth> findAllByAuthObjectIdIn(List<Long> orgIds);

  List<ReportAuth> findAllByReportIdInAndAuthObjectIdIn(Collection<Long> reportIds,
      List<Long> orgIds);

  List<ReportAuth> findAllByReportIdAndAuthObjectIdIn(Long reportId, List<Long> orgIds);

  List<ReportAuth> findAllByReportId(Long reportId);

  long countByReportIdAndAuthObjectIdAndAuthObjectType(Long reportId, Long authObjectId,
      AuthObjectType authObjectType);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM report_auth WHERE report_id IN ?1", nativeQuery = true)
  void deleteByReportIdIn(List<Long> reportIds);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM report_auth WHERE report_id = ?1 AND auth_object_id IN ?2 AND creator_flag = ?3", nativeQuery = true)
  void deleteByReportIdAndAuthObjectIdInAndCreatorFlag(Long reportId, Set<Long> creatorIds,
      boolean creatorFlag);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM report_auth WHERE report_id IN ?1", nativeQuery = true)
  void deleteByReportIdIn(Collection<Long> reportIds);
}
