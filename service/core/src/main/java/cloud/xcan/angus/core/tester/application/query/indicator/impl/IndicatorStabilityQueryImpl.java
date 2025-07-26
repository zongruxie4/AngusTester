package cloud.xcan.angus.core.tester.application.query.indicator.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.IndicatorStabilityConverter.toIndicatorStability;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL_CODE;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.setting.tenant.SettingTenant;
import cloud.xcan.angus.api.manager.SettingTenantManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.converter.IndicatorStabilityConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorStabilityQuery;
import cloud.xcan.angus.core.tester.domain.CombinedTarget;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStability;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStabilityListRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStabilityRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStabilitySearchRepo;
import cloud.xcan.angus.remote.message.SysException;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of IndicatorStabilityQuery for managing stability test indicator queries.
 * <p>
 * This class provides comprehensive functionality for querying and managing stability
 * test indicators, which define stability metrics and thresholds for stability testing.
 * It handles indicator retrieval, default value fallback, and target information enrichment.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Stability indicator retrieval with target validation</li>
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
public class IndicatorStabilityQueryImpl implements IndicatorStabilityQuery {

  @Resource
  private IndicatorStabilityRepo indicatorStabilityRepo;
  @Resource
  private IndicatorStabilityListRepo indicatorStabilityListRepo;
  @Resource
  private IndicatorStabilitySearchRepo indicatorStabilitySearchRepo;
  @Resource
  private SettingTenantManager settingTenantManager;
  @Resource
  private CommonQuery commonQuery;

  /**
   * Finds a stability indicator by target ID and type with validation.
   * <p>
   * Retrieves a stability indicator for the specified target and validates its existence.
   * Enriches the indicator with target name information for complete context.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param targetId the target ID to find the indicator for
   * @param targetType the target type (e.g., PROJECT, PLAN, CASE)
   * @return IndicatorStability object with enriched target information
   * @throws ResourceNotFound if the indicator is not found
   */
  @Override
  public IndicatorStability find(Long targetId, CombinedTargetType targetType) {
    return new BizTemplate<IndicatorStability>() {

      @Override
      protected IndicatorStability process() {
        // Retrieve stability indicator from database
        IndicatorStability stabilizeDb = indicatorStabilityRepo
            .findByTargetIdAndTargetType(targetId, targetType);
            
        // Validate that the indicator exists
        assertResourceNotFound(nonNull(stabilizeDb), targetId, "IndicatorStability");
        
        // Enrich indicator with target name information
        assembleTargetName(stabilizeDb, targetType, targetId);
        return stabilizeDb;
      }
    }.execute();
  }

  /**
   * Retrieves stability indicator details or falls back to platform defaults.
   * <p>
   * First attempts to find user-configured indicators, then falls back to platform
   * default indicators if none are found. Handles tenant-specific configuration
   * and target information enrichment.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param targetType the target type for the indicator
   * @param targetId the target ID for the indicator
   * @return IndicatorStability object with either user or platform default configuration
   * @throws SysException if platform configuration retrieval fails
   */
  @Override
  public IndicatorStability detailOrDefault(CombinedTargetType targetType, Long targetId) {
    return new BizTemplate<IndicatorStability>() {

      @Override
      protected IndicatorStability process() {
        // First query whether there are stability indicators set by the user
        IndicatorStability stabilityDb = indicatorStabilityRepo.findByTargetIdAndTargetType(
            targetId, targetType);
        if (nonNull(stabilityDb)) {
          // Return user-configured indicator if found
          return stabilityDb;
        }

        // Query the default stability indicators of the platform
        try {
          // Retrieve tenant-specific platform settings
          SettingTenant setting = settingTenantManager.checkAndFindSetting(getOptTenantId());
          
          // Convert platform data to indicator format
          IndicatorStability stability = toIndicatorStability(setting.getStabilityData(),
              targetId, targetType);
              
          // Enrich indicator with target name information
          assembleTargetName(stability, targetType, targetId);
          return stability;
        } catch (Exception e) {
          // Handle platform configuration retrieval failures
          throw SysException.of(INDICATOR_GET_PLATFORM_FAIL_CODE, INDICATOR_GET_PLATFORM_FAIL);
        }
      }
    }.execute();
  }

  /**
   * Retrieves a paginated list of stability indicators with search capabilities.
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
   * @return Page of IndicatorStability objects with converted data
   */
  @Override
  public Page<IndicatorStability> list(GenericSpecification<IndicatorStability> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<IndicatorStability>>() {

      @Override
      protected Page<IndicatorStability> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriteria());
        
        // Execute search based on search type with appropriate converter
        return fullTextSearch
            ? indicatorStabilitySearchRepo.find(spec.getCriteria(), pageable,
            IndicatorStability.class, IndicatorStabilityConverter::objectArrToStability, match)
            : indicatorStabilityListRepo.find(spec.getCriteria(), pageable,
                IndicatorStability.class, IndicatorStabilityConverter::objectArrToStability, null);
      }
    }.execute();
  }

  /**
   * Finds a stability indicator by target ID and type without validation.
   * <p>
   * Retrieves a stability indicator without existence validation or target enrichment.
   * Used for internal operations where validation is handled separately.
   *
   * @param targetId the target ID to find the indicator for
   * @param targetType the target type for the indicator
   * @return IndicatorStability object or null if not found
   */
  @Override
  public IndicatorStability find0(Long targetId, CombinedTargetType targetType) {
    return indicatorStabilityRepo.findByTargetIdAndTargetType(targetId, targetType);
  }

  /**
   * Enriches indicator with target name information.
   * <p>
   * Retrieves and associates target name with the indicator for complete data context.
   * Uses common query service for target information retrieval.
   *
   * @param stability the indicator to enrich with target name
   * @param targetType the target type for name retrieval
   * @param targetId the target ID for name retrieval
   */
  private void assembleTargetName(IndicatorStability stability,
      CombinedTargetType targetType, Long targetId) {
    // Retrieve combined target information including name
    CombinedTarget combinedTarget = commonQuery.getCombinedTarget(targetType, targetId, true);
    
    // Set target name on the indicator
    stability.setTargetName(combinedTarget.getTargetName());
  }

}




