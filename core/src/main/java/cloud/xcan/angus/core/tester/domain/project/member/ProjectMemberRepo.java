package cloud.xcan.angus.core.tester.domain.project.member;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProjectMemberRepo extends BaseRepository<ProjectMember, Long> {

  List<ProjectMember> findByProjectId(Long projectId);

  @Query(value = "SELECT DISTINCT member_id FROM project_members WHERE project_id = ?1", nativeQuery = true)
  List<Long> findMemberIdsByProjectId(Long projectId);

  @Modifying
  @Query(value = "DELETE FROM project_members WHERE project_id = ?1", nativeQuery = true)
  void deleteByProjectId(Long projectId);

  @Modifying
  @Query(value = "DELETE FROM project_members WHERE project_id IN ?1", nativeQuery = true)
  void deleteByProjectIdIn(Collection<Long> projectIds);

}
