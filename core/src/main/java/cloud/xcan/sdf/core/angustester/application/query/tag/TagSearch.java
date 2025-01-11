package cloud.xcan.sdf.core.angustester.application.query.tag;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.tag.Tag;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TagSearch {

  Page<Tag> search(Set<SearchCriteria> criterias, Pageable pageable, Class<Tag> cls,
      String... matches);
}
