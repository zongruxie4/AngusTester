import { EnumMessage, PageQuery } from '@xcan-angus/infra';
import { ProjectType } from '@/enums/enums';
import { Ref } from 'vue';

/**
 * Project member interface - represents a project member with basic info
 * @interface ProjectMember
 */
export interface ProjectMember {
  /** Unique identifier for the member */
  id: string;
  /** Display name of the member */
  name: string;
  /** Avatar URL of the member (optional) */
  avatar?: string;
  /** Full name of the member (used in forms) */
  fullName?: string;
  /** Whether the member is disabled for selection */
  disabled?: boolean;
}

/**
 * Project members grouped by type - organizes members by their roles
 * @interface ProjectMembers
 */
export interface ProjectMembers {
  /** Individual user members */
  USER?: ProjectMember[];
  /** Department members */
  DEPT?: ProjectMember[];
  /** Group members */
  GROUP?: ProjectMember[];
}

/**
 * Main Project interface - represents a complete project entity
 * @interface Project
 */
export interface Project {
  /** Unique project identifier */
  id?: string;
  /** Project name */
  name?: string;
  /** Project description (rich text content) */
  description?: string;
  /** Project avatar/logo URL */
  avatar?: string;
  /** Project owner's user ID */
  ownerId?: string;
  /** Project owner's display name */
  ownerName?: string;
  /** Project type with enum value and display message */
  type?: EnumMessage<ProjectType>;
  /** All project members organized by type */
  members?: ProjectMembers;
  /** Total number of members (calculated field) */
  membersNum?: number;
  /** Members to display in UI (limited for performance) */
  showMembers?: {
    USER: ProjectMember[];
    DEPT: ProjectMember[];
    GROUP: ProjectMember[];
  };
  /** Project start date (ISO string) */
  startDate?: string;
  /** Project deadline date (ISO string) */
  deadlineDate?: string;
  /** User ID of the project creator */
  createdBy?: string;
  /** Last modification timestamp */
  lastModifiedDate?: string;
  /** Date range for form input [start, end] */
  dateRange?: [string, string];
  /** Whether to import example data on creation */
  importExample?: boolean;
}

/**
 * Tree data structure interface - used for hierarchical data display
 * @interface TreeData
 */
export interface TreeData {
  /** Display name of the tree node */
  name: string;
  /** Sequence/order of the node */
  sequence: string;
  /** Depth level in the tree (0-based) */
  level: number;
  /** Child nodes array */
  children?: TreeData[];
  /** Unique identifier for the tree node */
  id: string;
  /** Parent node ID (if applicable) */
  pid?: string;
  /** Index position among siblings */
  index?: number;
  /** Array of parent IDs (path from root) */
  ids?: string[];
  /** Whether this is the last child in its level */
  isLast?: boolean;
  /** Maximum depth of child levels */
  childLevels?: number;
  /** Whether the node is disabled for interaction */
  disabled?: boolean;
}

/**
 * Project API query parameters - used for list endpoints
 * @interface ProjectApiParams
 */
export interface ProjectApiParams extends PageQuery {
  /** Search keyword for project name */
  keyword?: string;
  /** Project owner filter */
  ownerId?: string;
  /** Project type filter */
  projectType?: ProjectType;
  /** Date range filter */
  dateRange?: [string, string];
}

/**
 * Parameters for creating a new project
 * @interface CreateProjectParams
 */
export interface CreateProjectParams {
  /** Project name (required) */
  name: string;
  /** Project owner ID (required) */
  ownerId: string;
  /** Project type (required) */
  type: ProjectType;
  /** Project description (optional) */
  description?: string;
  /** Project avatar URL (optional) */
  avatar?: string;
  /** Project start date */
  startDate?: string;
  /** Project deadline date */
  deadlineDate?: string;
  /** Member type IDs organized by type */
  memberTypeIds?: {
    USER?: string[];
    DEPT?: string[];
    GROUP?: string[];
  };
  /** Whether to import example data */
  importExample?: boolean;
}

/**
 * Parameters for updating an existing project
 * @interface UpdateProjectParams
 */
export interface UpdateProjectParams extends CreateProjectParams {
  /** Project ID (required for updates) */
  id: string;
}

/**
 * Parameters for deleting a project
 * @interface DeleteProjectParams
 */
export interface DeleteProjectParams {
  /** Project ID to delete */
  id: string;
}

/**
 * API response for project list
 * @interface ProjectListResponse
 */
export interface ProjectListResponse {
  /** Array of projects */
  list: Project[];
  /** Total count of projects */
  total: number;
  /** Current page number */
  pageNo: number;
  /** Page size */
  pageSize: number;
}

/**
 * Properties for the main project component
 * @interface ProjectProps
 */
export interface ProjectProps {
  /** Current project ID */
  projectId?: string;
  /** Current user information */
  userInfo?: {
    id: string | number;
    fullName?: string;
    name?: string;
    avatar?: string;
  };
  /** Application information */
  appInfo?: {
    id?: number;
  };
}

