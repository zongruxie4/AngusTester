package cloud.xcan.sdf.core.angustester.domain.apis.design;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisDesignInfoRepo extends BaseRepository<ApisDesignInfo, Long> {

}
