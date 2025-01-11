package cloud.xcan.sdf.core.angustester.domain.report.record;

import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ReportRecordRepo extends BaseRepository<ReportRecord, Long>,
    NameJoinRepository<ReportRecord, Long> {

  @Query(value = "SELECT * FROM report_record WHERE report_id = ?1 ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
  ReportRecord findLastByReportId(Long reportId);

  @Query(value = "SELECT * FROM report_record WHERE report_id = ?1 AND template = ?2 ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
  ReportRecord findLastByReportIdAndTemplate(Long reportId, String template);

  ReportRecord findByReportIdAndIdAndTemplate(Long reportId, Long id, ReportTemplate template);

  @Query(value = "SELECT report_id FROM report_record WHERE id IN ?1", nativeQuery = true)
  Set<Long> findReportIdByIdIn(Collection<Long> ids);

  @Query(value = "SELECT a0.report_id FROM report_record a0 GROUP BY a0.report_id HAVING(count(a0.report_id) > ?1) LIMIT ?2", nativeQuery = true)
  List<Long> getReportIdsHavingCount(Long reservedNum, Long batchNum);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM report_record WHERE report_id = ?1 AND id NOT IN "
      + "(SELECT id FROM (SELECT id FROM report_record WHERE report_id = ?1 ORDER BY id DESC LIMIT ?2) as a)", nativeQuery = true)
  void deleteByReportIdAndCount(Long reportId, Long reservedNum);

  @Modifying
  @Query(value = "DELETE FROM report_record WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
