package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.core.tester.domain.test.favourite.FuncCaseFavouriteP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncCaseFavouriteQuery {

  Page<FuncCaseFavouriteP> list(Long projectId, String apisName, PageRequest pageable);

  Long count(Long projectId);
}




