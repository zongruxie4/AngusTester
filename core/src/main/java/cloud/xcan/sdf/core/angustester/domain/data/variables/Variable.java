package cloud.xcan.sdf.core.angustester.domain.data.variables;


import static cloud.xcan.angus.function.impl.utils.FunctionChecker.hasMockFunction;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.sdf.spec.annotations.SpecIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(name = "data_variable")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@DynamicInsert
@Setter
@Getter
@Accessors(chain = true)
public class Variable extends TenantAuditingEntity<Variable, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  private String name;

  private String description;

  private Boolean extracted;

  private String value;

  @Column(name = "password_value")
  private Boolean passwordValue;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "extraction")
  private DefaultExtraction extraction;

  @Transient
  private Boolean hasMockValue = null;

  @Override
  public Long getParentId() {
    return projectId;
  }

  @Override
  public Long getProjectId() {
    return projectId;
  }

  @SpecIgnore
  public boolean isExtractable() {
    return extracted && isEmpty(value) && nonNull(extraction);
  }

  @JsonIgnore
  public Boolean hasMockValue() {
    // Cache dynamic value status
    if (isNull(hasMockValue)) {
      hasMockValue = isNotEmpty(value) && hasMockFunction(value);
    }
    return hasMockValue;
  }

  @Override
  public Long identity() {
    return id;
  }
}
