package cloud.xcan.angus.core.tester.domain.module;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ModuleRepo extends BaseRepository<Module, Long>,
    NameJoinRepository<Module, Long> {

  List<Module> findByProjectIdAndNameIn(Long projectId, Collection<String> names);

  List<Module> findByIdIn(Collection<Long> ids);

  List<Module> findByProjectIdAndIdIn(Long projectId, Collection<Long> ids);

  @Override
  @Query(value = "SELECT id FROM module WHERE id IN ?1", nativeQuery = true)
  List<Long> findIdByIdIn(Collection<Long> ids);

  List<Module> findByProjectIdAndPidIn(Long projectId, Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM module WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
