package cloud.xcan.angus.core.tester.domain.shard;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ShardTablesRepo extends BaseRepository<ShardTables, Long> {

  List<ShardTables> findByTableNameLike(String tenantId);

}
