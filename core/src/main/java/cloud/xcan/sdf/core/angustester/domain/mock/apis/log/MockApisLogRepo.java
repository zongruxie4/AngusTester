package cloud.xcan.sdf.core.angustester.domain.mock.apis.log;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xiaolong.liu
 */
@NoRepositoryBean
public interface MockApisLogRepo extends BaseRepository<MockApisLog, Long> {

  @Query(value = "SELECT a0.mock_apis_id FROM mock_apis_log a0 GROUP BY a0.mock_apis_id HAVING(count(a0.mock_apis_id) > ?1) LIMIT ?2", nativeQuery = true)
  List<Long> getApisIdsHavingCount(Long reservedNum, Long batchNum);

  @Transactional // Required
  @Modifying
  @Query(value = "DELETE FROM mock_apis_log WHERE mock_apis_id = ?1 AND id NOT IN "
      + "(SELECT id FROM (SELECT id FROM mock_apis_log WHERE mock_apis_id = ?1 ORDER BY id DESC LIMIT ?2) as a)", nativeQuery = true)
  void deleteByApisIdAndCount(Long apisId, Long reservedNum);

  @Transactional // Required
  @Modifying
  @Query(value = "DELETE FROM mock_apis_log WHERE mock_service_id IN ?1", nativeQuery = true)
  void deleteAllByServiceIdIn(Collection<Long> serviceIds);
}
