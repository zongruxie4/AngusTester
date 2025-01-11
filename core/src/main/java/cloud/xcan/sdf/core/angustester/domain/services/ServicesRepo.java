package cloud.xcan.sdf.core.angustester.domain.services;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ServicesRepo extends BaseRepository<Services, Long>,
    NameJoinRepository<Services, Long> {

  @Query(value = "SELECT * FROM services WHERE id = ?1", nativeQuery = true)
  Optional<Services> find0ById(Long id);

  @Query(value = "SELECT id FROM services WHERE id in ?1 AND auth_flag = ?2 ", nativeQuery = true)
  List<Long> findIds0ByIdInAndAuthFlag(Collection<Long> ids, boolean authFlag);

  /**
   * Note: Logical deletion data is included
   */
  @Query(value = "SELECT * FROM services WHERE id in ?1", nativeQuery = true)
  List<Services> findAll0ByIdIn(Collection<Long> ids);

  @Query(value = "SELECT id FROM services WHERE id in ?1", nativeQuery = true)
  List<Long> findIds0ByIdIn(Collection<Long> ids);

  @Query(value = "SELECT COUNT(id) FROM services WHERE name = ?1 AND project_id = ?2 ", nativeQuery = true)
  Long countAll0ByNameAndAndProjectId(String name, long projectId);

  boolean existsByName(String name);

  @Query("SELECT a.authFlag FROM Services a WHERE a.id = ?1")
  Boolean findAuthFlagById(Long id);

  @Modifying
  @Query(value = "UPDATE services p SET p.name=?2 WHERE p.id=?1", nativeQuery = true)
  void updateNameById(Long id, String name);

  @Modifying
  @Query(value = "UPDATE services p SET p.auth_flag=?2 WHERE p.id=?1", nativeQuery = true)
  void updateAuthFlagById(Long id, Boolean enabledFlag);

  @Modifying
  @Query(value = "UPDATE services p SET p.status=?2 WHERE p.id=?1", nativeQuery = true)
  void updateStatusById(Long id, String status);

  @Modifying
  @Query(value = "UPDATE services p SET p.status=?2 WHERE p.id in ?1", nativeQuery = true)
  void updateStatusByIdIn(Collection<Long> id, String status);

  @Modifying
  @Query("UPDATE Services p SET p.deletedFlag = 0, p.deletedBy = -1, p.deletedDate = null WHERE p.id in ?1")
  void updateToUndeletedStatusByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM services WHERE id in ?1", nativeQuery = true)
  void deleteAllByIdIn(List<Long> ids);

}
