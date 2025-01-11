package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.tag;

import cloud.xcan.sdf.core.angustester.domain.tag.TagRepo;
import org.springframework.stereotype.Repository;

@Repository("tagRepo")
public interface TagRepoMysql extends TagRepo {

}
