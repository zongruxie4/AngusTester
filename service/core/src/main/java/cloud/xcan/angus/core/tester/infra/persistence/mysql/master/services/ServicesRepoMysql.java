package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.services;

import cloud.xcan.angus.core.tester.domain.services.ServicesRepo;
import org.springframework.stereotype.Repository;

@Repository("servicesRepo")
public interface ServicesRepoMysql extends ServicesRepo {


}