/**
 * Properties for the project list component
 * @interface ProjectListProps
 */
export interface ProjectListProps extends ProjectProps {
  /** Whether user has admin privileges */
  isAdmin?: boolean;
}

/**
 * Properties for project add/edit modal
 * @interface ProjectModalProps
 */
export interface ProjectModalProps {
  /** Whether modal is visible */
  visible: boolean;
  /** Project data for editing (undefined for new project) */
  dataSource?: Project;
  /** Whether modal can be closed */
  closable?: boolean;
}

/**
 * Properties for project detail component
 * @interface ProjectDetailProps
 */
export interface ProjectDetailProps {
  /** Project ID to display */
  projectId: string;
  /** Initial tab to show */
  data?: {
    tab?: string;
  };
  /** Whether component is closable */
  closable?: boolean;
}

/**
 * Raw project data from API (before processing)
 * @interface RawProjectData
 */
export interface RawProjectData {
  id: string;
  name: string;
  description?: string;
  avatar?: string;
  ownerId: string;
  ownerName: string;
  type: EnumMessage<ProjectType>;
  members: {
    USER?: any[];
    DEPT?: any[];
    GROUP?: any[];
  };
  startDate: string;
  deadlineDate: string;
  createdBy: string;
  lastModifiedDate: string;
}

/**
 * Project type configuration interface
 * @interface ProjectTypeConfig
 */
export interface ProjectTypeConfig {
  /** Configuration for Agile projects */
  AGILE: string[];
  /** Configuration for General projects */
  GENERAL: string[];
  /** Configuration for Testing projects */
  TESTING: string[];
}

/**
 * Sort options for project list
 * @interface ProjectSortOption
 */
export interface ProjectSortOption {
  /** Display name for the sort option */
  name: string;
  /** Field to sort by */
  key: 'createdDate' | 'createdByName' | 'name' | 'lastModifiedDate';
  /** Sort direction */
  orderSort: PageQuery.OrderSort;
}

export interface DetailProps {
  closable: boolean;
  projectId: string;
  data?: {
    tab: string
  }
}

/**
 * Form validation result interface
 */
export interface FormValidationResult {
  valid: boolean;
  errors?: string[];
}

/**
 * Member type for UI selection
 */
export type MemberType = 'user' | 'dept' | 'group';

/**
 * Default options for member selectors
 */
export interface DefaultOptions {
  [key: string]: ProjectMember & { fullName?: string };
}

/**
 * Return type of useProjectForm composable
 */
export interface UseProjectFormReturn {
  // State
  loading: Ref<boolean>;
  formRef: Ref<any>;
  projectDetail: Ref<Project>;
  descRef: Ref<any>;
  // Methods
  loadProjectDetail: (projectId: string) => Promise<void>;
  validateForm: () => Promise<FormValidationResult>;
  submitForm: () => Promise<void>;
  resetForm: () => void;
  validateDescription: () => Promise<void>;
  selectProjectType: (type: ProjectType) => void;
  cancelForm: () => void;
}

/**
 * Return type of useProjectMembers composable
 */
export interface UseProjectMembersReturn {
  // State
  memberType: Ref<MemberType>;
  members: Ref<{
    USER: string[];
    DEPT: string[];
    GROUP: string[];
  }>;
  defaultOptionsUser: Ref<DefaultOptions>;
  defaultOptionsDept: Ref<DefaultOptions>;
  defaultOptionsGroup: Ref<DefaultOptions>;
  // Methods
  initializeMembers: (projectMembers?: ProjectMembers) => void;
  getMembersForSubmission: () => {
    USER?: string[];
    DEPT?: string[];
    GROUP?: string[];
  };
  resetMembers: () => void;
  addMember: (type: 'USER' | 'DEPT' | 'GROUP', memberId: string, memberData?: ProjectMember) => void;
  removeMember: (type: 'USER' | 'DEPT' | 'GROUP', memberId: string) => void;
  getTotalMemberCount: () => number;
  hasMembers: () => boolean;
  switchMemberType: (type: MemberType) => void;
}

/**
 * Return type of useProjectAvatar composable
 */
export interface UseProjectAvatarReturn {
  // State
  uploadAvatarVisible: Ref<boolean>;
  // Methods
  openCropper: () => void;
  closeCropper: () => void;
  uploadImgSuccess: (response: any) => void;
  deleteImage: () => void;
  handleUploadError: (error: any) => void;
  validateImageFile: (file: File) => boolean;
  getCurrentAvatar: () => string;
  hasAvatar: () => boolean;
  resetAvatar: () => void;
}

/**
 * Enhanced project edit props interface
 */
export interface ProjectEditProps {
  /** Project ID for editing (undefined for new project) */
  projectId?: string;
  /** Additional data passed to component */
  data?: {
    tab?: string;
    [key: string]: any;
  };
}

/**
 * Project edit emit events interface
 */
export interface ProjectEditEmits {
  /** Emitted when form is successfully submitted */
  (e: 'ok'): void;

  /** Emitted when form is canceled */
  (e: 'cancel'): void;
}
