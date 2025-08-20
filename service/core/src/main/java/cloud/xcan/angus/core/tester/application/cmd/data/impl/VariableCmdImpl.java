package cloud.xcan.angus.core.tester.application.cmd.data.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.VARIABLE;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_VARIABLE_FILE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.VariableConverter.toVariable;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.VARIABLE_FILE_PARSING_ERROR_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.VARIABLE_IS_NOT_VALID;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.CLONE;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.IMPORT;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.readSample;
import static cloud.xcan.angus.core.tester.infra.util.ServicesFileUtils.getImportTmpPath;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.JsonUtils.isJson;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.data.VariableCmd;
import cloud.xcan.angus.core.tester.application.converter.VariableConverter;
import cloud.xcan.angus.core.tester.application.query.data.VariableQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableRepo;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableTargetRepo;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.parser.AngusParser;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.spec.experimental.IdKey;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command implementation for managing variables.
 * <p>
 * Provides methods for adding, updating, replacing, cloning, importing, and deleting variables.
 * Handles permission checks, name uniqueness, quota validation, activity logging, and batch operations.
 */
@Slf4j
@Biz
public class VariableCmdImpl extends CommCmd<Variable, Long> implements VariableCmd {

  @Resource
  private VariableRepo variablesRepo;
  @Resource
  private VariableTargetRepo variableTargetRepo;
  @Resource
  private VariableQuery variableQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Add a new variable.
   * <p>
   * Validates project membership, required parameters, name uniqueness, and tenant quota.
   * Inserts the variable and logs the creation activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Variable variable) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected void checkParams() {
        // Check the member permissions
        projectMemberQuery.checkMember(getUserId(), variable.getProjectId());
        // Check the required parameters
        variableQuery.checkRequiredParam(variable);
        // Check the name is not repeated
        variableQuery.checkAddNameExists(variable);
        // Check the tenant variable quota
        variableQuery.checkTenantQuota(1);
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKeys = insert(variable, "name");

        activityCmd.add(toActivity(VARIABLE, variable, ActivityType.CREATED));
        return idKeys;
      }
    }.execute();
  }

  /**
   * Update an existing variable.
   * <p>
   * Validates variable existence, project ID, and name uniqueness. Updates the variable and logs the update activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Variable variable) {
    new BizTemplate<Void>() {
      Variable variableDb;

      @Override
      protected void checkParams() {
        // Check and find variable
        variableDb = variableQuery.checkAndFind(variable.getId());
        // Safe project id
        variable.setProjectId(variableDb.getProjectId());
        // Check the name is not repeated
        if (nonNull(variable.getName())) {
          variableQuery.checkUpdateNameExists(variable);
        }
      }

      @Override
      protected Void process() {
        variablesRepo.save(copyPropertiesIgnoreNull(variable, variableDb, "projectId"));

        activityCmd.add(toActivity(VARIABLE, variableDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Replace (add or update) a variable.
   * <p>
   * Adds a new variable if ID is null, otherwise updates the existing variable.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Variable variable) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected void checkParams() {
        if (nonNull(variable.getId())) {
          // Check the required parameters
          variableQuery.checkRequiredParam(variable);
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(variable.getId())) {
          return add(variable);
        }

        update(variable);
        return new IdKey<Long, Object>().setId(variable.getId());
      }
    }.execute();
  }

  /**
   * Clone a batch of variables.
   * <p>
   * Validates variable existence, clones variables with unique names, inserts them, and logs clone activities.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> clone(HashSet<Long> ids) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      List<Variable> variablesDb;

      @Override
      protected void checkParams() {
        // Check the variables exists
        variablesDb = variableQuery.checkAndFind(ids);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<Variable> clonedVariables = new ArrayList<>();
        for (Variable variableDb : variablesDb) {
          Variable clonedVariable = VariableConverter.toCloneVariable(variableDb);
          variableQuery.setSafeCloneName(clonedVariable);
          clonedVariables.add(clonedVariable);
        }

        List<IdKey<Long, Object>> idKeys = batchInsert(clonedVariables, "name");

        // Add clone activities
        activityCmd.addAll(toActivities(VARIABLE, clonedVariables, CLONE,
            variablesDb.stream().map(s -> new Object[]{s.getName()}).toList()));
        return idKeys;
      }
    }.execute();
  }

  /**
   * Import variables from content or file.
   * <p>
   * Validates project membership and file presence, parses content, inserts variables, and logs import activities.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> imports(Long projectId,
      StrategyWhenDuplicated strategyWhenDuplicated, String content, MultipartFile file) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // Check the member permissions
        projectMemberQuery.checkMember(getUserId(), projectId);
        // Check the upload content is required
        assertTrue(isNotEmpty(content) || nonNull(file), "Upload file is required");
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        String finalContent = content;

        // Read from upload file
        if (isEmpty(content)) {
          String srcFileName = file.getOriginalFilename();
          File tmpPath = getImportTmpPath(ApiImportSource.ANGUS, srcFileName);
          File importFile = new File(tmpPath.getPath() + File.separator + srcFileName);
          try {
            file.transferTo(importFile);
            finalContent = FileUtil.readAsString(importFile);
          } catch (IOException e) {
            log.error("Exception reading import file", e);
            throw SysException.of("Exception reading import file, cause: "
                + e.getMessage(), ExceptionLevel.ERROR);
          }
        }

        List<Variable> variables = parseVariablesFromScript(projectId,
            strategyWhenDuplicated, finalContent);
        List<IdKey<Long, Object>> idKeys = batchInsert(variables, "name");

        // Save import variable activities
        activityCmd.addAll(toActivities(VARIABLE, variables, IMPORT,
            variables.stream().map(s -> new Object[]{s.getName()}).toList()));
        return idKeys;
      }
    }.execute();
  }

  /**
   * Note: When API calls that are not user-action, tenant and user information must be injected
   * into the PrincipalContext.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {

      @Override
      protected List<IdKey<Long, Object>> process() {
        URL resourceUrl = this.getClass().getResource("/samples/data/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_VARIABLE_FILE);
        String content = readSample(Objects.requireNonNull(resourceUrl), SAMPLE_VARIABLE_FILE);
        List<Variable> variables = parseVariablesFromScript(projectId,
            StrategyWhenDuplicated.IGNORE, content);
        for (Variable variable : variables) {
          variable.setProjectId(projectId);
        }
        return batchInsert(variables, "name");
      }
    }.execute();
  }

  /**
   * Delete a batch of variables.
   * <p>
   * Deletes variables and their targets, and logs delete activities.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        List<Variable> variablesDb = variablesRepo.findAllById(ids);

        if (isEmpty(variablesDb)) {
          return null;
        }

        variablesRepo.deleteByIdIn(ids);
        variableTargetRepo.deleteByVariableIdIn(ids);

        activityCmd.addAll(toActivities(VARIABLE, variablesDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Parse variables from script content.
   * <p>
   * Parses JSON or YAML content, validates variable structure, handles duplicates according to strategy, and returns valid variables.
   */
  private @NotNull List<Variable> parseVariablesFromScript(Long projectId,
      StrategyWhenDuplicated strategyWhenDuplicated, String finalContent) {
    // Parse angus variables script
    List<cloud.xcan.angus.model.element.variable.Variable> validVariables;
    List<cloud.xcan.angus.model.element.variable.Variable> parsedVariables;
    try {
      parsedVariables = isJson(finalContent) ? AngusParser.JSON_MAPPER.readValue(finalContent,
          new TypeReference<List<cloud.xcan.angus.model.element.variable.Variable>>() {
          }) : AngusParser.YAML_MAPPER.readValue(finalContent,
          new TypeReference<List<cloud.xcan.angus.model.element.variable.Variable>>() {
          });
    } catch (Exception e) {
      throw ProtocolException.of(VARIABLE_FILE_PARSING_ERROR_T,
          new Object[]{e.getMessage()});
    }

    validVariables = parsedVariables.stream().filter(x -> isNotEmpty(x.getName()))
        .toList();
    assertNotEmpty(validVariables, VARIABLE_IS_NOT_VALID);

    Set<String> names = validVariables.stream()
        .map(cloud.xcan.angus.model.element.variable.Variable::getName)
        .collect(Collectors.toSet());
    Set<String> existedNames = variablesRepo.findNamesByProjectIdAndNameIn(projectId, names);
    if (strategyWhenDuplicated.isCover() && isNotEmpty(existedNames)) {
      variablesRepo.deleteByProjectIdAndNameIn(projectId, existedNames);
    } else {
      validVariables = validVariables.stream().filter(x -> !existedNames.contains(x.getName()))
          .toList();
    }

    assertNotEmpty(validVariables, VARIABLE_IS_NOT_VALID);

    // Save final valid variables
    return validVariables.stream().map(x -> toVariable(projectId, x)).toList();
  }

  /**
   * Get the repository for Variable entity.
   * <p>
   * @return the VariableRepo instance
   */
  @Override
  protected BaseRepository<Variable, Long> getRepository() {
    return this.variablesRepo;
  }
}
