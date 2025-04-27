package cloud.xcan.angus.core.tester.domain.data.variables;


import static cloud.xcan.angus.model.FunctionChecker.hasMockFunction;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import cloud.xcan.angus.spec.annotations.SpecIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;


@Entity
@Table(name = "data_variable")
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

  @Type(JsonType.class)
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
