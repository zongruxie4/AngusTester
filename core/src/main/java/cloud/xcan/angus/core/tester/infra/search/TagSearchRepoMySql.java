package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.tag.Tag;
import cloud.xcan.angus.core.tester.domain.tag.TagSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class TagSearchRepoMySql extends SimpleSearchRepository<Tag> implements
    TagSearchRepo {

}
