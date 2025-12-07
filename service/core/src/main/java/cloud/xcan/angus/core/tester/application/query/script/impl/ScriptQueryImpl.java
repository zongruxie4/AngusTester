package cloud.xcan.angus.core.tester.application.query.script.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirstValue;
import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findInfoScope;
import static cloud.xcan.angus.core.tester.application.converter.ScriptConverter.countCreationScript;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCRIPT_PROPERTIES_CONSTRAINT_ERROR;
import static cloud.xcan.angus.core.utils.CoreUtils.getCommonResourcesStatsFilter;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.spec.experimental.BizConstant.ANGUS_SCRIPT_LENGTH;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptAuthQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfoListRepo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfoRepo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfoSearchRepo;
import cloud.xcan.angus.core.tester.domain.script.ScriptRepo;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptCount;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptResourcesCreationCount;
import cloud.xcan.angus.core.tester.domain.script.tag.ScriptTag;
import cloud.xcan.angus.core.tester.domain.script.tag.ScriptTagRepo;
import cloud.xcan.angus.core.utils.PrincipalContextUtils;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.parser.AngusParser;
import cloud.xcan.angus.parser.models.AngusParseResult;
import cloud.xcan.angus.parser.models.ParseOptions;
import cloud.xcan.angus.remote.InfoScope;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.locale.MessageHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of ScriptQuery for script management and query operations.
 * </p>
 * <p>
 * Provides methods for script CRUD operations, parsing, validation, statistics, and comprehensive
 * data assembly.
 * </p>
 */
@Slf4j
@SummaryQueryRegister(name = "Script", table = "script", groupByColumns = {/*"created_date", */
    "type", "source"})
@Biz
public class ScriptQueryImpl implements ScriptQuery {

  @Resource
  private ScriptRepo scriptRepo;

  @Resource
  private ScriptInfoRepo scriptInfoRepo;

  @Resource
  private ScriptInfoListRepo scriptInfoListRepo;

  @Resource
  private ScriptInfoSearchRepo scriptInfoSearchRepo;

  @Resource
  private ScriptTagRepo scriptTagRepo;

  @Resource
  private ScriptAuthQuery scriptAuthQuery;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private UserManager userManager;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  @Resource
  private Validator validator;

