package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.indicator;

import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorFuncRepo;
import org.springframework.stereotype.Repository;

@Repository("indicatorFuncRepo")
public interface IndicatorFuncRepoPostgres extends IndicatorFuncRepo {


}
