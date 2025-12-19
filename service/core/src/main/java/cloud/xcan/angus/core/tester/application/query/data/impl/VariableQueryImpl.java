package cloud.xcan.angus.core.tester.application.query.data.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.VariableConverter.toAngusVariable;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.VARIABLE_NAME_REPEATED_T;
import static cloud.xcan.angus.core.utils.AngusUtils.assembleVariableHttpExtraction;
import static cloud.xcan.angus.core.utils.AngusUtils.toServer;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableTargetQuery;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableRepo;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableSearchRepo;
import cloud.xcan.angus.extraction.VariableExtractor;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import cloud.xcan.jmock.core.parser.replacer.DefaultMockTextReplacer;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Implementation of variable query operations for data management and validation.
 *
 * <p>This class provides comprehensive functionality for querying, validating,
 * and managing variables including preview generation, quota checking, and name validation.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Variable detail retrieval and validation</li>
 *   <li>Value preview generation with extraction</li>
 *   <li>Variable listing with search and pagination</li>
 *   <li>Name existence validation and safe cloning</li>
 *   <li>Tenant quota management and validation</li>
 *   <li>Required parameter validation</li>
 *   <li>Server discovery by service ID</li>
 * </ul></p>
 *
 * @author XiaoLong Liu
 */
@Service
public class VariableQueryImpl implements VariableQuery {

  @Resource
  private VariableRepo variablesRepo;
  @Resource
  private VariableSearchRepo variableSearchRepo;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private VariableTargetQuery variableTargetQuery;
  @Resource
  private VariableExtractor defaultVariableExtractor;
  @Resource
  private DefaultMockTextReplacer defaultMockTextReplacer;

  /**
   * Retrieves detailed variable information by ID.
   *
   * <p>This method fetches a variable by its ID, performing validation
   * to ensure the variable exists.</p>
   *
   * @param id the variable ID to retrieve
   * @return the detailed variable information
   */
  @Override
  public Variable detail(Long id) {
    return new BizTemplate<Variable>() {

      @Override
      protected Variable process() {
        return checkAndFind(id);
      }
    }.execute();
  }

  /**
   * Generates value preview for a variable.
   *
   * <p>This method extracts the actual value from a variable using either
   * static value processing or dynamic extraction methods.</p>
   *
   * @param id         the variable ID (optional if name and value are provided)
   * @param name       the variable name (used if ID is not provided)
   * @param value      the variable value (used if ID is not provided)
   * @param extraction the extraction method configuration (used if ID is not provided)
   * @return the preview value of the variable
   */
  @Override
  public String valuePreview(Long id, String name, String value, DefaultExtraction extraction) {
    return new BizTemplate<String>() {
      Variable variableDb;

      @Override
      protected void checkParams() {
        if (nonNull(id)) {
          variableDb = checkAndFind(id);
        }
      }

      @Override
      protected String process() {
        // Return static value if variable exists and has a value
        if (nonNull(variableDb) && isNotEmpty(variableDb.getValue())) {
          return getStaticVariableValue(variableDb);
        }

        // Convert to Angus variable model and extract dynamic value
        cloud.xcan.angus.model.element.variable.Variable variable = nonNull(variableDb)
            ? toAngusVariable(variableDb) : toAngusVariable(name, value, extraction);
        assembleExtractVariable(variable);
        return variable.getActualValue();
      }
    }.execute();
  }

  /**
   * Generates value preview for multiple variables.
   *
   * <p>This method extracts values from multiple variables, handling both
   * static values and dynamic extraction for each variable.</p>
   *
   * @param ids list of variable IDs to preview
   * @return map of variable names to their preview values
   */
  @Override
  public Map<String, String> valuePreview(List<Long> ids) {
    return new BizTemplate<Map<String, String>>() {
      List<Variable> variablesDb;

      @Override
      protected void checkParams() {
        variablesDb = checkAndFind(ids);
      }

      @Override
      protected Map<String, String> process() {
        Map<String, String> valueMap = new LinkedHashMap<>();
        for (Variable variableDb : variablesDb) {
          // Handle static values first
          if (isNotEmpty(variableDb.getValue())) {
            valueMap.put(variableDb.getName(), getStaticVariableValue(variableDb));
            continue;
          }

          // Extract dynamic values for variables without static values
          cloud.xcan.angus.model.element.variable.Variable variable = toAngusVariable(variableDb);
          assembleExtractVariable(variable);
          valueMap.put(variableDb.getName(), variable.getActualValue());
        }
        return valueMap;
      }
    }.execute();
  }

