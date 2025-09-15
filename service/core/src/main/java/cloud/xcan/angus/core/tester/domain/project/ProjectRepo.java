package cloud.xcan.angus.core.tester.domain.project;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProjectRepo extends NameJoinRepository<Project, Long>,
    BaseRepository<Project, Long> {

  @Query(value = "SELECT count(*) FROM project WHERE id = ?1 AND (type = 'AGILE' || type is null)", nativeQuery = true)
  long countAgileById(Long id);

  @Query(value = "SELECT DISTINCT p.* FROM project p, project_members pm WHERE p.id = pm.project_id AND p.deleted = 0 AND pm.member_id IN ?1 ORDER BY p.id DESC ", nativeQuery = true)
  List<Project> findMemberProjects(Collection<Long> orgIds);

  @Query(value = "SELECT id FROM project WHERE id IN ?1", nativeQuery = true)
  List<Long> findId0ByIdIn(Collection<Long> ids);

  @Query(value = "SELECT * FROM project WHERE id IN ?1", nativeQuery = true)
  List<Project> findAllByIdIn(Collection<Long> ids);

  @Query(value = "SELECT COUNT(*) FROM project", nativeQuery = true)
  long countAll0();

  @Query(value = "SELECT COUNT(*) FROM project WHERE name = ?1 ", nativeQuery = true)
  long countAll0ByName(String name);

  @Query(value = "SELECT COUNT(*) FROM project WHERE name = ?1 AND id <> ?2", nativeQuery = true)
  long countAll0ByNameAndIdNot(String name, Long id);

  @Modifying
  @Query(value = "UPDATE project a SET a.deleted = false, a.deleted_by = null, a.deleted_date = null WHERE a.id in ?1", nativeQuery = true)
  void updateToUndeletedStatusByIdIn(Collection<Long> ids);

  @DoInFuture("Postgres support")
  @Modifying
  @Query(value = "UPDATE project a SET a.name = CONCAT(a.name, ?2) WHERE a.id in ?1", nativeQuery = true)
  void updateNameByProjectId(List<Long> ids, String renaming);

  @Modifying
  @Query(value = "DELETE FROM project WHERE id IN ?1", nativeQuery = true)
  void deleteAllByIdIn(List<Long> ids);

}
