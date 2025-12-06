package cloud.xcan.angus.core.tester.domain.project.template.content;

import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlanTemplateContent implements TemplateContent {

  private TemplateType templateType = TemplateType.TEST_PLAN;

  private String testingScope;

  private String testingObjectives;

  private String acceptanceCriteria;

  private String otherInformation;

}
