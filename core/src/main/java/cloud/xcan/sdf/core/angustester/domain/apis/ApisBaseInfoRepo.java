package cloud.xcan.sdf.core.angustester.domain.apis;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Make the query include logic delete data
 */
@NoRepositoryBean
public interface ApisBaseInfoRepo extends BaseRepository<ApisBaseInfo, Long>,
    NameJoinRepository<ApisBaseInfo, Long> {

  @Override
  Optional<ApisBaseInfo> findById(Long id);

  List<ApisBaseInfo> findAllByIdIn(Collection<Long> ids);

  List<ApisBaseInfo> findByServiceId(Long serviceId);

  /**
   * Note: Logical deletion data is included
   */
  @Query(value = "SELECT * FROM apis WHERE id = ?1", nativeQuery = true)
  ApisBaseInfo find0ById(Long id);

  /**
   * Note: Logical deletion data is included
   */
  @Query(value = "SELECT * FROM apis WHERE id IN ?1", nativeQuery = true)
  List<ApisBaseInfo> findAll0ByIdIn(Collection<Long> ids);

  List<ApisBaseInfo> findAllByServiceIdAndEndpointIn(Long serviceId, List<String> endpoint);

  @Query(value = "SELECT id FROM apis WHERE id IN ?1 AND (auth_flag = ?2 OR service_auth_flag = ?2) ", nativeQuery = true)
  List<Long> findIds0ByIdInAndAuthFlag(Collection<Long> apis, boolean authFlag);

  @Query(value = "SELECT id FROM apis WHERE service_id = ?1 ", nativeQuery = true)
  List<Long> findIdByServiceId(Long serviceId);

}
