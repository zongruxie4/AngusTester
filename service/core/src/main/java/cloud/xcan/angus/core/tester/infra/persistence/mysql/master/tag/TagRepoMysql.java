package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.tag;

import cloud.xcan.angus.core.tester.domain.tag.TagRepo;
import org.springframework.stereotype.Repository;

@Repository("tagRepo")
public interface TagRepoMysql extends TagRepo {

}
