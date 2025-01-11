package cloud.xcan.sdf.core.angustester.domain.tag;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TagRepo extends BaseRepository<Tag, Long> {

  List<Tag> findByProjectIdAndNameIn(Long projectId, Collection<String> names);

  List<Tag> findByIdIn(Collection<Long> ids);

  @Override
  @Query(value = "select id from tag where id in ?1", nativeQuery = true)
  List<Long> findIdByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM tag WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);
}
