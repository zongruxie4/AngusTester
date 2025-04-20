package cloud.xcan.angus.core.tester.infra.persistence.postgres.apis;

import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteP;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApisFavouriteRepoPostgres extends ApisFavouriteRepo {

  @Override
  @Query(value =
      "select af.id, a.id apisId,a.summary apisName,a.method,a.endpoint apisUri FROM apis_favourite af, apis a WHERE a.id = af.apis_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.service_deleted = 0 AND a.deleted = 0 "
          + "AND to_tsvector(a.summary,a.endpoint,a.operation_id,a.ext_search_merge) @@ plainto_tsquery(?3)",
      countQuery = "select count(af.id) FROM apis_favourite af, apis a WHERE a.id = af.apis_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.service_deleted = 0 AND a.deleted = 0 "
          + "AND to_tsvector(a.summary,a.endpoint,a.operation_id,a.ext_search_merge) @@ plainto_tsquery(?3)",
      nativeQuery = true)
  Page<ApisFavouriteP> searchByMatch(Long projectId, Long userId, String apisName,
      Pageable pageable);

}
