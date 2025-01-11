package cloud.xcan.sdf.core.angustester.domain.mock.apis;


import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.biz.ResourceName;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.TypeDef;


@Entity
@Table(name = "mock_apis")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Setter
@Getter
@Accessors(chain = true)
public class MockApisBase extends TenantAuditingEntity<MockApisBase, Long> implements
    ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "mock_service_id")
  private Long mockServiceId;

  @ResourceName
  @Column(name = "summary")
  private String summary;

  @Override
  public String getName() {
    return summary;
  }

  @Override
  public Long getParentId() {
    return mockServiceId;
  }

  @Override
  public Long getProjectId() {
    return projectId;
  }


  @Override
  public Long identity() {
    return this.id;
  }
}
