package cloud.xcan.sdf.core.angustester.domain.services.schema;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ServicesSchemaRepo extends BaseRepository<ServicesSchema, Long> {

  boolean existsByServiceId(Long serviceId);

  @Query(value = "SELECT * FROM services_schema WHERE service_id = ?1", nativeQuery = true)
  Optional<ServicesSchema> findByServiceId(Long serviceId);

  @Query(value = "SELECT service_id FROM services_schema WHERE project_id = ?1", nativeQuery = true)
  List<Long> findServiceIdByProjectId(Long projectId);

  @Modifying
  @Query(value = "DELETE FROM services_schema WHERE service_id IN ?1", nativeQuery = true)
  void deleteByServiceIdIn(Collection<Long> serviceIds);

}
