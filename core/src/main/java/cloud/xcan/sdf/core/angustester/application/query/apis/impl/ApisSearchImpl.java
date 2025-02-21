package cloud.xcan.sdf.core.angustester.application.query.apis.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.converter.ApisConverter;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisSearch;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisInfoSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ApisSearchImpl implements ApisSearch {

  @Resource
  private ApisInfoSearchRepo apisInfoSearchRepo;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<ApisBasicInfo> searchByServiceId(Long serviceId, Set<SearchCriteria> criteria,
      Pageable pageable, String... matches) {
    return new BizTemplate<Page<ApisBasicInfo>>() {
      @Override
      protected void checkParams() {
        // Check the view permissions
        servicesAuthQuery.checkViewAuth(getUserId(), serviceId);
      }

      @Override
      protected Page<ApisBasicInfo> process() {
        criteria.add(SearchCriteria.equal("serviceId", serviceId));
        return search0(criteria, pageable, matches);
      }
    }.execute();
  }

  @Override
  public Page<ApisBasicInfo> search(Set<SearchCriteria> criteria, Pageable pageable,
      String... matches) {
    return new BizTemplate<Page<ApisBasicInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<ApisBasicInfo> process() {
        return search0(criteria, pageable, matches);
      }
    }.execute();
  }

  private Page<ApisBasicInfo> search0(Set<SearchCriteria> criteria, Pageable pageable,
      String... matches) {
    criteria.add(SearchCriteria.equal("deletedFlag", false));
    criteria.add(SearchCriteria.equal("serviceDeletedFlag", false));

    // Set authorization conditions when you are not an administrator or only query yourself
    commonQuery.checkAndSetAuthObjectIdCriteria(criteria);

    Page<ApisBasicInfo> page = apisInfoSearchRepo.find(criteria, pageable, ApisBasicInfo.class,
        ApisConverter::objectArrToApis, matches);

    if (page.hasContent()) {
      if (isUserAction()) {
        // Set favourite state
        apisQuery.setFavourite(page.getContent());
        // Set follow state
        apisQuery.setFollow(page.getContent());
      }

      // Set associated mock apis
      apisQuery.setInfoAssocMockApis(page.getContent());
      // Set user name and avatar
      userManager.setUserNameAndAvatar(page.getContent(), "createdBy", "createdByName", "avatar");
    }
    return page;
  }
}
