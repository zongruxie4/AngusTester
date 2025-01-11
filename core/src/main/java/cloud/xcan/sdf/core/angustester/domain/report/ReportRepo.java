package cloud.xcan.sdf.core.angustester.domain.report;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ReportRepo extends BaseRepository<Report, Long>, NameJoinRepository<Report, Long> {

  @Query(value = "SELECT id FROM report WHERE id IN ?1 AND auth_flag = ?2 ", nativeQuery = true)
  List<Long> findIds0ByIdInAndAuthFlag(Collection<Long> ids, boolean authFlag);

  @DoInFuture("Do not use index when there are two `OR` condition")
  @Query(value = "SELECT * FROM report WHERE (status = 'PENDING' AND created_at = 'NOW') "
      + " OR (status = 'PENDING' AND created_at = 'AT_SOME_DATE' AND next_generation_date < NOW()) "
      + " OR (status <> 'FAILURE' AND created_at = 'PERIODICALLY' AND next_generation_date < NOW()) "
      + " LIMIT ?1", nativeQuery = true)
  List<Report> findGenerateByNow(Long count);

  long countByProjectIdAndNameAndVersion(Long projectId, String name, String version);

  long countByProjectIdAndTargetTypeAndTargetId(Long projectId, CombinedTargetType targetType,
      Long targetId);

  @Modifying
  @Query("UPDATE Report s SET s.authFlag=?2 WHERE s.id=?1")
  void updateAuthFlagById(Long id, Boolean enabledFlag);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM report WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
