package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.project;

import cloud.xcan.angus.core.tester.domain.project.tag.TagRepo;
import org.springframework.stereotype.Repository;

@Repository("tagRepo")
public interface TagRepoMysql extends TagRepo {

}
