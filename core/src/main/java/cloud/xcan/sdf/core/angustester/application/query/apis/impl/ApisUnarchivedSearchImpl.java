package cloud.xcan.sdf.core.angustester.application.query.apis.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.converter.ApisConverter;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisUnarchivedSearch;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchivedSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ApisUnarchivedSearchImpl implements ApisUnarchivedSearch {

  @Resource
  private ApisUnarchivedSearchRepo apisUnarchivedSearchRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public Page<ApisUnarchived> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<ApisUnarchived> apisClass, String... matches) {
    return new BizTemplate<Page<ApisUnarchived>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<ApisUnarchived> process() {
        criterias.add(SearchCriteria.equal("createdBy", getUserId()));
        return apisUnarchivedSearchRepo.find(criterias, pageable, apisClass,
            ApisConverter::objectArrToApisUnarchived, matches);
      }
    }.execute();
  }

}




