package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import cloud.xcan.sdf.core.angustester.domain.analysis.AnalysisSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class AnalysisSearchRepoMysql extends SimpleSearchRepository<Analysis>
    implements AnalysisSearchRepo {

}
