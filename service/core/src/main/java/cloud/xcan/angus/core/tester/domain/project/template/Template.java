package cloud.xcan.angus.core.tester.domain.project.template;


import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.project.template.content.TemplateContent;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "func_test_template")
@Setter
@Getter
@Accessors(chain = true)
public class Template extends TenantAuditingEntity<Template, Long>  {

  @Id
  private Long id;

  private String name;

  @Column(name = "template_type")
  @Enumerated(EnumType.STRING)
  private TemplateType templateType;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "template_content")
  private TemplateContent templateContent;

  @Column(name = "is_system")
  private Boolean isSystem;

  @Override
  public Long identity() {
    return this.id;
  }

}
