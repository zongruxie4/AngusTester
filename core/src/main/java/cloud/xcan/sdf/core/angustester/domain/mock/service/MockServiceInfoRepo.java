package cloud.xcan.sdf.core.angustester.domain.mock.service;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MockServiceInfoRepo extends BaseRepository<MockServiceInfo, Long>,
    NameJoinRepository<MockService, Long> {

  List<MockServiceInfo> findAllByNameIn(Collection<String> names);

  List<MockServiceInfo> findByNodeId(Long nodeId);

  @Query(value = "SELECT * FROM mock_service s LEFT JOIN mock_apis a ON s.id = a.mock_service_id WHERE a.assoc_apis_id = ?1", nativeQuery = true)
  MockServiceInfo findByApisId(Long apisId);

  MockServiceInfo findByAssocServiceId(Long assocProjectId);

  @Query(value = "SELECT id FROM mock_service WHERE id IN ?1 AND auth_flag = ?2 ", nativeQuery = true)
  List<Long> findIds0ByIdInAndAuthFlag(Collection<Long> ids, boolean authFlag);

}
