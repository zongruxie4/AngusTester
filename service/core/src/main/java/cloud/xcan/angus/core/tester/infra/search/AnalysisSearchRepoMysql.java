package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.analysis.Analysis;
import cloud.xcan.angus.core.tester.domain.analysis.AnalysisSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class AnalysisSearchRepoMysql extends SimpleSearchRepository<Analysis>
    implements AnalysisSearchRepo {

}
