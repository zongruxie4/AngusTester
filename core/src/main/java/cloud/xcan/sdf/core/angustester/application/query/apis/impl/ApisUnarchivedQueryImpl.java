package cloud.xcan.sdf.core.angustester.application.query.apis.impl;

import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.APIS_UNARCHIVED_NO_PERMISSION_CODE;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.APIS_UNARCHIVED_NO_PERMISSION_T;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.converter.ApisConverter;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisUnarchivedQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchivedListRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchivedRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.repository.summary.SummaryQueryRegister;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public ApisUnarchived detail(Long id) {
    return new BizTemplate<ApisUnarchived>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Long process() {
        return isNull(projectId) ? apisUnarchivedRepo.countByCreatedBy(getUserId())
            : apisUnarchivedRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

  @Override
  public Page<ApisUnarchived> find(GenericSpecification<ApisUnarchived> spec,
      Pageable pageable, Class<ApisUnarchived> clz) {
    return new BizTemplate<Page<ApisUnarchived>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriterias());
      }

      @Override
      protected Page<ApisUnarchived> process() {
        Set<SearchCriteria> criteria = spec.getCriterias();
        criteria.add(SearchCriteria.equal("createdBy", getUserId()));
        return apisUnarchivedListRepo.find(criteria, pageable, clz,
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
