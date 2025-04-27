package cloud.xcan.angus.core.tester.domain.comment;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

//@Repository("commonCommentRepo") -> Main DB
@NoRepositoryBean
public interface CommentRepo extends BaseRepository<Comment, Long> {

  List<Comment> findAllByPid(Long pid);

  Optional<Comment> findByIdAndUserId(Long id, Long userId);

  List<Comment> findAllByTargetIdAndTargetTypeOrderByIdDesc(Long targetId, String targetType);

  int countByTargetIdAndTargetType(Long targetId, String targetType);

  @Modifying
  @Query(value = "DELETE FROM comment WHERE target_id in ?1 AND target_type = ?2", nativeQuery = true)
  void deleteByTargetIdInAndTargetType(Collection<Long> targetId, String targetType);
}