  /**
   * Lists variables with optional search and pagination.
   *
   * <p>This method retrieves variables based on specification criteria,
   * supporting both full-text search and standard filtering.</p>
   *
   * @param spec           the specification for filtering variables
   * @param pageable       the pagination parameters
   * @param fullTextSearch whether to use full-text search
   * @param match          the match fields for full-text search
   * @return paginated list of variables
   */
  @Override
  public Page<Variable> list(GenericSpecification<Variable> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<Variable>>() {

      @Override
      protected Page<Variable> process() {
        return fullTextSearch
            ? variableSearchRepo.find(spec.getCriteria(), pageable, Variable.class, match)
            : variablesRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * Finds servers by service ID through variable associations.
   *
   * <p>This method discovers servers by finding variables associated with
   * a service and extracting server information from HTTP extraction configurations.</p>
   *
   * @param serviceId the service ID to find servers for
   * @return list of servers associated with the service
   */
  @Override
  public List<Server> findServerByServiceId(Long serviceId) {
    return new BizTemplate<List<Server>>() {

      @Override
      protected List<Server> process() {
        // Find variable IDs associated with the service
        Set<Long> variableIds = variableTargetQuery.findVariableIdByServiceId(serviceId);
        if (isEmpty(variableIds)) {
          return emptyList();
        }

        // Retrieve extracted variables
        List<Variable> variables = variablesRepo.findByIdAndExtracted(variableIds, true);
        if (isEmpty(variables)) {
          return emptyList();
        }

        // Extract server information from HTTP extraction configurations
        return variables.stream().filter(x -> x.getExtraction() instanceof HttpExtraction)
            .map(x -> (HttpExtraction) x.getExtraction())
            .filter(x -> nonNull(x.getRequest().getServer())
                && nonNull(x.getRequest().getServer().getUrl()))
            .map(x -> toServer(x.getRequest().getServer()))
            .filter(ObjectUtils.distinctByKey(Server::getUrl))
            .toList();
      }
    }.execute();
  }

  /**
   * Finds variables by project ID and optional variable IDs.
   *
   * <p>This method retrieves variables for a specific project, optionally
   * filtered by a set of variable IDs.</p>
   *
   * @param projectId the project ID
   * @param ids       optional set of variable IDs to filter by
   * @return list of variables matching the criteria
   */
  @Override
  public List<Variable> findByProjectAndIds(Long projectId, @Nullable LinkedHashSet<Long> ids) {
    return new BizTemplate<List<Variable>>() {

      @Override
      protected List<Variable> process() {
        return isEmpty(ids) ? variablesRepo.findByProjectId(projectId)
            : variablesRepo.findByProjectIdAndIdIn(projectId, ids);
      }
    }.execute();
  }

  /**
   * Validates and retrieves a variable by ID.
   *
   * <p>This method fetches a variable by its ID, throwing a ResourceNotFound
   * exception if the variable does not exist.</p>
   *
   * @param id the variable ID to check and retrieve
   * @return the variable if found
   * @throws ResourceNotFound if the variable is not found
   */
  @Override
  public Variable checkAndFind(Long id) {
    return variablesRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Variable"));
  }

  /**
   * Validates and retrieves multiple variables by IDs.
   *
   * <p>This method fetches multiple variables by their IDs, ensuring all
   * requested variables exist and are valid.</p>
   *
   * @param ids collection of variable IDs to check and retrieve
   * @return list of variables if all found
   * @throws ResourceNotFound if any variable is not found
   */
  @Override
  public List<Variable> checkAndFind(Collection<Long> ids) {
    List<Variable> variables = variablesRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(variables), ids.iterator().next(), "Variable");
    if (ids.size() != variables.size()) {
      for (Variable variable : variables) {
        assertResourceNotFound(ids.contains(variable.getId()), variable.getId(), "Variable");
      }
    }
    return variables;
  }

  /**
   * Validates and retrieves variables by project ID and names.
   *
   * <p>This method fetches variables by their names within a specific project,
   * ensuring all requested variables exist.</p>
   *
   * @param projectId the project ID
   * @param names     list of variable names to check and retrieve
   * @return list of variables if all found
   * @throws ResourceNotFound if any variable is not found
   */
  @Override
  public List<Variable> checkAndFindByName(Long projectId, List<String> names) {
    List<Variable> variables = variablesRepo.findByProjectIdAndNameIn(projectId, names);
    assertResourceNotFound(isNotEmpty(variables), names.iterator().next(), "Variable");
    if (names.size() != variables.size()) {
      for (Variable variable : variables) {
        assertResourceNotFound(names.contains(variable.getName()), variable.getName(), "Variable");
      }
    }
    return variables;
  }

  /**
   * Validates tenant quota for variable creation.
   *
   * <p>This method checks if the current tenant has sufficient quota
   * to create additional variables.</p>
   *
   * @param inc the increment amount to check
   * @throws QuotaException if the quota limit would be exceeded
   */
  @Override
  public void checkTenantQuota(int inc) {
    long count = variablesRepo.countByTenantId(getTenantId());
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterVariable,
        Collections.singleton(getTenantId()), count + inc);
  }

  /**
   * Validates required parameters for a variable.
   *
   * <p>This method checks if the variable has all required parameters
   * based on its extraction configuration.</p>
   *
   * @param variable the variable to validate
   * @throws ProtocolException if required parameters are missing
   */
  @Override
  public void checkRequiredParam(Variable variable) {
    if (variable.getExtracted() && isNull(variable.getExtraction())) {
      throw ProtocolException.of(PARAM_MISSING_T, new Object[]{"extraction"});
    }

    if (variable.getExtracted() && variable.getExtraction().getMethod().needExpression()
        && isEmpty(variable.getExtraction().getExpression())) {
      throw ProtocolException.of(PARAM_MISSING_T, new Object[]{"expression"});
    }
  }

  /**
   * Validates that a variable name does not already exist in the project.
   *
   * <p>This method checks if a variable with the same name already exists
   * in the specified project for add operations.</p>
   *
   * @param variable the variable to check
   * @throws ResourceExisted if a variable with the same name already exists
   */
  @Override
  public void checkAddNameExists(Variable variable) {
    if (variablesRepo.existsByProjectIdAndName(variable.getProjectId(), variable.getName())) {
      throw ResourceExisted.of(VARIABLE_NAME_REPEATED_T, new Object[]{variable.getName()});
    }
  }

  /**
   * Validates that a variable name does not already exist in the project for updates.
   *
   * <p>This method checks if a variable with the same name already exists
   * in the specified project, excluding the current variable for update operations.</p>
   *
   * @param variable the variable to check
   * @throws ResourceExisted if a variable with the same name already exists
   */
  @Override
  public void checkUpdateNameExists(Variable variable) {
    if (variablesRepo.existsByProjectIdAndNameAndIdNot(variable.getProjectId(),
        variable.getName(), variable.getId())) {
      throw ResourceExisted.of(VARIABLE_NAME_REPEATED_T, new Object[]{variable.getName()});
    }
  }

  /**
   * Sets a safe clone name for a variable.
   *
   * <p>This method generates a unique name for variable cloning by appending
   * "-Copy" and optionally a random suffix to ensure uniqueness.</p>
   *
   * @param variable the variable to set the clone name for
   */
  @Override
  public void setSafeCloneName(Variable variable) {
    String saltName = randomAlphanumeric(3);
    String clonedName = variablesRepo.existsByName(variable.getName() + "-Copy")
        ? variable.getName() + "-Copy." + saltName : variable.getName() + "-Copy";
    // Ensure name length does not exceed maximum limit
    clonedName = clonedName.length() > MAX_NAME_LENGTH ? clonedName.substring(0,
        MAX_NAME_LENGTH - 3) + saltName : clonedName;
    variable.setName(clonedName);
  }

  /**
   * Retrieves static variable value with mock processing.
   *
   * <p>This method processes static variable values, applying mock text
   * replacement if the variable has mock values configured.</p>
   *
   * @param variableDb the variable to get the static value for
   * @return the processed static value
   * @throws ProtocolException if mock processing fails
   */
  private String getStaticVariableValue(Variable variableDb) {
    if (variableDb.hasMockValue()) {
      try {
        return defaultMockTextReplacer.replace(variableDb.getValue());
      } catch (Exception e) {
        throw ProtocolException.of(e.getMessage());
      }
    }
    return variableDb.getValue();
  }

  /**
   * Assembles and extracts variable values using HTTP extraction.
   *
   * <p>This method configures HTTP extraction for variables and performs
   * the actual value extraction, handling any extraction failures.</p>
   *
   * @param angusVariable the Angus variable model to process
   * @throws ProtocolException if extraction fails or configuration is invalid
   */
  private void assembleExtractVariable(
      cloud.xcan.angus.model.element.variable.Variable angusVariable) {
    try {
      // Assemble HTTP extraction configuration
      assembleVariableHttpExtraction(List.of(angusVariable));
    } catch (Exception e) {
      throw ProtocolException.of(
          nonNull(e.getCause()) ? e.getCause().getMessage() : e.getMessage());
    }

    // Perform variable extraction
    defaultVariableExtractor.extract(List.of(angusVariable));

    // Check for extraction failures
    if (nonNull(angusVariable.getExtraction())
        && nonNull(angusVariable.getExtraction().getFailureMessage())) {
      throw ProtocolException.of(angusVariable.getExtraction().getFailureMessage());
    }
  }

}
