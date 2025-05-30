package cloud.xcan.angus.core.tester.domain.apis.design;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisDesignInfoRepo extends BaseRepository<ApisDesignInfo, Long> {

  ApisDesignInfo findByDesignSourceId(Long serviceId);
}
