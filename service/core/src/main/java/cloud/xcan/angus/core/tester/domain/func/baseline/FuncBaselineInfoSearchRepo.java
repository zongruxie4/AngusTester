package cloud.xcan.angus.core.tester.domain.func.baseline;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncBaselineInfoSearchRepo extends CustomBaseRepository<FuncBaselineInfo> {

}
