package cloud.xcan.sdf.core.angustester.domain;

import cloud.xcan.sdf.spec.experimental.Entity;

public interface ResourceFavouriteAndFollow<T extends Entity<T, ID>, ID>{

  Long getId();

  T setId(Long id);

  Boolean getFavouriteFlag();

  T setFavouriteFlag(Boolean favouriteFlag);

  Boolean getFollowFlag();

  T setFollowFlag(Boolean followFlag);
}
