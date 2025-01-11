package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.core.angustester.domain.func.favourite.FuncCaseFavouriteP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncCaseFavouriteQuery {

  Page<FuncCaseFavouriteP> search(Long projectId, String apisName, PageRequest pageable);

  Long count(Long projectId);
}




