package cloud.xcan.sdf.core.angustester.application.query.mock.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceSearch;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceInfo;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class MockServiceSearchImpl implements MockServiceSearch {

  @Resource
  private MockServiceSearchRepo mockServiceSearchRepo;

  @Resource
  private MockServiceQuery mockServiceQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public Page<MockServiceInfo> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<MockServiceInfo> clz, String... matches) {
    return new BizTemplate<Page<MockServiceInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<MockServiceInfo> process() {
        Page<MockServiceInfo> page = mockServiceSearchRepo.find(criterias, pageable, clz, matches);
        if (page.isEmpty()) {
          return page;
        }

        // Set mock service status
        mockServiceQuery.setMockServiceInfoStatus(page.getContent());

        // Set the current user service permissions
        mockServiceQuery.setMockServiceInfoCurrentAuths(page.getContent());

        // Set node info
        mockServiceQuery.setInfoNodeInfo(page.getContent());
        return page;
      }
    }.execute();
  }

}
