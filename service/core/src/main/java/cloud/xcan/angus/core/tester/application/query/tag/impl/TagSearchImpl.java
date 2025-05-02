package cloud.xcan.angus.core.tester.application.query.tag.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.tag.TagQuery;
import cloud.xcan.angus.core.tester.application.query.tag.TagSearch;
import cloud.xcan.angus.core.tester.domain.tag.Tag;
import cloud.xcan.angus.core.tester.domain.tag.TagSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
      protected Page<Tag> process() {
        Page<Tag> page = tagSearchRepo.find(criteria, pageable, clz, matches);
        tagQuery.setEditPermission(criteria, page.getContent());
        return page;
      }
    }.execute();
  }
}
