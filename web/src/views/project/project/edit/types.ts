import { Ref } from 'vue';
import type { Project, ProjectMember, ProjectMembers } from '../types';
import { ProjectType } from '@/enums/enums';

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
