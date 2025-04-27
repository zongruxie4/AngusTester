package cloud.xcan.angus.core.tester.domain.mock.apis;


import cloud.xcan.angus.core.biz.ResourceName;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;



@Entity
@Table(name = "mock_apis")
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