  /**
   * <p>
   * Get detailed information of a script including tags, source name, and permissions.
   * </p>
   * <p>
   * Checks view permissions and sets comprehensive script information for user interaction.
   * </p>
   *
   * @param id Script ID
   * @return Script with complete information
   */
  @Override
  public Script detail(Long id) {
    return new BizTemplate<Script>() {
      @Override
      protected void checkParams() {
        scriptAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected Script process() {
        Script script = checkAndFind(id);

        setScriptTag(script);

        setScriptSourceName(script);

        // Note: For ctrl check permission
        script.setPermissions(scriptAuthQuery.getUserAuth(id, getUserId()));
        return script;
      }
    }.execute();
  }

  /**
   * <p>
   * Find a script by ID with optional permission checking for user actions.
   * </p>
   *
   * @param id Script ID
   * @return Script entity
   */
  @Override
  public Script findById(Long id) {
    return new BizTemplate<Script>() {
      @Override
      protected void checkParams() {
        if (isUserAction()) {
          scriptAuthQuery.checkViewAuth(getUserId(), id);
        }
      }

      @Override
      protected Script process() {
        return checkAndFind(id);
      }
    }.execute();
  }

  /**
   * <p>
   * Get script information for a set of IDs with source names.
   * </p>
   *
   * @param ids Set of script IDs
   * @return List of script information
   */
  @Override
  public List<ScriptInfo> infos(Set<Long> ids) {
    return new BizTemplate<List<ScriptInfo>>() {

      @Override
      protected List<ScriptInfo> process() {
        List<ScriptInfo> scripts = scriptInfoRepo.findAllById(ids);
        setScriptSourceName(scripts);
        return scripts;
      }
    }.execute();
  }

  /**
   * <p>
   * Get detailed script information including parsed Angus script content.
   * </p>
   * <p>
   * Checks view permissions and parses the script content into AngusScript format.
   * </p>
   *
   * @param id Script ID
   * @return Script with parsed content
   */
  @Override
  public Script angusDetail(Long id) {
    return new BizTemplate<Script>() {
      @Override
      protected void checkParams() {
        scriptAuthQuery.checkViewAuth(getUserId(), id);
      }

      @Override
      protected Script process() {
        Script script = checkAndFind(id);

        setScriptSourceName(script);

        if (isNotEmpty(script.getContent())) {
          script.setAngusScript(checkAndParse(script.getContent(), false));
        }
        return script;
      }
    }.execute();
  }

  /**
   * <p>
   * Get script count statistics with authorization filtering.
   * </p>
   *
   * @param criteria Search criteria
   * @return Script count statistics
   */
  @Override
  public ScriptCount countStatistics(Set<SearchCriteria> criteria) {
    return new BizTemplate<ScriptCount>() {
      @Override
      protected void checkParams() {
        // NOOP: Check view permission
      }

      @Override
      protected ScriptCount process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);

        return scriptInfoListRepo.count(criteria);
      }
    }.execute();
  }

  /**
   * <p>
   * Get script creation statistics for a project within a specified time range.
   * </p>
   * <p>
   * Supports filtering by creator object type and provides comprehensive creation count
   * statistics.
   * </p>
   *
   * @param projectId         Project ID
   * @param creatorObjectType Type of creator object
   * @param creatorObjectId   Creator object ID
   * @param createdDateStart  Start date for filtering
   * @param createdDateEnd    End date for filtering
   * @return Script creation statistics
   */
  @Override
  public ScriptResourcesCreationCount creationStatistics(Long projectId,
      AuthObjectType creatorObjectType, Long creatorObjectId, LocalDateTime createdDateStart,
      LocalDateTime createdDateEnd) {
    return new BizTemplate<ScriptResourcesCreationCount>() {

      @Override
      protected ScriptResourcesCreationCount process() {
        final ScriptResourcesCreationCount result = new ScriptResourcesCreationCount();

        // Find all when condition is null, else find by condition
        Set<Long> createdBys = isNull(creatorObjectType) ? null
            : userManager.getUserIdByOrgType0(creatorObjectType, creatorObjectId);

        Set<SearchCriteria> allFilters = getCommonResourcesStatsFilter(
            projectId, createdDateStart, createdDateEnd, createdBys);

        // Number of statistical scenario
        List<ScriptInfo> scripts = scriptInfoRepo.findAllByFilters(allFilters);
        countCreationScript(result, scripts);
        return result;
      }
    }.execute();
  }

  /**
   * <p>
   * List scripts with optional full-text search and comprehensive data assembly.
   * </p>
   * <p>
   * Checks project member permissions, applies authorization filters, and assembles tags and source
   * names based on the requested information scope.
   * </p>
   *
   * @param spec           Script search specification
   * @param pageable       Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match          Full-text search keywords
   * @return Page of scripts
   */
  @Override
  public Page<ScriptInfo> list(GenericSpecification<ScriptInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ScriptInfo>>() {
      @Override
      protected void checkParams() {
        if (!PrincipalContextUtils.isJobOrInnerApi()) {
          // Check the project member permission
          projectMemberQuery.checkMember(spec.getCriteria());
        }
      }

      @Override
      protected Page<ScriptInfo> process() {
        safeScenarioQuery(spec.getCriteria());

        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriteria());
        Page<ScriptInfo> pages = fullTextSearch
            ? scriptInfoSearchRepo.find(spec.getCriteria(), pageable, ScriptInfo.class, match)
            : scriptInfoListRepo.find(spec.getCriteria(), pageable, ScriptInfo.class, null);
        if (pages.isEmpty()) {
          return pages;
        }

        // For AngusCtrl remote query
        InfoScope infoScope = findInfoScope(spec.getCriteria(), InfoScope.DETAIL);
        if (InfoScope.DETAIL.equals(infoScope)) {
          setScriptInfoTag(pages.getContent());
          setScriptSourceName(pages.getContent());
          //setScriptCurrentPermission(pages.getContent());
        }
        return pages;
      }
    }.execute();
  }

  /**
   * <p>
   * List script information with conditional authorization filtering.
   * </p>
   * <p>
   * Applies authorization filters only for user actions and sets source names accordingly.
   * </p>
   *
   * @param spec           Script search specification
   * @param pageable       Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match          Full-text search keywords
   * @return Page of script information
   */
  @Override
  public Page<ScriptInfo> infoList(GenericSpecification<ScriptInfo> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<ScriptInfo>>() {
      @Override
      protected void checkParams() {
        if (!PrincipalContextUtils.isJobOrInnerApi()) {
          // Check the project member permission
          projectMemberQuery.checkMember(spec.getCriteria());
        }
      }

      @Override
      protected Page<ScriptInfo> process() {
        safeScenarioQuery(spec.getCriteria());

        if (isUserAction()) {
          // Set authorization conditions when you are not an administrator or only query yourself
          commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriteria());

          Page<ScriptInfo> pages = scriptInfoListRepo.find(spec.getCriteria(), pageable,
              ScriptInfo.class, null);
          if (pages.isEmpty()) {
            return pages;
          }
          setScriptSourceName(pages.getContent());
        }
        return scriptInfoRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * <p>
   * Safely convert scenario queries from associated table to main table queries.
   * </p>
   * <p>
   * Converts sourceTargetId to scenarioId when the source is SCENARIO.
   * </p>
   *
   * @param criteria Search criteria to modify
   */
  @Override
  public void safeScenarioQuery(Set<SearchCriteria> criteria) {
    // Convert the scenario query from associated table to the main table query
    String source = findFirstValue(criteria, "source");
    String sourceTargetId = findFirstValue(criteria, "sourceTargetId");
    if (isNotEmpty(source) && isNotEmpty(sourceTargetId) &&
        CombinedTargetType.SCENARIO.getValue().equals(source)) {
      CriteriaUtils.findAndRemove(criteria, "sourceTargetId");
      criteria.add(SearchCriteria.equal("scenarioId", sourceTargetId));
    }
  }

  /**
   * <p>
   * Find script information by source and source target ID.
   * </p>
   *
   * @param source         Script source
   * @param sourceTargetId Source target ID
   * @return List of script information
   */
  @Override
  public List<ScriptInfo> findInfoBySource(ScriptSource source, Long sourceTargetId) {
    return scriptInfoRepo.findBySourceIdAndSource(sourceTargetId, source);
  }

  /**
   * <p>
   * Find scripts by source and source target ID.
   * </p>
   *
   * @param source         Script source
   * @param sourceTargetId Source target ID
   * @return List of scripts
   */
  @Override
  public List<Script> findBySource(ScriptSource source, Long sourceTargetId) {
    return scriptRepo.findBySourceIdAndSource(sourceTargetId, source);
  }

  /**
   * <p>
   * Find script IDs by source and source target IDs.
   * </p>
   *
   * @param source          Script source
   * @param sourceTargetIds Collection of source target IDs
   * @return Set of script IDs
   */
  @Override
  public Set<Long> findIdsBySource(ScriptSource source, Collection<Long> sourceTargetIds) {
    return scriptRepo.findIdsBySourceIdInAndSourceAndType(sourceTargetIds, source.getValue());
  }

  /**
   * <p>
   * Find script IDs by source, source target IDs, and test types.
   * </p>
   *
   * @param source          Script source
   * @param sourceTargetIds Collection of source target IDs
   * @param testTypes       Collection of test types
   * @return Set of script IDs
   */
  @Override
  public Set<Long> findIdsBySourceAndTypeIn(ScriptSource source, Collection<Long> sourceTargetIds,
      Collection<ScriptType> testTypes) {
    return scriptRepo.findIdsBySourceIdInAndSourceAndType(sourceTargetIds, source.getValue(),
        testTypes.stream().map(ScriptType::getValue).toList());
  }

  /**
   * <p>
   * Find script by scenario ID, ensuring one-to-one relationship.
   * </p>
   *
   * @param scenarioId Scenario ID
   * @return Script entity or null
   */
  @Override
  public Script findScriptByScenarioId(Long scenarioId) {
    List<Script> scripts = scriptRepo.findBySourceIdAndSource(scenarioId, ScriptSource.SCENARIO);
    if (scripts.size() > 1) {
      log.error("Scenario[{}] and script are not one-on-one", scenarioId);
    }
    return isEmpty(scripts) ? null : scripts.get(0);
  }

  /**
   * <p>
   * Find script information by scenario ID, ensuring one-to-one relationship.
   * </p>
   *
   * @param scenarioId Scenario ID
   * @return Script information entity or null
   */
  @Override
  public ScriptInfo findScriptInfoByScenarioId(Long scenarioId) {
    List<ScriptInfo> scripts = scriptInfoRepo.findBySourceIdAndSource(scenarioId,
        ScriptSource.SCENARIO);
    if (scripts.size() > 1) {
      log.error("Scenario[{}] and script are not one-on-one", scenarioId);
    }
    return isEmpty(scripts) ? null : scripts.get(0);
  }

  /**
   * <p>
   * Find script by source, source target ID, and script type.
   * </p>
   *
   * @param source         Script source
   * @param sourceTargetId Source target ID
   * @param scriptType     Script type
   * @return Script entity
   */
  @Override
  public Script findBySourceAndScriptType(ScriptSource source, Long sourceTargetId,
      ScriptType scriptType) {
    return scriptRepo.findBySourceIdAndSourceAndType(sourceTargetId, source, scriptType);
  }

  /**
   * <p>
   * Check and find a script by ID.
   * </p>
   *
   * @param id Script ID
   * @return Script entity
   */
  @Override
  public Script checkAndFind(Long id) {
    return scriptRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Script"));
  }

  /**
   * <p>
   * Check and find script information by ID.
   * </p>
   *
   * @param id Script ID
   * @return Script information entity
   */
  @Override
  public ScriptInfo checkAndFindInfo(Long id) {
    return scriptInfoRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Script"));
  }

  /**
   * <p>
   * Check and find multiple script information by IDs.
   * </p>
   * <p>
   * Validates that all requested scripts exist and throws appropriate exceptions if any are
   * missing.
   * </p>
   *
   * @param ids Collection of script IDs
   * @return List of script information
   */
  @Override
  public List<ScriptInfo> checkAndFindInfos(Collection<Long> ids) {
    List<ScriptInfo> scriptDbs = scriptInfoRepo.findAllById(ids);
    Set<Long> scriptDbIds = scriptDbs.stream().map(ScriptInfo::getId).collect(Collectors.toSet());
    Set<Long> retainsIds = new HashSet<>(ids);
    retainsIds.removeAll(scriptDbIds);
    assertResourceNotFound(isEmpty(retainsIds), retainsIds, "Script");
    return scriptDbs;
  }

  @Override
  public List<Script> findByProjectId(Long projectId) {
    return scriptRepo.findByProjectId(projectId);
  }

  @Override
  public List<Script> findByProjectIdAndIdNot(Long projectId, List<Long> scriptIds) {
    return scriptRepo.findByProjectIdAndIdNotIn(projectId, scriptIds);
  }

  @Override
  public List<Script> findAllById(Collection<Long> ids) {
    return scriptRepo.findAllById(ids);
  }

  /**
   * <p>
   * Get a map of script information by script IDs.
   * </p>
   * <p>
   * Handles exceptions gracefully when scripts are deleted and returns partial results.
   * </p>
   *
   * @param scriptIds Set of script IDs
   * @return Map of script ID to script information
   */
  @Override
  public Map<Long, ScriptInfo> getScriptInfoMap(Set<Long> scriptIds) {
    Map<Long, ScriptInfo> scriptVoMap = new HashMap<>();
    if (isEmpty(scriptIds)) {
      return scriptVoMap;
    }
    try {
      scriptVoMap = infos(new HashSet<>(scriptIds))
          .stream().collect(Collectors.toMap(ScriptInfo::getId, x -> x));
    } catch (Exception e) {
      log.warn("Join script infos failed.", e);
      // 404, script deleted
    }
    return scriptVoMap;
  }

  /**
   * <p>
   * Check and parse script content into AngusScript format.
   * </p>
   * <p>
   * Validates parsing results and optionally performs additional script validation.
   * </p>
   *
   * @param content    Script content
   * @param validation Whether to perform additional validation
   * @return Parsed AngusScript or null
   */
  @Override
  public AngusScript checkAndParse(String content, boolean validation) {
    if (isEmpty(content)) {
      return null;
    }

    AngusParseResult parse = AngusParser.getInstance().readContents(content,
        ParseOptions.newBuilder().validate(false).build() // Close default validation
    );

    // Check the parsing validation passed
    ProtocolAssert.assertTrue(nonNull(parse.getScript()), isNotEmpty(parse.getMessages())
        ? parse.getMessages().get(0) : null);

    // Check the script content is valid
    if (validation) {
      checkAngusScript(parse.getScript());
    }
    return parse.getScript();
  }

  /**
   * <p>
   * Check and serialize AngusScript to string format.
   * </p>
   * <p>
   * Validates script content and checks length constraints.
   * </p>
   *
   * @param script     AngusScript to serialize
   * @param validation Whether to perform validation
   * @return Serialized script content
   */
  @Override
  public String checkAndSerialize(AngusScript script, boolean validation) {
    if (isEmpty(script)) {
      return null;
    }

    if (validation) {
      checkAngusScript(script);
    }

    String content = null;
    try {
      content = AngusParser.YAML_MAPPER.writeValueAsString(script);
    } catch (JsonProcessingException e) {
      ProtocolAssert.assertTrue(false, "Script format exception: " + e.getMessage());
    }

    checkScriptLength(content);
    return content;
  }

  /**
   * <p>
   * Validate AngusScript using Bean Validation constraints.
   * </p>
   *
   * @param script AngusScript to validate
   */
  @Override
  public void checkAngusScript(AngusScript script) {
    // Verify parameter constraints
    Set<ConstraintViolation<AngusScript>> constraints = validator.validate(script);
    if (!constraints.isEmpty()) {
      ConstraintViolation<AngusScript> constraint = constraints.iterator().next();
      ProtocolAssert.assertTrue(false,
          MessageHolder.message(SCRIPT_PROPERTIES_CONSTRAINT_ERROR) +
              ": [" + constraint.getPropertyPath() + "] " + constraint.getMessage());
    }
  }

  /**
   * <p>
   * Check if the script quota is exceeded after increment.
   * </p>
   *
   * @param incr Number of scripts to add
   */
  @Override
  public void checkQuota(int incr) {
    long count = scriptRepo.count();
    settingTenantQuotaManager.checkTenantQuota(QuotaResource.AngusTesterScript, null, incr + count);
  }

  /**
   * <p>
   * Check if script content length exceeds the maximum allowed length.
   * </p>
   *
   * @param script Script content
   */
  @Override
  public void checkScriptLength(String script) {
    if (isNotEmpty(script)) {
      // @Length(max = ANGUS_SCRIPT_LENGTH) // 10MB
      ProtocolAssert.assertTrue(script.length() <= ANGUS_SCRIPT_LENGTH,
          "The length of the script content cannot exceed: " + ANGUS_SCRIPT_LENGTH);
    }
  }

  /**
   * <p>
   * Check if the source resource exists for a script.
   * </p>
   * <p>
   * Validates that APIs or scenarios referenced by the script actually exist.
   * </p>
   *
   * @param script Script to validate
   */
  @Override
  public void checkSourceResourceExist(Script script) {
    //Assert.assertNotNull(script.getSourceId(), "Script source id is null");
    if (script.getSource().isUnique() && nonNull(script.getSourceId())) {
      if (script.getSource().isApis()) {
        apisQuery.check(script.getSourceId());
      }
      if (script.getSource().isScenario()) {
        scenarioQuery.check(script.getSourceId());
      }
    }
  }

  /**
   * <p>
   * Check if a script already exists when adding a new script.
   * </p>
   *
   * @param script Script to check
   */
  @Override
  public void checkSourceAddScriptExist(Script script) {
    assertNotNull(script.getType(), "Script type is null");
    assertNotNull(script.getSource(), "Script source is null");
    if (script.getSource().isUnique()) {
      assertNotNull(script.getSourceId(), "Script source id is null");
      if (scriptRepo.existsBySourceIdAndSourceAndType(script.getSourceId(), script.getSource(),
          script.getType())) {
        throw ResourceExisted.of(script.getType().getMessage(), script.getSource().getMessage());
      }
    }
  }

  /**
   * <p>
   * Check if a script already exists when updating a script, excluding the current script.
   * </p>
   *
   * @param script Script to check
   */
  @Override
  public void checkSourceUpdateScriptExist(Script script) {
    assertNotNull(script.getId(), "Script id is null");
    assertNotNull(script.getType(), "Script type id is null");
    if (script.getSource().isUnique()) {
      assertNotNull(script.getSourceId(), "Script source id is null");
      if (scriptRepo.existsBySourceIdAndSourceAndTypeAndIdNot(script.getSourceId(),
          script.getSource(), script.getType(), script.getId())) {
        throw ResourceExisted.of(script.getType().getMessage(), script.getSource().getMessage());
      }
    }
  }

  /**
   * <p>
   * Check if authorization control is enabled for a script.
   * </p>
   *
   * @param scriptId Script ID
   * @return true if authorization control is enabled, false otherwise
   */
  @Override
  public boolean isAuthCtrl(Long scriptId) {
    ScriptInfo script = scriptInfoRepo.findById(scriptId).orElse(null);
    return nonNull(script) && (script.getAuth());
  }

  /**
   * <p>
   * Set tags for a script.
   * </p>
   *
   * @param script Script to set tags for
   */
  @Override
  public void setScriptTag(Script script) {
    List<String> tags = scriptTagRepo.findByScriptId(script.getId()).stream()
        .map(ScriptTag::getName).toList();
    script.setTags(tags);
  }

  /**
   * <p>
   * Set tags for a list of script information.
   * </p>
   * <p>
   * Batch retrieves tag information to avoid N+1 query problems.
   * </p>
   *
   * @param scriptInfos List of script information to update
   */
  @Override
  public void setScriptInfoTag(List<ScriptInfo> scriptInfos) {
    Map<Long, List<ScriptTag>> tagsMap = scriptTagRepo
        .findByScriptIdIn(scriptInfos.stream().map(ScriptInfo::getId)
            .collect(Collectors.toSet())).stream()
        .collect(Collectors.groupingBy(ScriptTag::getScriptId));
    if (isNotEmpty(tagsMap)) {
      for (ScriptInfo scriptInfo : scriptInfos) {
        if (tagsMap.containsKey(scriptInfo.getId())) {
          scriptInfo.setTags(tagsMap.get(scriptInfo.getId()).stream()
              .map(ScriptTag::getName).toList());
        }
      }
    }
  }

  /**
   * <p>
   * Set the source name for a script.
   * </p>
   *
   * @param script Script to set source name for
   */
  @Override
  public void setScriptSourceName(Script script) {
    if (script.getSource().isApis() && nonNull(script.getSourceId())) {
      ApisBaseInfo apis = apisQuery.findBase0ById(script.getSourceId());
      if (nonNull(apis)) {
        script.setSourceName(apis.getSummary());
      }
    }

    if (script.getSource().isScenario() && nonNull(script.getSourceId())) {
      Scenario scenario = scenarioQuery.find0(script.getSourceId());
      if (nonNull(scenario)) {
        script.setSourceName(scenario.getName());
      }
    }
  }

  /**
   * <p>
   * Set the source name for a list of script information.
   * </p>
   *
   * @param scriptInfos List of script information to set source name for
   */
  @Override
  public void setScriptSourceName(List<ScriptInfo> scriptInfos) {
    Map<ScriptSource, List<ScriptInfo>> sourceScriptMap = scriptInfos.stream()
        .filter(x -> x.getSource().isUnique())
        .collect(Collectors.groupingBy(ScriptInfo::getSource));
    if (isEmpty(sourceScriptMap)) {
      return;
    }

    if (sourceScriptMap.containsKey(ScriptSource.API)) {
      Set<Long> scriptSourceIdsMap = sourceScriptMap.get(ScriptSource.API).stream()
          .map(ScriptInfo::getSourceId).filter(Objects::nonNull).collect(Collectors.toSet());
      Map<Long, ApisBaseInfo> apisMap = apisQuery.findBase0ByIdIn(scriptSourceIdsMap)
          .stream().collect(Collectors.toMap(ApisBaseInfo::getId, x -> x));
      for (ScriptInfo script : scriptInfos) {
        if (nonNull(script.getSourceId()) && apisMap.containsKey(script.getSourceId())) {
          script.setSourceName(apisMap.get(script.getSourceId()).getSummary());
        }
      }
    }

    if (sourceScriptMap.containsKey(ScriptSource.SCENARIO)) {
      Set<Long> scriptSourceIdsMap = sourceScriptMap.get(ScriptSource.SCENARIO).stream()
          .map(ScriptInfo::getSourceId).filter(Objects::nonNull).collect(Collectors.toSet());
      Map<Long, Scenario> apisMap = scenarioQuery.find0ByIdIn(scriptSourceIdsMap)
          .stream().collect(Collectors.toMap(Scenario::getId, x -> x));
      for (ScriptInfo script : scriptInfos) {
        if (nonNull(script.getSourceId()) && apisMap.containsKey(script.getSourceId())) {
          script.setSourceName(apisMap.get(script.getSourceId()).getName());
        }
      }
    }
  }

}
