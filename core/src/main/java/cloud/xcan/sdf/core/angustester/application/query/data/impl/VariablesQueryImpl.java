package cloud.xcan.sdf.core.angustester.application.query.data.impl;

import static cloud.xcan.angus.core.utils.AngusUtils.assembleVariableHttpExtraction;
import static cloud.xcan.angus.core.utils.AngusUtils.toServer;
import static cloud.xcan.sdf.api.message.CommProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.sdf.core.angustester.application.converter.VariableConverter.toAngusVariable;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.VARIABLE_NAME_REPEATED_T;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getTenantId;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.angus.extraction.VariableExtractor;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.comp.jmock.core.parser.replacer.DefaultMockTextReplacer;
import cloud.xcan.sdf.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableTargetQuery;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class VariablesQueryImpl implements VariableQuery {

  @Resource
  private VariableRepo variablesRepo;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private VariableTargetQuery variableTargetQuery;

  @Resource
  private VariableExtractor defaultVariableExtractor;

  @Resource
  private DefaultMockTextReplacer defaultMockTextReplacer;

  @Override
  public Variable detail(Long id) {
    return new BizTemplate<Variable>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Variable process() {
        return checkAndFind(id);
      }
    }.execute();
  }

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
        if (nonNull(variableDb) && isNotEmpty(variableDb.getValue())) {
          return getStaticVariableValue(variableDb);
        }

        cloud.xcan.angus.model.element.variable.Variable variable = nonNull(variableDb)
            ? toAngusVariable(variableDb) : toAngusVariable(name, value, extraction);
        assembleExtractVariable(variable);
        return variable.getActualValue();
      }
    }.execute();
  }

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
          if (isNotEmpty(variableDb.getValue())) {
            valueMap.put(variableDb.getName(), getStaticVariableValue(variableDb));
            continue;
          }

          cloud.xcan.angus.model.element.variable.Variable variable = toAngusVariable(variableDb);
          assembleExtractVariable(variable);
          valueMap.put(variableDb.getName(), variable.getActualValue());
        }
        return valueMap;
      }
    }.execute();
  }

  @Override
  public Page<Variable> find(GenericSpecification<Variable> spec, Pageable pageable) {
    return new BizTemplate<Page<Variable>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Variable> process() {
        return variablesRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public List<Server> findServerByServiceId(Long serviceId) {
    return new BizTemplate<List<Server>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<Server> process() {
        Set<Long> variableIds = variableTargetQuery.findVariableIdByServiceId(serviceId);
        if (isEmpty(variableIds)){
          return emptyList();
        }
        List<Variable> variables = variablesRepo.findByIdAndExtracted(variableIds, true);
        if (isEmpty(variables)) {
          return emptyList();
        }
        return variables.stream().filter(x -> x.getExtraction() instanceof HttpExtraction)
            .map(x -> (HttpExtraction) x.getExtraction())
            .filter(x -> nonNull(x.getRequest().getServer())
                && nonNull(x.getRequest().getServer().getUrl()))
            .map(x -> toServer(x.getRequest().getServer()))
            .filter(ObjectUtils.distinctByKey(Server::getUrl))
            .collect(Collectors.toList());
      }
    }.execute();
  }

  @Override
  public List<Variable> findByProjectAndIds(Long projectId, @Nullable LinkedHashSet<Long> ids) {
    return new BizTemplate<List<Variable>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<Variable> process() {
        return isEmpty(ids) ? variablesRepo.findByProjectId(projectId)
            : variablesRepo.findByProjectIdAndIdIn(projectId, ids);
      }
    }.execute();
  }

  @Override
  public Variable checkAndFind(Long id) {
    return variablesRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Variable"));
  }

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

  @Override
  public void checkTenantQuota(int inc) {
    long count = variablesRepo.countByTenantId(getTenantId());
    commonQuery.checkTenantQuota(QuotaResource.AngusTesterVariable,
        Collections.singleton(getTenantId()), count + inc);
  }

  @Override
  public void checkRequiredParam(Variable variable) {
    if (variable.getExtracted() && isNull(variable.getExtraction())) {
      throw CommProtocolException.of(PARAM_MISSING_T, new Object[]{"extraction"});
    }

    if (variable.getExtracted() && variable.getExtraction().getMethod().needExpression()
        && isEmpty(variable.getExtraction().getExpression())) {
      throw CommProtocolException.of(PARAM_MISSING_T, new Object[]{"expression"});
    }
  }

  @Override
  public void checkAddNameExists(Variable variable) {
    if (variablesRepo.existsByProjectIdAndName(variable.getProjectId(), variable.getName())) {
      throw ResourceExisted.of(VARIABLE_NAME_REPEATED_T, new Object[]{variable.getName()});
    }
  }

  @Override
  public void checkUpdateNameExists(Variable variable) {
    if (variablesRepo.existsByProjectIdAndNameAndIdNot(variable.getProjectId(),
        variable.getName(), variable.getId())) {
      throw ResourceExisted.of(VARIABLE_NAME_REPEATED_T, new Object[]{variable.getName()});
    }
  }

  @Override
  public void setSafeCloneName(Variable variable) {
    String saltName = randomAlphanumeric(3);
    String clonedName = variablesRepo.existsByName(variable.getName() + "-Copy")
        ? variable.getName() + "-Copy." + saltName : variable.getName() + "-Copy";
    clonedName = clonedName.length() > DEFAULT_NAME_LENGTH ? clonedName.substring(0,
        DEFAULT_NAME_LENGTH - 3) + saltName : clonedName;
    variable.setName(clonedName);
  }

  private String getStaticVariableValue(Variable variableDb) {
    if (variableDb.hasMockValue()) {
      try {
        return defaultMockTextReplacer.replace(variableDb.getValue());
      } catch (Exception e) {
        throw CommProtocolException.of(e.getMessage());
      }
    }
    return variableDb.getValue();
  }

  private void assembleExtractVariable(
      cloud.xcan.angus.model.element.variable.Variable angusVariable) {
    try {
      assembleVariableHttpExtraction(List.of(angusVariable));
    } catch (Exception e) {
      throw CommProtocolException.of(
          nonNull(e.getCause()) ? e.getCause().getMessage() : e.getMessage());
    }
    defaultVariableExtractor.extract(List.of(angusVariable));
    if (nonNull(angusVariable.getExtraction())
        && nonNull(angusVariable.getExtraction().getFailureMessage())) {
      throw CommProtocolException.of(angusVariable.getExtraction().getFailureMessage());
    }
  }

}
