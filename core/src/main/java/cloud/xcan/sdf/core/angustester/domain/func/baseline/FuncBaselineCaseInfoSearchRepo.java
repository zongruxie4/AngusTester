package cloud.xcan.sdf.core.angustester.domain.func.baseline;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncBaselineCaseInfoSearchRepo extends CustomBaseRepository<FuncBaselineCaseInfo> {

}
