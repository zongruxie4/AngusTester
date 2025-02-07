package cloud.xcan.sdf.core.angustester.application.converter;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;

public class VariableConverter {

  public static Variable toCloneVariable(Variable variableDb) {
    return new Variable().setProjectId(variableDb.getProjectId())
        .setName(variableDb.getName())
        .setExtracted(variableDb.getExtracted())
        .setValue(variableDb.getValue())
        .setPasswordValue(variableDb.getPasswordValue())
        .setExtraction(variableDb.getExtraction());
  }

  public static cloud.xcan.angus.model.element.variable.Variable toAngusVariable(
      Variable variableDb) {
    return cloud.xcan.angus.model.element.variable.Variable.newBuilder()
        .name(variableDb.getName()).value(variableDb.getValue())
        .passwordValue(variableDb.getPasswordValue())
        .extraction(variableDb.getExtraction())
        .build();
  }

  public static cloud.xcan.angus.model.element.variable.Variable toAngusVariable(
      String name, String value, DefaultExtraction extraction) {
    return cloud.xcan.angus.model.element.variable.Variable.newBuilder()
        .name(name).value(value)
        .extraction(extraction)
        .build();
  }

  public static Variable toVariable(Long projectId,
      cloud.xcan.angus.model.element.variable.Variable x) {
    return new Variable().setProjectId(projectId)
        .setName(x.getName()).setDescription(x.getDescription()).setValue(x.getValue())
        .setExtracted(nonNull(x.getExtraction())).setExtraction(x.getExtraction());
  }
}
