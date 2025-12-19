package cloud.xcan.angus.core.tester.application.query.report.impl;

import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.report.ReportQuery;
import cloud.xcan.angus.core.tester.application.query.report.ReportSearch;
import cloud.xcan.angus.core.tester.domain.report.ReportInfo;
import cloud.xcan.angus.core.tester.domain.report.ReportSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ReportSearchImpl implements ReportSearch {

  @Resource
  private ReportSearchRepo reportSearchRepo;

  @Resource
  private ReportQuery reportQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public Page<ReportInfo> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<ReportInfo> clz, String... matches) {
    return new BizTemplate<Page<ReportInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<ReportInfo> process() {
        Page<ReportInfo> page = reportSearchRepo.find(criteria, pageable, clz, matches);
        if (page.isEmpty()) {
          return page;
        }

        // Set the current user report permissions
        reportQuery.setReportInfoCurrentAuths(page.getContent());
        return page;
      }
    }.execute();
  }
}
