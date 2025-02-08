package cloud.xcan.sdf.core.angustester.application.query.apis.impl;

import static cloud.xcan.sdf.core.angustester.application.converter.ApisTestConverter.assembleApisTestCount;
import static cloud.xcan.sdf.core.angustester.application.converter.ApisTestConverter.assembleTestApisCount;
import static cloud.xcan.sdf.core.utils.CoreUtils.getCommonDeletedResourcesStatsFilter;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisTestQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.kanban.TestApisCount;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.model.services.ApisTestCount;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

@Biz
public class ApisTestQueryImpl implements ApisTestQuery {

  @Resource
  private ApisBaseInfoRepo apisBaseInfoRepo;

  @Resource
  private UserManager userManager;

  @Override
  public List<TestType> findEnabledTestTypes(Long apisId) {
    return new BizTemplate<List<TestType>>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        apisDb = apisBaseInfoRepo.findById(apisId)
            .orElseThrow(() -> ResourceNotFound.of(apisId, "Apis"));
      }

      @Override
      protected List<TestType> process() {
        List<TestType> enabledTestTypes = new ArrayList<>();
        if (nonNull(apisDb.getTestFuncFlag()) && apisDb.getTestFuncFlag()) {
          enabledTestTypes.add(TestType.FUNCTIONAL);
        }
        if (nonNull(apisDb.getTestPerfFlag()) && apisDb.getTestPerfFlag()) {
          enabledTestTypes.add(TestType.PERFORMANCE);
        }
        if (nonNull(apisDb.getTestStabilityFlag()) && apisDb.getTestStabilityFlag()) {
          enabledTestTypes.add(TestType.STABILITY);
        }
        return enabledTestTypes;
      }
    }.execute();
  }

  @Override
  public ApisTestCount countServiceTestApis(Long serviceId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<ApisTestCount>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected ApisTestCount process() {
        ApisTestCount count = new ApisTestCount();

        List<ApisBaseInfo> apis = getServiceApisBaseInfos(serviceId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        if (isNotEmpty(apis)) {
          assembleApisTestCount(count, apis);
        }
        return count;
      }
    }.execute();
  }

  @Override
  public ApisTestCount countProjectTestApis(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<ApisTestCount>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected ApisTestCount process() {
        ApisTestCount count = new ApisTestCount();

        List<ApisBaseInfo> apis = getProjectApisBaseInfos(projectId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        if (isNotEmpty(apis)) {
          assembleApisTestCount(count, apis);
        }
        return count;
      }
    }.execute();
  }

  @Override
  public TestApisCount countTestResult(Long projectId, AuthObjectType creatorObjectType,
      Long creatorObjectId, LocalDateTime createdDateStart, LocalDateTime createdDateEnd) {
    return new BizTemplate<TestApisCount>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected TestApisCount process() {
        TestApisCount count = new TestApisCount();

        List<ApisBaseInfo> apis = getProjectApisBaseInfos(projectId, creatorObjectType,
            creatorObjectId, createdDateStart, createdDateEnd);
        if (isNotEmpty(apis)) {
          assembleTestApisCount(count, apis);
        }
        return count;
      }
    }.execute();
  }

  private List<ApisBaseInfo> getServiceApisBaseInfos(Long serviceId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    Set<Long> createdBys = null;
    if (nonNull(creatorObjectType) && nonNull(creatorObjectId)) {
      createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
    }

    Set<SearchCriteria> allFilters = getCommonDeletedResourcesStatsFilter(null,
        createdDateStart, createdDateEnd, createdBys);
    allFilters.add(SearchCriteria.equal("serviceId", serviceId));

    return apisBaseInfoRepo.findAllByFilters(allFilters, Sort.by(Order.desc("id")));
  }

  private List<ApisBaseInfo> getProjectApisBaseInfos(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    Set<Long> createdBys = null;
    if (nonNull(creatorObjectType) && nonNull(creatorObjectId)) {
      createdBys = userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);
    }

    Set<SearchCriteria> allFilters = getCommonDeletedResourcesStatsFilter(projectId,
        createdDateStart, createdDateEnd, createdBys);
    return apisBaseInfoRepo.findAllByFilters(allFilters, Sort.by(Order.desc("id")));
  }

}
