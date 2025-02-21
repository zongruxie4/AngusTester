package cloud.xcan.sdf.core.angustester.application.query.tag.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagQuery;
import cloud.xcan.sdf.core.angustester.application.query.tag.TagSearch;
import cloud.xcan.sdf.core.angustester.domain.tag.Tag;
import cloud.xcan.sdf.core.angustester.domain.tag.TagSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class TagSearchImpl implements TagSearch {

  @Resource
  private TagSearchRepo tagSearchRepo;

  @Resource
  private TagQuery tagQuery;

  @Override
  public Page<Tag> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<Tag> clz, String... matches) {
    return new BizTemplate<Page<Tag>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Tag> process() {
        Page<Tag> page = tagSearchRepo.find(criteria, pageable, clz, matches);
        tagQuery.setEditPermissionFlag(criteria, page.getContent());
        return page;
      }
    }.execute();
  }
}
