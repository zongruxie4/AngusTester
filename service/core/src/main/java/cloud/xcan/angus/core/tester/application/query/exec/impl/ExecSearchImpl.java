package cloud.xcan.angus.core.tester.application.query.exec.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.SneakyThrow0;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSearch;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfoSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ExecSearchImpl implements ExecSearch {

  @Resource
  private ExecInfoSearchRepo execSearchRepo;

  @Resource
  private ExecSampleQuery execSampleQuery;

  @Resource
  private ExecQuery execQuery;

  @SneakyThrow0(level = "WARN") // Check exec quota in running
  @Override
  public Page<ExecInfo> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ExecInfo> clz, String... matches) {
    return new BizTemplate<Page<ExecInfo>>() {
      String projectId;

      @Override
      protected void checkParams() {
        projectId = findFirstValue(criteria, "projectId", SearchOperation.EQUAL);
        assertNotNull(projectId, "projectId must not be null");
      }

      @Override
      protected Page<ExecInfo> process() {
        Page<ExecInfo> page = execSearchRepo.find(criteria, pageable, clz, matches);
        if (page.hasContent()) {
          execQuery.setExecInfoScriptName(page.getContent());
          execQuery.setExecInfoCurrentOperationPermission(page.getContent());
          execSampleQuery.setExecInfoLatestTotalMergeSample(page.getContent());
        }
        return page;
      }
    }.execute();
  }
}
