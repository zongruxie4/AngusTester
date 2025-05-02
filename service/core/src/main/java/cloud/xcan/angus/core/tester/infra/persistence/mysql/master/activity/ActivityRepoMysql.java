package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.activity;

import cloud.xcan.angus.core.tester.domain.activity.ActivityRepo;
import org.springframework.stereotype.Repository;

@Repository("activityRepo")
public interface ActivityRepoMysql extends ActivityRepo {


}
