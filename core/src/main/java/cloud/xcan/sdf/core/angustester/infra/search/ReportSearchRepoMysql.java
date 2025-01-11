package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.report.ReportInfo;
import cloud.xcan.sdf.core.angustester.domain.report.ReportSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ReportSearchRepoMysql extends SimpleSearchRepository<ReportInfo>
    implements ReportSearchRepo {

}
