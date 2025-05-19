package cloud.xcan.angus.core.tester.application.query.mock.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceSearch;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
  public Page<MockServiceInfo> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<MockServiceInfo> clz, String... matches) {
    return new BizTemplate<Page<MockServiceInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<MockServiceInfo> process() {
        Page<MockServiceInfo> page = mockServiceSearchRepo.find(criteria, pageable, clz, matches);
        if (page.isEmpty()) {
          return page;
        }

        // Set node info and the IP address must be set before executing the following code
        mockServiceQuery.setInfoNodeInfo(page.getContent());

        // Set mock service status
        mockServiceQuery.setMockServiceInfoStatus(page.getContent());

        // Set the current user service permissions
        mockServiceQuery.setMockServiceInfoCurrentAuths(page.getContent());

        return page;
      }
    }.execute();
  }

}
