package cloud.xcan.sdf.core.angustester.domain.report;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ReportSearchRepo extends CustomBaseRepository<ReportInfo> {

}
