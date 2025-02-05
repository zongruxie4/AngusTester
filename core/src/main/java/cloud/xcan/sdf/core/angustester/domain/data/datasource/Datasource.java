package cloud.xcan.sdf.core.angustester.domain.data.datasource;


import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.sdf.spec.experimental.AESValue;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;


@Entity
@Table(name = "data_datasource")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Setter
@Getter
@Accessors(chain = true)
public class Datasource extends TenantAuditingEntity<Datasource, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  private String name;

  @Column(name = "database0")
  private String database;

  @Column(name = "driver_class_name")
  private String driverClassName;

  private String username;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "passd")
  private AESValue passd;

  /**
   * Connection URL Syntax:
   * <p>
   * https://docs.oracle.com/cd/E17952_01/connector-j-8.0-en/connector-j-reference-jdbc-url-format.html
   * <p>
   * protocol//[hosts][/database][?properties] -> jdbc:mysql://host1:33060/sakila
   * <p>
   * Note: The format of jdbcUrl is different without database.
   */
  @Column(name = "jdbc_url")
  private String jdbcUrl;

  @Transient
  private Boolean connSuccess;
  @Transient
  private String connFailureMessage;
  @Transient
  private String lastModifiedByName;
  @Transient
  private String avatar;

  public String getDecryptPassd() {
    return nonNull(passd) ? passd.decrypt(String.valueOf(getOptTenantId())) : null;
  }

  @Override
  public Long identity() {
    return this.id;
  }
}
