package cloud.xcan.angus.core.tester.domain.shard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "shard_tables")
@Setter
@Getter
@Accessors(chain = true)
public class ShardTables {

  @Id
  @Column(name = "id")
  private Long id;

  @Column(name = "tenant_id")
  private Long tenantId;

  @Column(name = "table_name")
  private String tableName;

  @Column(name = "db_index")
  private Integer dbIndex;

}
