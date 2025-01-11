package cloud.xcan.sdf.core.angustester.domain.apis.favourite;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisFavouriteRepo extends BaseRepository<ApisFavourite, Long> {

  ApisFavourite findByApisIdAndCreatedBy(Long apisId, Long createdBy);

  List<ApisFavourite> findAllByApisIdInAndCreatedBy(Collection<Long> apisIds, Long userId);

  // Note: There are performance issues
  //  @Query(value =
  //      "select af.id, a.id apisId,a.summary apisName,a.method FROM apis_favourite af, apis a WHERE a.id = af.apis_id "
  //          + "AND af.created_by = ?1 AND case when ?2 IS NOT NULL then MATCH(a.summary,a.uri,a.operation_id,a.ext_search_merge) AGAINST (?2 IN BOOLEAN MODE) else 1=1 end",
  //      countQuery = "select count(af.id) FROM apis_favourite af, apis a WHERE a.id = af.apis_id "
  //          + "AND af.created_by = ?1 AND case when ?2 IS NOT NULL then MATCH(a.summary,a.uri,a.operation_id,a.ext_search_merge) AGAINST (?2 IN BOOLEAN MODE) else 1=1 end", nativeQuery = true)
  //  Page<FuncCaseFavouriteP> search(Long userId, String apisName, Pageable pageable);

  @Query(value =
      "select af.id, a.id apisId,a.summary apisName,a.method,a.endpoint apisUri FROM apis_favourite af, apis a WHERE a.id = af.apis_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.service_deleted_flag = 0 AND a.deleted_flag = 0",
      countQuery = "select count(af.id) FROM apis_favourite af, apis a WHERE a.id = af.apis_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.service_deleted_flag = 0 AND a.deleted_flag = 0", nativeQuery = true)
  Page<ApisFavouriteP> search(Long projectId, Long userId, Pageable pageable);

  /**
   * Note: Overwritten in implementation.
   */
  Page<ApisFavouriteP> searchByMatch(Long projectId, Long userId, String apisName,
      Pageable pageable);

  @Query(value = "select count(af.id) FROM apis_favourite af, apis a WHERE a.id = af.apis_id "
      + "AND af.created_by = ?1 AND a.service_deleted_flag = 0 AND a.deleted_flag = 0", nativeQuery = true)
  Long countByCreatedBy(Long userId);

  @Query(value = "select count(af.id) FROM apis_favourite af, apis a WHERE a.id = af.apis_id "
      + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.service_deleted_flag = 0 AND a.deleted_flag = 0", nativeQuery = true)
  Long countByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Modifying
  @Query(value = "DELETE FROM apis_favourite WHERE apis_id = ?1 AND created_by = ?2", nativeQuery = true)
  int deleteByApisIdAndCreatedBy(Long apisId, Long createdBy);

  @Modifying
  @Query(value = "DELETE FROM apis_favourite WHERE created_by = ?1", nativeQuery = true)
  int deleteByCreatedBy(Long createdBy);

  @Modifying
  @Query(value = "DELETE FROM apis_favourite WHERE project_id ?1 AND created_by = ?2", nativeQuery = true)
  void deleteByProjectIdAndCreatedBy(Long projectId, Long userId);

  @Modifying
  @Query(value = "DELETE FROM apis_favourite WHERE apis_id in ?1", nativeQuery = true)
  void deleteByApisIdIn(List<Long> apisId);

}
