package cloud.xcan.angus.core.tester.domain.project.template.content;

import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "templateType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = IssueTemplateContent.class, name = "ISSUE"),
    @JsonSubTypes.Type(value = PlanTemplateContent.class, name = "TEST_PLAN"),
    @JsonSubTypes.Type(value = CaseTemplateContent.class, name = "TEST_CASE")
})
public interface TemplateContent {

  // Inner parameter
  @JsonIgnore
  TemplateType getTemplateType();

}
