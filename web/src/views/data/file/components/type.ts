/**
 * Interface for capacity source data
 */
export interface SourceType {
  availableSize?: string;
  usage?: string;
  usedSize?: string;
}

/**
 * Interface for tracking upload file status
 */
export interface UploadFileStatus {
  name: string;
  size: string;
  progress: number;
  status: number; // 0 = uploading, 1 = failed, 2 = completed
}

/**
 * Interface for move target information
 */
export interface MoveTarget {
  targetSpaceId?: string;
  targetDirectoryId?: string;
}

/**
 * Interface for sort menu items
 */
export interface SortMenuItem {
  key: string;
  name: string;
  orderSort: string;
}

/**
 * Interface for sort type
 */
export interface SortType {
  orderBy?: string;
  orderSort?: string;
}

/**
 * Interface for space information column configuration
 */
export interface SpaceInfoColumnType {
  dataIndex: string;
  label: string;
  customRender?: ({ text }: { text: any }) => any;
}
