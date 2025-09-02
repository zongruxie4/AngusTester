import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string) => value);

/**
 * <p>Table column configuration for file list.</p>
 * <p>Defines the structure and display properties for file table columns.</p>
 */
export const columns = [
  {
    title: t('fileSpace.columns.name'),
    dataIndex: 'name',
    width: '30%',
    ellipsis: true
  },
  {
    title: t('fileSpace.columns.fileCount'),
    dataIndex: 'fileNum',
    width: 60,
    ellipsis: true
  },
  {
    title: t('fileSpace.columns.folderCount'),
    dataIndex: 'subDirectoryNum',
    width: 80,
    ellipsis: true
  },
  {
    title: t('fileSpace.columns.actualSize'),
    dataIndex: 'size',
    ellipsis: true,
    width: 80
  },
  {
    title: t('fileSpace.columns.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    ellipsis: true,
    width: 160
  },
  {
    title: t('fileSpace.columns.action'),
    dataIndex: 'action',
    width: 380
  }
];

/**
 * <p>Default space permissions for admin users.</p>
 * <p>Includes all available permissions for space management.</p>
 */
export const SPACE_PERMISSIONS = [
  'VIEW',
  'MODIFY',
  'DELETE',
  'SHARE',
  'GRANT',
  'OBJECT_READ',
  'OBJECT_WRITE',
  'OBJECT_DELETE'
] as const;

/**
 * <p>Type for space permission values.</p>
 */
export type SpacePermission = typeof SPACE_PERMISSIONS[number];

/**
 * <p>File type information including display message and value.</p>
 */
export interface FileType {
  /** Display message for the file type */
  message: string;
  /** Internal value identifier for the file type */
  value: 'FILE' | 'DIRECTORY';
}

/**
 * <p>File summary information including size and count data.</p>
 */
export interface FileSummary {
  /** Used size in bytes or formatted string */
  usedSize: number | string;
  /** Number of sub-files in directory */
  subFileNum: number | string;
  /** Number of sub-directories in directory */
  subDirectoryNum?: number | string;
}

/**
 * <p>Source type representing files and directories in the file system.</p>
 * <p>Extends base file information with UI state and navigation data.</p>
 */
export interface SourceType {
  /** Unique identifier for the file or directory */
  id: string;
  /** Name of the file or directory */
  name: string;
  /** File summary information */
  summary: FileSummary;
  /** File type information */
  type: FileType;
  /** Last modified date */
  lastModifiedDate: string;
  /** Flag indicating if rename mode is active */
  renameFlag?: boolean;
  /** Cached name for rename operations */
  cacheName?: string;
  /** Parent directory ID */
  parentDirectoryId?: string;
  /** Space ID containing this file */
  spaceId: string;
}

/**
 * <p>Space information type for space list display.</p>
 * <p>Contains space metadata and authorization information.</p>
 */
export interface SpaceInfoType {
  /** Unique identifier for the space */
  id: string;
  /** Name of the space */
  name: string;
  /** Array of authorization permissions */
  auth: SpacePermission[];
  /** Quota size configuration */
  quotaSize?: {
    /** Quota value */
    value: string;
    /** Quota unit information */
    unit: {
      /** Unit value identifier */
      value: string;
      /** Unit display message */
      message: string;
    };
  };
  /** Current used size */
  size: string;
  /** Number of sub-directories */
  subDirectoryNum: string;
  /** Number of sub-files */
  subFileNum: string;
  /** Name of user who created the space */
  createdByName: string;
  /** Date when space was created */
  createdDate: string;
}

/**
 * <p>Pagination configuration for table components.</p>
 */
export interface PaginationType {
  /** Current page number */
  current: number;
  /** Number of items per page */
  pageSize: number;
  /** Total number of items */
  total: number;
  /** Whether to hide pagination when only one page */
  hideOnSinglePage: boolean;
}

/**
 * <p>Search parameters for filtering data.</p>
 */
export interface SearchType {
  /** Search keyword for name filtering */
  name?: string;
}

/**
 * <p>Sorting parameters for data ordering.</p>
 */
export interface SortType {
  /** Field to order by */
  orderBy?: string;
  /** Sort direction (asc/desc) */
  orderSort?: 'asc' | 'desc';
}

/**
 * <p>Breadcrumb navigation item.</p>
 * <p>Represents a level in the file system navigation path.</p>
 */
export interface CrumbType {
  /** Unique identifier for the navigation level */
  id: string;
  /** Display name for the navigation level */
  name: string;
}

/**
 * <p>Form state for space editing.</p>
 * <p>Contains editable fields for space configuration.</p>
 */
export interface SpaceFormState {
  /** Space name */
  name: string;
  /** Quota size configuration */
  quotaSize: {
    /** Quota value */
    value?: string;
    /** Quota unit */
    unit: string;
  };
  /** Space remarks */
  remark: string;
  /** Space ID for editing existing space */
  id?: string;
}

/**
 * <p>Props for EditSpace component.</p>
 */
export interface EditSpaceProps {
  /** Whether the modal is visible */
  visible: boolean;
  /** Space ID to edit (undefined for new space) */
  id?: string;
}

/**
 * <p>Emit events for EditSpace component.</p>
 */
export interface EditSpaceEmits {
  /** Form submission event */
  (e: 'ok', form: Record<string, any>): void;
  /** Modal visibility update event */
  (e: 'update:visible', value: boolean): void;
}
