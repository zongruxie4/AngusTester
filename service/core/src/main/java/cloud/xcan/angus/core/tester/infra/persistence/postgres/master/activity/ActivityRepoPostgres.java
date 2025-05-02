package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.activity;

import cloud.xcan.angus.core.tester.domain.activity.ActivityRepo;
import org.springframework.stereotype.Repository;

@Repository("activityRepo")
public interface ActivityRepoPostgres extends ActivityRepo {


}
