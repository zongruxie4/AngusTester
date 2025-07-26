package cloud.xcan.angus.core.tester.application.query.indicator.impl;


import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.IndicatorFuncConverter.toIndicatorFunc;
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
import cloud.xcan.angus.core.tester.application.converter.IndicatorFuncConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorFuncQuery;
import cloud.xcan.angus.core.tester.domain.CombinedTarget;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFunc;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFuncListRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFuncRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFuncSearchRepo;
import cloud.xcan.angus.remote.message.SysException;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of IndicatorFuncQuery for managing functional test indicator queries.
 * <p>
 * This class provides comprehensive functionality for querying and managing functional
 * test indicators, which define performance metrics and thresholds for functional testing.
 * It handles indicator retrieval, default value fallback, and target information enrichment.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Functional indicator retrieval with target validation</li>
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
public class IndicatorFuncQueryImpl implements IndicatorFuncQuery {

  @Resource
  private IndicatorFuncRepo indicatorFuncRepo;
  @Resource
  private IndicatorFuncListRepo indicatorFuncListRepo;
  @Resource
  private IndicatorFuncSearchRepo indicatorPerfSearchRepo;
  @Resource
  private SettingTenantManager settingTenantManager;
  @Resource
  private CommonQuery commonQuery;

  /**
   * Finds a functional indicator by target ID and type with validation.
   * <p>
   * Retrieves a functional indicator for the specified target and validates its existence.
   * Enriches the indicator with target name information for complete context.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param targetId the target ID to find the indicator for
   * @param targetType the target type (e.g., PROJECT, PLAN, CASE)
   * @return IndicatorFunc object with enriched target information
   * @throws ResourceNotFound if the indicator is not found
   */
  @Override
  public IndicatorFunc find(Long targetId, CombinedTargetType targetType) {
    return new BizTemplate<IndicatorFunc>() {

      @Override
      protected IndicatorFunc process() {
        // Retrieve functional indicator from database
        IndicatorFunc funcDb = indicatorFuncRepo.findByTargetIdAndTargetType(targetId, targetType);
        
        // Validate that the indicator exists
        assertResourceNotFound(isNotEmpty(funcDb), targetId, "IndicatorFunc");
        
        // Enrich indicator with target name information
        assembleTargetName(funcDb, targetType, targetId);
        return funcDb;
      }
    }.execute();
  }

  /**
   * Retrieves functional indicator details or falls back to platform defaults.
   * <p>
   * First attempts to find user-configured indicators, then falls back to platform
   * default indicators if none are found. Handles tenant-specific configuration
   * and target information enrichment.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param targetType the target type for the indicator
   * @param targetId the target ID for the indicator
   * @return IndicatorFunc object with either user or platform default configuration
   * @throws SysException if platform configuration retrieval fails
   */
  @Override
  public IndicatorFunc detailOrDefault(CombinedTargetType targetType, Long targetId) {
    return new BizTemplate<IndicatorFunc>() {

      @Override
      protected IndicatorFunc process() {
        // First query whether there are functional indicators set by the user
        IndicatorFunc funcDb = indicatorFuncRepo.findByTargetIdAndTargetType(targetId, targetType);
        if (nonNull(funcDb)) {
          // Return user-configured indicator if found
          return funcDb;
        }

        // Query the default functional indicators of the platform
        try {
          // Retrieve tenant-specific platform settings
          SettingTenant setting = settingTenantManager.checkAndFindSetting(getOptTenantId());
          
          // Convert platform data to indicator format
          IndicatorFunc func = toIndicatorFunc(setting.getFuncData(), targetId, targetType);
          
          // Enrich indicator with target name information
          assembleTargetName(func, targetType, targetId);
          return func;
        } catch (Exception e) {
          // Handle platform configuration retrieval failures
          throw SysException.of(INDICATOR_GET_PLATFORM_FAIL_CODE, INDICATOR_GET_PLATFORM_FAIL);
        }
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of functional indicators with search capabilities.
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
   * @return Page of IndicatorFunc objects with converted data
   */
  @Override
  public Page<IndicatorFunc> list(GenericSpecification<IndicatorFunc> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<IndicatorFunc>>() {
      @Override
      protected Page<IndicatorFunc> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriteria());

        // Execute search based on search type with appropriate converter
        return fullTextSearch ? indicatorPerfSearchRepo.find(spec.getCriteria(),
            pageable, IndicatorFunc.class, IndicatorFuncConverter::objectArrToFunc, match)
            : indicatorFuncListRepo.find(spec.getCriteria(),
                pageable, IndicatorFunc.class, IndicatorFuncConverter::objectArrToFunc, null);
      }
    }.execute();
  }

  /**
   * Finds a functional indicator by target ID and type without validation.
   * <p>
   * Retrieves a functional indicator without existence validation or target enrichment.
   * Used for internal operations where validation is handled separately.
   *
   * @param targetId the target ID to find the indicator for
   * @param targetType the target type for the indicator
   * @return IndicatorFunc object or null if not found
   */
  @Override
  public IndicatorFunc find0(Long targetId, CombinedTargetType targetType) {
    return indicatorFuncRepo.findByTargetIdAndTargetType(targetId, targetType);
  }

  /**
   * Enriches indicator with target name information.
   * <p>
   * Retrieves and associates target name with the indicator for complete data context.
   * Uses common query service for target information retrieval.
   *
   * @param func the indicator to enrich with target name
   * @param targetType the target type for name retrieval
   * @param targetId the target ID for name retrieval
   */
  private void assembleTargetName(IndicatorFunc func, CombinedTargetType targetType,
      Long targetId) {
    // Retrieve combined target information including name
    CombinedTarget combinedTarget = commonQuery.getCombinedTarget(targetType, targetId, true);
    
    // Set target name on the indicator
    func.setTargetName(combinedTarget.getTargetName());
  }
}
