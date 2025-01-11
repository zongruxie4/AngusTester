package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.favourite.ApisFavouriteP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisFavouriteQuery {

  Page<ApisFavouriteP> search(Long projectId, String apisName, PageRequest pageable);

  Long count(Long projectId);
}




