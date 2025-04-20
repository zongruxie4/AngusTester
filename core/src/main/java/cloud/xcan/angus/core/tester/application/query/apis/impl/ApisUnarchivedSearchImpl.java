package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.converter.ApisConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisUnarchivedSearch;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ApisUnarchivedSearchImpl implements ApisUnarchivedSearch {

  @Resource
  private ApisUnarchivedSearchRepo apisUnarchivedSearchRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public Page<ApisUnarchived> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ApisUnarchived> apisClass, String... matches) {
    return new BizTemplate<Page<ApisUnarchived>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<ApisUnarchived> process() {
        criteria.add(SearchCriteria.equal("createdBy", getUserId()));
        return apisUnarchivedSearchRepo.find(criteria, pageable, apisClass,
            ApisConverter::objectArrToApisUnarchived, matches);
      }
    }.execute();
  }

}




