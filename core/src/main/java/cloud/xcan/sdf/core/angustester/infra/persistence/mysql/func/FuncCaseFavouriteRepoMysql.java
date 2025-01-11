package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.func;

import cloud.xcan.sdf.core.angustester.domain.func.favourite.FuncCaseFavouriteP;
import cloud.xcan.sdf.core.angustester.domain.func.favourite.FuncCaseFavouriteRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncCaseFavouriteRepoMysql extends FuncCaseFavouriteRepo {

  @Query(value =
      "select af.id id, a.id caseId,a.name caseName, a.project_id projectId, a.plan_id planId, a.code "
          + "FROM func_case_favourite af, func_case a WHERE a.id = af.case_id "
          + "AND af.project_id = ?1 AND af.created_by = ?2 AND MATCH(a.name,a.description,a.code) AGAINST (?3 IN BOOLEAN MODE) "
          + "AND a.plan_deleted_flag = 0 AND a.deleted_flag = 0 ",
      countQuery =
          "select count(af.id) FROM func_case_favourite af, func_case a WHERE a.id = af.case_id "
              + "AND af.project_id = ?1 AND af.created_by = ?2 AND MATCH(a.name,a.description,a.code) AGAINST (?3 IN BOOLEAN MODE) "
              + "AND a.plan_deleted_flag = 0 AND a.deleted_flag = 0 ",
      nativeQuery = true)
  Page<FuncCaseFavouriteP> searchByMatch(Long projectId, Long userId, String name,
      Pageable pageable);

}
