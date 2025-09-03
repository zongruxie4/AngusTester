import { ref, inject, unref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { project } from '@/api/tester';
import { ProjectType } from '@/enums/enums';
import type { FormValidationResult, Project, UseProjectFormReturn } from '../types';

/**
 * Project form management composable
 * Handles form state, validation, submission, and data loading
 */
export function useForm (
  projectId?: string,
  members?: Ref<{
    USER: string[];
    DEPT: string[];
    GROUP: string[];
  }>
): UseProjectFormReturn {
  // ============================================================================
  // Dependencies
  // ============================================================================

  const { t } = useI18n();

  // Injected dependencies with type safety
  const changeProjectInfo = inject('changeProjectInfo',
    (projectId: string, force: boolean) => ({ projectId, force })
  );
  const getNewCurrentProject = inject('getNewCurrentProject', () => undefined);
  const projectInfo = inject('projectInfo', ref({ id: '' }));
  const delTabPane = inject('delTabPane',
    (tabKey: string) => { console.log('delTabPane not provided:', tabKey); }
  );
  // ============================================================================
  // State Management
  // ============================================================================

  /** Form loading state */
  const loading = ref(false);

  /** Form reference for validation */
  const formRef = ref<any>();

  /** Rich text editor reference for description validation */
  const descRef = ref<any>();

  /** Project detail form data */
  const projectDetail = ref<Project>({
    type: { value: ProjectType.AGILE, message: '' },
    importExample: false
  });
  // ============================================================================
  // Form Validation
  // ============================================================================

  /**
   * Validate description field length
   * @returns Promise that resolves if valid, rejects with error message if invalid
   */
  const validateDescription = async (): Promise<void> => {
    if (descRef.value && descRef.value.getLength() > 2000) {
      return Promise.reject(t('project.projectEdit.validation.maxCharacters'));
    }
    return Promise.resolve();
  };

  /**
   * Validate entire form
   * @returns Promise with validation result
   */
  const validateForm = async (): Promise<FormValidationResult> => {
    try {
      await formRef.value?.validate();
      return { valid: true };
    } catch (error: any) {
      const errors = Array.isArray(error)
        ? error.map(err => err.message || String(err))
        : [String(error)];

      return {
        valid: false,
        errors
      };
    }
  };
  // ============================================================================
  // Data Loading
  // ============================================================================

  /**
   * Load project detail data for editing
   * @param id - Project ID to load
   */
  const loadProjectDetail = async (id: string): Promise<void> => {
    if (!id) return;

    try {
      loading.value = true;

      const [error, response] = await project.getProjectDetail(id);

      if (error) {
        console.error('Failed to load project detail:', error);
        notification.error(t('project.projectEdit.messages.loadError'));
        return;
      }

      const { data } = response;
      const {
        startDate,
        deadlineDate,
        type,
        id: projectId,
        name,
        ownerId,
        description,
        avatar
      } = data;

      // Set form data
      projectDetail.value = {
        id: projectId,
        name,
        ownerId,
        description,
        avatar,
        type,
        importExample: false,
        members: data.members,
        dateRange: [startDate, deadlineDate]
      };
    } catch (error) {
      console.error('Error loading project detail:', error);
      notification.error(t('project.projectEdit.messages.loadError'));
    } finally {
      loading.value = false;
    }
  };
  // ============================================================================
  // Form Actions
  // ============================================================================
  /**
   * Select project type
   * @param type - Project type to select
   */
  const selectProjectType = (type: ProjectType): void => {
    if (projectDetail.value.type) {
      projectDetail.value.type.value = type;
    } else {
      projectDetail.value.type = { value: type, message: '' };
    }
  };

  /**
   * Submit form data
   */
  const submitForm = async (): Promise<void> => {
    try {
      // Validate form first
      const validation = await validateForm();
      if (!validation.valid) {
        return;
      }

      loading.value = true;

      const currentMembers = unref(members) || { USER: [], DEPT: [], GROUP: [] };
      const { dateRange, ...otherProject } = projectDetail.value;

      // Prepare submission data
      const submitData = {
        ...otherProject,
        startDate: dateRange?.[0],
        deadlineDate: dateRange?.[1],
        memberTypeIds: {
          USER: currentMembers.USER.length ? currentMembers.USER : undefined,
          DEPT: currentMembers.DEPT.length ? currentMembers.DEPT : undefined,
          GROUP: currentMembers.GROUP.length ? currentMembers.GROUP : undefined
        }
      };

      // Submit data (create or update)
      const [error] = projectId
        ? await project.updateProject(submitData)
        : await project.addProject(submitData);

      if (error) {
        console.error('Form submission error:', error);
        return;
      }

      // Handle success
      if (projectId === unref(projectInfo).id) {
        changeProjectInfo(projectId, true);
      }

      // Show success message
      const messageKey = projectId
        ? 'project.projectEdit.messages.updateSuccess'
        : 'project.projectEdit.messages.createSuccess';
      notification.success(t(messageKey));

      // Clean up tabs
      if (!projectId) {
        delTabPane('addProject');
      }

      getNewCurrentProject();
    } catch (error) {
      console.error('Error submitting form:', error);
      notification.error(t('project.projectEdit.messages.submitError'));
    } finally {
      loading.value = false;
    }
  };
  /**
   * Reset form to default state
   */
  const resetForm = (): void => {
    projectDetail.value = {
      type: { value: ProjectType.AGILE, message: '' },
      importExample: false
    };
    formRef.value?.resetFields();
  };

  /**
   * Cancel form and close tabs
   */
  const cancelForm = (): void => {
    delTabPane(`${projectId}_project`);
    delTabPane('addProject');
  };

  // ============================================================================
  // Return Interface
  // ============================================================================

  return {
    // State
    loading,
    formRef,
    projectDetail,
    descRef,

    // Methods
    loadProjectDetail,
    validateForm,
    validateDescription,
    submitForm,
    resetForm,
    selectProjectType,
    cancelForm
  };
}
