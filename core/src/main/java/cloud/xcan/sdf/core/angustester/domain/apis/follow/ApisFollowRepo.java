package cloud.xcan.sdf.core.angustester.domain.apis.follow;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisFollowRepo extends BaseRepository<ApisFollow, Long> {

  Long countByApisIdAndCreatedBy(Long apisId, Long userId);

  List<ApisFollow> findAllByApisIdInAndCreatedBy(Collection<Long> apisIds, Long userId);

  // Note: There are performance issues
  // @Query(value =
  //      "select af.id id, a.id apisId,a.summary apisName,a.method,a.uri apisUri FROM apis_follow af, apis a WHERE a.id = af.apis_id "
  //          + "AND af.created_by = ?1 AND case when ?2 IS NOT NULL then MATCH(a.summary,a.uri,a.operation_id,a.ext_search_merge) AGAINST (?2 IN BOOLEAN MODE) else 1=1 end",
  //      countQuery = "select count(af.id) FROM apis_follow af, apis a WHERE a.id = af.apis_id "
  //          + "AND af.created_by = ?1 AND case when ?2 IS NOT NULL then MATCH(a.summary,a.uri,a.operation_id,a.ext_search_merge) AGAINST (?2 IN BOOLEAN MODE) else 1=1 end",
  //      nativeQuery = true)
  // Page<ApisFollowP> search(Long userId, String apisName, Pageable pageable);

  @Query(value =
      "select af.id id, a.id apisId,a.summary apisName,a.method,a.endpoint apisUri FROM apis_follow af, apis a WHERE a.id = af.apis_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.service_deleted_flag = 0 AND a.deleted_flag = 0",
      countQuery = "select count(af.id) FROM apis_follow af, apis a WHERE a.id = af.apis_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.service_deleted_flag = 0 AND a.deleted_flag = 0",
      nativeQuery = true)
  Page<ApisFollowP> search(Long projectId, Long userId, Pageable pageable);

  /**
   * Note: Overwritten in implementation.
   */
  Page<ApisFollowP> searchByMatch(Long projectId, Long userId, String apisName, Pageable pageable);

  @Query(value = "select count(af.id) FROM apis_follow af, apis a WHERE a.id = af.apis_id "
      + "AND af.created_by = ?1 AND a.service_deleted_flag = 0 AND a.deleted_flag = 0", nativeQuery = true)
  Long countByCreatedBy(Long userId);

  @Query(value = "select count(af.id) FROM apis_follow af, apis a WHERE a.id = af.apis_id "
      + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.service_deleted_flag = 0 AND a.deleted_flag = 0", nativeQuery = true)
  Long countByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Query(value = "select created_by FROM apis_follow WHERE apis_id = ?1", nativeQuery = true)
  List<Long> findUserIdsByApisId(Long apisId);

  @Modifying
  @Query(value = "DELETE FROM apis_follow WHERE apis_id = ?1 AND created_by = ?2", nativeQuery = true)
  int deleteByApisIdAndCreatedBy(Long apisId, Long createdBy);

  @Modifying
  @Query(value = "DELETE FROM apis_follow WHERE created_by = ?1", nativeQuery = true)
  int deleteByCreatedBy(Long createdBy);

  @Modifying
  @Query(value = "DELETE FROM apis_follow WHERE project_id = ?1 AND created_by = ?2", nativeQuery = true)
  void deleteByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Modifying
  @Query(value = "DELETE FROM apis_follow WHERE apis_id in ?1", nativeQuery = true)
  void deleteByApisIdIn(List<Long> apisId);

}
