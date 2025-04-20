package cloud.xcan.angus.core.tester.infra.persistence.postgres.func;

import cloud.xcan.angus.core.tester.domain.func.favourite.FuncCaseFavouriteP;
import cloud.xcan.angus.core.tester.domain.func.favourite.FuncCaseFavouriteRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncCaseFavouriteRepoPostgres extends FuncCaseFavouriteRepo {

  @Override
  @Query(value =
      "select af.id id, a.id caseId,a.name caseName, a.project_id projectId, a.plan_id planId, a.code "
          + "FROM func_case_favourite af, func_case a WHERE a.id = af.case_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.plan_deleted = 0 AND a.deleted = 0 "
          + "AND to_tsvector(a.name,a.description,a.code) @@ plainto_tsquery(?3)",
      countQuery =
          "select count(af.id) FROM func_case_favourite af, func_case a WHERE a.id = af.case_id "
              + "AND af.project_id = ?1 AND af.created_by = ?2 AND a.plan_deleted = 0 AND a.deleted = 0 "
              + "AND to_tsvector(a.name,a.description,a.code) @@ plainto_tsquery(?3)",
      nativeQuery = true)
  Page<FuncCaseFavouriteP> searchByMatch(Long projectId, Long userId, String name,
      Pageable pageable);

}
