package cloud.xcan.sdf.core.angustester.application.cmd.data.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.VARIABLE;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.SAMPLE_VARIABLE_FILE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.VariableConverter.toVariable;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.VARIABLE_FILE_PARSING_ERROR_T;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.VARIABLE_IS_NOT_VALID;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.CLONE;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.IMPORT;
import static cloud.xcan.sdf.core.angustester.infra.util.AngusTesterUtils.parseSample;
import static cloud.xcan.sdf.core.angustester.infra.util.ServicesFileUtils.getImportTmpPath;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;
import static cloud.xcan.sdf.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.sdf.spec.utils.JsonUtils.isJson;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.parser.AngusParser;
import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.api.commonlink.user.User;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.api.message.CommSysException;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.data.VariableCmd;
import cloud.xcan.sdf.core.angustester.application.converter.VariableConverter;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableRepo;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableTargetRepo;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.spec.experimental.Assert;
import cloud.xcan.sdf.spec.experimental.IdKey;
import com.fasterxml.jackson.core.type.TypeReference;
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
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
  private ProjectQuery projectQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Resource
  private UserManager userManager;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Variable variable) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected void checkParams() {
        // Check the member permissions
        projectMemberQuery.checkMember(variable.getProjectId(), getUserId());
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
        activityCmd.batchAdd(toActivities(VARIABLE, clonedVariables, CLONE,
            variablesDb.stream().map(s -> new Object[]{s.getName()}).collect(Collectors.toList())));
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> imports(Long projectId,
      StrategyWhenDuplicated strategyWhenDuplicated, String content, MultipartFile file) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      @Override
      protected void checkParams() {
        // Check the member permissions
        projectMemberQuery.checkMember(projectId, getUserId());
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
            throw CommSysException.of("Exception reading import file, cause: "
                + e.getMessage(), ExceptionLevel.ERROR);
          }
        }

        List<Variable> variables = parseVariablesFromScript(projectId,
            strategyWhenDuplicated, finalContent);
        List<IdKey<Long, Object>> idKeys = batchInsert(variables, "name");

        // Save import variable activities
        activityCmd.batchAdd(toActivities(VARIABLE, variables, IMPORT,
            variables.stream().map(s -> new Object[]{s.getName()}).collect(Collectors.toList())));
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> importExample(Long projectId) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Check the project exists
        projectDb = projectQuery.checkAndFind(projectId);
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        URL resourceUrl = this.getClass().getResource("/samples/data/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_VARIABLE_FILE);
        String content = parseSample(Objects.requireNonNull(resourceUrl), SAMPLE_VARIABLE_FILE);
        List<Variable> variables = parseVariablesFromScript(projectId,
            StrategyWhenDuplicated.IGNORE, content);

        if (!isUserAction()){
          List<User> users = userManager.findByTenantId(getOptTenantId());
          Assert.assertNotEmpty(users, "Tenant users are empty");
          for (Variable variable : variables) {
            variable.setId(uidGenerator.getUID()).setTenantId(projectDb.getTenantId())
                .setCreatedBy(users.get(0).getId()).setLastModifiedBy(users.get(0).getId());
          }
        }

        return batchInsert(variables, "name");
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        List<Variable> variablesDb = variablesRepo.findAllById(ids);

        if (isEmpty(variablesDb)) {
          return null;
        }

        variablesRepo.deleteByIdIn(ids);
        variableTargetRepo.deleteByVariableIdIn(ids);

        activityCmd.batchAdd(toActivities(VARIABLE, variablesDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

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
      throw CommProtocolException.of(VARIABLE_FILE_PARSING_ERROR_T,
          new Object[]{e.getMessage()});
    }

    validVariables = parsedVariables.stream().filter(x -> isNotEmpty(x.getName()))
        .collect(Collectors.toList());
    assertNotEmpty(validVariables, VARIABLE_IS_NOT_VALID);

    Set<String> names = validVariables.stream()
        .map(cloud.xcan.angus.model.element.variable.Variable::getName)
        .collect(Collectors.toSet());
    Set<String> existedNames = variablesRepo.findNamesByProjectIdAndNameIn(projectId, names);
    if (strategyWhenDuplicated.isCover() && isNotEmpty(existedNames)) {
      variablesRepo.deleteByProjectIdAndNameIn(projectId, existedNames);
    } else {
      validVariables = validVariables.stream().filter(x -> !existedNames.contains(x.getName()))
          .collect(Collectors.toList());
    }

    assertNotEmpty(validVariables, VARIABLE_IS_NOT_VALID);

    // Save final valid variables
    return validVariables.stream().map(x -> toVariable(projectId, x)).collect(Collectors.toList());
  }

  @Override
  protected BaseRepository<Variable, Long> getRepository() {
    return this.variablesRepo;
  }
}
