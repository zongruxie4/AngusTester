package cloud.xcan.angus.core.tester.application.query.test.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.FuncCaseConverter.getCommonCreatorResourcesFilter;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.test.FuncBaselineQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineInfo;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineInfoRepo;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineInfoSearchRepo;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of FuncBaselineQuery for managing baseline queries.
 * <p>
 * Provides methods to retrieve, list, and check baselines and their info, including permission checks and summary statistics.
 */
@Biz
public class FuncBaselineQueryImpl implements FuncBaselineQuery {

  @Resource
  private FuncBaselineRepo funcBaselineRepo;
  @Resource
  private FuncBaselineInfoSearchRepo funcBaselineInfoSearchRepo;
  @Resource
  private FuncBaselineInfoRepo funcBaselineInfoRepo;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private UserManager userManager;

  /**
   * Retrieves detailed information for a baseline by ID.
   *
   * @param id the baseline ID
   * @return FuncBaseline object
   */
  @Override
  public FuncBaseline detail(Long id) {
    return new BizTemplate<FuncBaseline>() {
      FuncBaseline baselineDb;

      @Override
      protected void checkParams() {
        baselineDb = checkAndFind(id);
      }

      @Override
      protected FuncBaseline process() {
        return baselineDb;
      }
    }.execute();
  }

  /**
   * Finds baseline info with optional full-text search.
   * <p>
   * Checks project member permission and enriches with user info.
   *
   * @param spec search specification
   * @param pageable pagination
   * @param fullTextSearch whether to use full-text search
   * @param match search match terms
   * @return paginated result of FuncBaselineInfo
   */
  @Override
  public Page<FuncBaselineInfo> find(GenericSpecification<FuncBaselineInfo> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<FuncBaselineInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<FuncBaselineInfo> process() {
        Page<FuncBaselineInfo> page = fullTextSearch
            ? funcBaselineInfoSearchRepo.find(spec.getCriteria(), pageable,
            FuncBaselineInfo.class, match)
            : funcBaselineInfoRepo.findAll(spec, pageable);

        if (page.hasContent()) {
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy", "createdByName",
              "createdByAvatar");
        }
        return page;
      }
    }.execute();
  }

  /**
   * Checks and finds a baseline by ID, throws if not found.
   *
   * @param id the baseline ID
   * @return FuncBaseline object
   */
  @Override
  public FuncBaseline checkAndFind(Long id) {
    return funcBaselineRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "FuncBaseline"));
  }

  /**
   * Checks and finds baseline info by ID, throws if not found.
   *
   * @param id the baseline info ID
   * @return FuncBaselineInfo object
   */
  @Override
  public FuncBaselineInfo checkAndFindInfo(Long id) {
    return funcBaselineInfoRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "FuncBaseline"));
  }

  /**
   * Checks and finds a list of baselines by IDs, throws if not found.
   *
   * @param ids collection of baseline IDs
   * @return list of FuncBaseline
   */
  @Override
  public List<FuncBaseline> checkAndFind(Collection<Long> ids) {
    List<FuncBaseline> baselines = funcBaselineRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(baselines), ids.iterator().next(), "FuncBaseline");
    if (ids.size() != baselines.size()) {
      for (FuncBaseline baseline : baselines) {
        assertResourceNotFound(ids.contains(baseline.getId()), baseline.getId(), "FuncBaseline");
      }
    }
    return baselines;
  }

  /**
   * Gets baseline summaries created by users/orgs within a time range.
   * <p>
   * Supports filtering by project, plan, creator org, and date range.
   *
   * @param projectId project ID
   * @param planId plan ID
   * @param createdDateStart start date
   * @param createdDateEnd end date
   * @param creatorOrgType creator org type
   * @param creatorOrgId creator org ID
   * @return list of FuncBaseline
   */
  @Override
  public List<FuncBaseline> getBaselineCreatedSummaries(Long projectId, Long planId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> creatorIds = Objects.isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);
    Set<SearchCriteria> allFilters = getCommonCreatorResourcesFilter(projectId, planId,
        createdDateStart, createdDateEnd, creatorIds);
    return funcBaselineRepo.findAllByFilters(allFilters);
  }

}
