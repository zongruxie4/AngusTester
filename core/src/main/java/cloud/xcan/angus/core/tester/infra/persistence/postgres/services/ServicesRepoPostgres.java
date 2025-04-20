package cloud.xcan.angus.core.tester.infra.persistence.postgres.services;

import cloud.xcan.angus.core.tester.domain.services.ServicesRepo;
import org.springframework.stereotype.Repository;

@Repository("servicesRepo")
public interface ServicesRepoPostgres extends ServicesRepo {


}
