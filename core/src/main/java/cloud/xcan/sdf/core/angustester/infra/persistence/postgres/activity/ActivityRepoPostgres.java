package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.activity;

import cloud.xcan.sdf.core.angustester.domain.activity.ActivityRepo;
import org.springframework.stereotype.Repository;

@Repository("activityRepo")
public interface ActivityRepoPostgres extends ActivityRepo {


}
