package cloud.xcan.angus.core.tester.infra.persistence.postgres.mock;

import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisRepo;
import org.springframework.stereotype.Repository;

@Repository("mockApisRepo")
public interface MockApisRepoPostgres extends MockApisRepo,
    NameJoinRepository<MockApis, Long> {

}
