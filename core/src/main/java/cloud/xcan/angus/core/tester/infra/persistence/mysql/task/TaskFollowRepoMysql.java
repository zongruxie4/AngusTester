package cloud.xcan.angus.core.tester.infra.persistence.mysql.task;

import cloud.xcan.angus.core.tester.domain.task.follow.TaskFollowP;
import cloud.xcan.angus.core.tester.domain.task.follow.TaskFollowRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskFollowRepoMysql extends TaskFollowRepo {

  @Override
  @Query(value =
      "select sf.id id, s.id taskId, s.name taskName, s.project_id projectId, s.sprint_id sprintId, s.task_type taskType, s.code taskCode "
          + "FROM task_follow sf, task s WHERE s.id = sf.task_id "
          + "AND sf.project_id = ?1 AND sf.created_by = ?2 AND MATCH(s.name,s.code) AGAINST (?3 IN BOOLEAN MODE) "
          + "AND s.sprint_deleted = 0 AND s.deleted = 0",
      countQuery = "select count(sf.id) FROM task_follow sf, task s WHERE s.id = sf.task_id "
          + "AND sf.project_id = ?1 AND sf.created_by = ?2 AND MATCH(s.name,s.code) AGAINST (?3 IN BOOLEAN MODE) "
          + "AND s.sprint_deleted = 0 AND s.deleted = 0",
      nativeQuery = true)
  Page<TaskFollowP> searchByMatch(Long projectId, Long userId, String taskName, Pageable pageable);

}
