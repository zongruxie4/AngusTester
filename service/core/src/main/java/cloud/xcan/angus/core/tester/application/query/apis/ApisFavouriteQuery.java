package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisFavouriteQuery {

  Page<ApisFavouriteP> search(Long projectId, String apisName, PageRequest pageable);

  Long count(Long projectId);
}




