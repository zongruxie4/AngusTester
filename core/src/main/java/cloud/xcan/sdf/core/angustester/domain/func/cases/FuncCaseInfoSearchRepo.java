package cloud.xcan.sdf.core.angustester.domain.func.cases;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncCaseInfoSearchRepo extends CustomBaseRepository<FuncCaseInfo> {

}
