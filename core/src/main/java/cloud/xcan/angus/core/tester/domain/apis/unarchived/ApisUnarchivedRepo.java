package cloud.xcan.angus.core.tester.domain.apis.unarchived;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisUnarchivedRepo extends BaseRepository<ApisUnarchived, Long> {

  @Query(value = "SELECT au.id FROM ApisUnarchived au WHERE au.id in ?1 AND au.createdBy = ?2")
  List<Long> findApisUnarchiveCreateBy(Collection<Long> ids, Long userId);

  Optional<ApisUnarchived> findByCreatedByAndId(Long createdBy, Long id);

  List<ApisUnarchived> findAllByCreatedByAndIdIn(Long createdBy, Collection<Long> ids);

  Long countByCreatedBy(Long userId);

  Long countByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Modifying
  @Query(value = "DELETE FROM apis_unarchived WHERE created_by = ?1", nativeQuery = true)
  void deleteAllByCreatedBy(Long createdBy);

  @Modifying
  @Query(value = "DELETE FROM apis_unarchived WHERE created_by = ?1 AND id = ?2", nativeQuery = true)
  void deleteByCreatedByAndId(Long createdBy, Long id);

  @Modifying
  @Query(value = "DELETE FROM apis_unarchived WHERE created_by = ?1 AND id in ?2", nativeQuery = true)
  void deleteAllByCreatedByAndIdIn(Long createdBy, Collection<Long> ids);

  @Modifying
  @Query("UPDATE ApisUnarchived a SET a.summary=?2 WHERE a.id = ?1")
  void updateApisUnarchivedSummary(Long id, String summary);

}
