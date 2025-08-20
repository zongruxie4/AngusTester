package cloud.xcan.angus.core.tester.application.query.indicator.impl;


import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.IndicatorPerfConverter.toIndicatorPerf;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL_CODE;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.setting.tenant.SettingTenant;
import cloud.xcan.angus.api.manager.SettingTenantManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.converter.IndicatorPerfConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorPerfQuery;
import cloud.xcan.angus.core.tester.domain.CombinedTarget;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerf;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfSearchRepo;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of IndicatorPerfQuery for managing performance test indicator queries.
 * <p>
 * This class provides comprehensive functionality for querying and managing performance
 * test indicators, which define performance metrics and thresholds for performance testing.
 * It handles indicator retrieval, default value fallback, and target information enrichment.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Performance indicator retrieval with target validation</li>
 *   <li>Default platform indicator fallback mechanism</li>
 *   <li>Paginated indicator listing with search capabilities</li>
 *   <li>Target name enrichment and association</li>
 *   <li>Authorization-based access control</li>
 *   <li>Tenant-specific configuration management</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for bulk operations and configuration retrieval.
 * <p>
 * Supports both individual indicator operations and bulk operations with proper error handling
 * and resource validation.
 */
@Biz
public class IndicatorPerfQueryImpl implements IndicatorPerfQuery {

  @Resource
  private IndicatorPerfRepo indicatorPerfRepo;
  @Resource
  private IndicatorPerfListRepo indicatorPerfListRepo;
  @Resource
  private IndicatorPerfSearchRepo indicatorPerfSearchRepo;
  @Resource
  private SettingTenantManager settingTenantManager;
  @Resource
  private CommonQuery commonQuery;

  /**
   * Finds a performance indicator by target ID and type with validation.
   * <p>
   * Retrieves a performance indicator for the specified target and validates its existence.
   * Enriches the indicator with target name information for complete context.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param targetId the target ID to find the indicator for
   * @param targetType the target type (e.g., PROJECT, PLAN, CASE)
   * @return IndicatorPerf object with enriched target information
   * @throws ResourceNotFound if the indicator is not found
   */
  @Override
  public IndicatorPerf find(Long targetId, CombinedTargetType targetType) {
    return new BizTemplate<IndicatorPerf>() {

      @Override
      protected IndicatorPerf process() {
        // Retrieve performance indicator from database
        IndicatorPerf perfDb = indicatorPerfRepo.findByTargetIdAndTargetType(targetId, targetType);

        // Validate that the indicator exists
        assertResourceNotFound(isNotEmpty(perfDb), targetId, "IndicatorPerf");

        // Enrich indicator with target name information
        assembleTargetName(perfDb, targetType, targetId);
        return perfDb;
      }
    }.execute();
  }

  /**
   * Retrieves performance indicator details or falls back to platform defaults.
   * <p>
   * First attempts to find user-configured indicators, then falls back to platform
   * default indicators if none are found. Handles tenant-specific configuration
   * and target information enrichment.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param targetType the target type for the indicator
   * @param targetId the target ID for the indicator
   * @return IndicatorPerf object with either user or platform default configuration
   * @throws SysException if platform configuration retrieval fails
   */
  @Override
  public IndicatorPerf detailOrDefault(CombinedTargetType targetType, Long targetId) {
    return new BizTemplate<IndicatorPerf>() {

      @Override
      protected IndicatorPerf process() {
        // First query whether there are performance indicators set by the user
        IndicatorPerf perfDb = indicatorPerfRepo.findByTargetIdAndTargetType(targetId, targetType);
        if (nonNull(perfDb)) {
          // Return user-configured indicator if found
          return perfDb;
        }

        // Query the default performance indicators of the platform
        try {
          // Retrieve tenant-specific platform settings
          SettingTenant setting = settingTenantManager.checkAndFindSetting(getOptTenantId());

          // Convert platform data to indicator format
          IndicatorPerf perf = toIndicatorPerf(setting.getPerfData(), targetId, targetType);

          // Enrich indicator with target name information
          assembleTargetName(perf, targetType, targetId);
          return perf;
        } catch (Exception e) {
          // Handle platform configuration retrieval failures
          throw SysException.of(INDICATOR_GET_PLATFORM_FAIL_CODE, INDICATOR_GET_PLATFORM_FAIL);
        }
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of performance indicators with search capabilities.
   * <p>
   * Supports both regular search and full-text search with comprehensive filtering.
   * Applies authorization-based access control and converts database results to
   * indicator objects.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param spec the search specification with criteria and filters
   * @param pageable pagination parameters (page, size, sort)
   * @param fullTextSearch whether to use full-text search capabilities
   * @param match full-text search match parameters
   * @return Page of IndicatorPerf objects with converted data
   */
  @Override
  public Page<IndicatorPerf> list(GenericSpecification<IndicatorPerf> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<IndicatorPerf>>() {

      @Override
      protected Page<IndicatorPerf> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriteria());

        // Execute search based on search type with appropriate converter
        return fullTextSearch ?
            indicatorPerfSearchRepo.find(spec.getCriteria(), pageable, IndicatorPerf.class,
                IndicatorPerfConverter::objectArrToPerf, match)
            : indicatorPerfListRepo.find(spec.getCriteria(),
                pageable, IndicatorPerf.class, IndicatorPerfConverter::objectArrToPerf, null);
      }
    }.execute();
  }

  /**
   * Finds a performance indicator by target ID and type without validation.
   * <p>
   * Retrieves a performance indicator without existence validation or target enrichment.
   * Used for internal operations where validation is handled separately.
   *
   * @param targetId the target ID to find the indicator for
   * @param targetType the target type for the indicator
   * @return IndicatorPerf object or null if not found
   */
  @Override
  public IndicatorPerf find0(Long targetId, CombinedTargetType targetType) {
    return indicatorPerfRepo.findByTargetIdAndTargetType(targetId, targetType);
  }

  /**
   * Enriches indicator with target name information.
   * <p>
   * Retrieves and associates target name with the indicator for complete data context.
   * Uses common query service for target information retrieval.
   *
   * @param perf the indicator to enrich with target name
   * @param targetType the target type for name retrieval
   * @param targetId the target ID for name retrieval
   */
  private void assembleTargetName(IndicatorPerf perf, CombinedTargetType targetType,
      Long targetId) {
    // Retrieve combined target information including name
    CombinedTarget combinedTarget = commonQuery.getCombinedTarget(targetType, targetId, true);

    // Set target name on the indicator
    perf.setTargetName(combinedTarget.getTargetName());
  }
}
