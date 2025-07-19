package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_UNARCHIVED_NO_PERMISSION_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_UNARCHIVED_NO_PERMISSION_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.converter.ApisConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisUnarchivedQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedListRepo;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedRepo;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
@Slf4j
@SummaryQueryRegister(name = "ApisUnarchived", table = "apis_unarchived",
    groupByColumns = {"created_date", "method"})
public class ApisUnarchivedQueryImpl implements ApisUnarchivedQuery {

  @Resource
  private ApisUnarchivedRepo apisUnarchivedRepo;

  @Resource
  private ApisUnarchivedListRepo apisUnarchivedListRepo;

  @Resource
  private ApisUnarchivedSearchRepo apisUnarchivedSearchRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public ApisUnarchived detail(Long id) {
    return new BizTemplate<ApisUnarchived>() {

      @Override
      protected ApisUnarchived process() {
        return checkAndFind(id);
      }
    }.execute();
  }

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? apisUnarchivedRepo.countByCreatedBy(getUserId())
            : apisUnarchivedRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

  @Override
  public Page<ApisUnarchived> list(GenericSpecification<ApisUnarchived> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ApisUnarchived>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<ApisUnarchived> process() {
        Set<SearchCriteria> criteria = spec.getCriteria();
        criteria.add(SearchCriteria.equal("createdBy", getUserId()));
        return fullTextSearch
            ? apisUnarchivedSearchRepo.find(criteria, pageable, ApisUnarchived.class,
            ApisConverter::objectArrToApisUnarchived, match)
            : apisUnarchivedListRepo.find(criteria, pageable, ApisUnarchived.class,
                ApisConverter::objectArrToApisUnarchived, null);
      }
    }.execute();
  }

  @Override
  public ApisUnarchived checkAndFind(Long id) {
    return apisUnarchivedRepo.findByCreatedByAndId(getUserId(), id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ApisUnarchived"));
  }

  /**
   * Allow only self modification of not archived apis.
   */
  @Override
  public void checkUpdateApiPermission(List<ApisUnarchived> apis) {
    List<Long> existIds = apisUnarchivedRepo.findApisUnarchiveCreateBy(
        apis.stream().map(ApisUnarchived::getId).collect(Collectors.toSet()), getUserId());

    List<Long> requestIds = apis.stream().map(ApisUnarchived::getId).collect(Collectors.toList());
    requestIds.removeAll(existIds);
    BizAssert.assertTrue(isEmpty(requestIds), APIS_UNARCHIVED_NO_PERMISSION_CODE,
        APIS_UNARCHIVED_NO_PERMISSION_T, new Object[]{requestIds});
  }

}
