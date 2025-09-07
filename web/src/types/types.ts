import {CreatedAt, PageQuery, PeriodicCreationUnit} from '@xcan-angus/infra';

export interface ProjectPageQuery extends PageQuery{
  projectId: string | number;
}

/**
 * Interface for tab pane configuration
 */
export interface IPane {
  /** Unique identifier for the pane */
  _id: string;

  /** Display text for the tab */
  name: string;

  /** Component identifier within the pane */
  value: string;

  /** Whether the tab can be closed (true: closable, false: not closable) */
  closable?: boolean;

  /** Whether to render DOM structure when hidden */
  forceRender?: boolean;

  /** Icon displayed before the tab text */
  icon?: string;

  /** Whether the tab is currently active (used for caching the last activated tab) */
  active?: boolean;

  // Component-specific properties
  /** Whether the component is in read-only mode */
  readonly?: boolean;

  /** Component required properties **/
  data?: { [key: string]: any; };

  /** Component extension properties **/
  [key: string]: any;
}

// Time setting interface for CreatedDate component
export interface CreateTimeSetting {
  createdAt: CreatedAt;
  createdAtSomeDate?: string;
  periodicCreationUnit?: PeriodicCreationUnit;
  dayOfMonth?: string;
  dayOfWeek?: string;
  timeOfDay?: string;
  hourOfDay?: string;
}
