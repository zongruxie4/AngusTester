package cloud.xcan.angus.core.tester.domain.test.template.content;

import cloud.xcan.angus.core.tester.domain.test.template.TestTemplateType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "templateType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TestPlanTemplateContent.class, name = "templateType"),
    @JsonSubTypes.Type(value = TestCaseTemplateContent.class, name = "templateType")
})
public interface TestTemplateContent {

  TestTemplateType getTemplateType();

}
