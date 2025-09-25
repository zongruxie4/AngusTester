import { ref, inject } from 'vue';
import { useI18n } from 'vue-i18n';
import { modal, notification } from '@xcan-angus/vue-ui';
import { project } from '@/api/tester';
import { getCurrentPage } from '@/utils/utils';
import type { Project, CreateProjectParams, UpdateProjectParams } from '../types';

interface TabPaneOptions {
  type: string;
  _id: string;
  name: string;
  id?: string;
  data?: { tab?: string; };
}

export function useActions (options: {
  onDataChange?: () => void;
  getCurrentPage?: (current: number, pageSize: number, total: number) => number;
} = {}) {
  const { t } = useI18n();

  const addTabPane = inject<(options: TabPaneOptions) => void>('addTabPane', () => {
    console.warn('addTabPane function not provided');
  });

  const delTabPane = inject<(keys: string | string[]) => void>('delTabPane', () => {
    console.warn('delTabPane function not provided');
  });

  const changeProjectInfo = inject<(projectId: string, force: boolean) => void>('changeProjectInfo', () => {
    console.warn('changeProjectInfo function not provided');
  });

  const getNewCurrentProject = inject<() => void>('getNewCurrentProject', () => {
    console.warn('getNewCurrentProject function not provided');
  });

  const modalVisible = ref(false);
  const editProjectData = ref<Project | undefined>();
  const operationLoading = ref(false);

  const openCreateModal = (): void => {
    editProjectData.value = undefined;
    modalVisible.value = true;
  };

  const openEditModal = (projectData: Project): void => {
    editProjectData.value = { ...projectData };
    modalVisible.value = true;
  };

  const closeModal = (): void => {
    modalVisible.value = false;
    editProjectData.value = undefined;
  };

  const addProjectTab = (): void => {
    addTabPane({
      type: 'project',
      _id: 'addProject',
      name: t('project.edit.modal.addProject')
    });
  };

  const editProjectTab = (projectData: Project, activeTab?: string): void => {
    addTabPane({
      type: 'project',
      _id: `${projectData.id}_project`,
      name: projectData.name || t('project.untitled'),
      id: projectData.id,
      data: { tab: activeTab || 'basic' }
    });
  };

  const openDetailTab = (projectData: Project): void => {
    addTabPane({
      _id: `${projectData.id}_detail`,
      name: projectData.name || t('project.untitled'),
      type: 'projectDetail',
      id: projectData.id
    });
  };

  const createProject = async (params: CreateProjectParams): Promise<boolean> => {
    try {
      operationLoading.value = true;
      const [error, response] = await project.addProject(params);

      if (error) {
        console.error('Failed to create project:', error);
        return false;
      }

      notification.success(t('actions.tips.createSuccess'));
      options.onDataChange?.();

      if (response.data?.id) {
        changeProjectInfo(response.data.id, true);
      }

      return true;
    } catch (error) {
      console.error('Error creating project:', error);
      return false;
    } finally {
      operationLoading.value = false;
    }
  };

  const updateProject = async (params: UpdateProjectParams): Promise<boolean> => {
    try {
      operationLoading.value = true;
      const [error] = await project.updateProject(params);

      if (error) {
        console.error('Failed to update project:', error);
        return false;
      }

      notification.success(t('actions.tips.updateSuccess'));
      options.onDataChange?.();
      changeProjectInfo(params.id, true);

      return true;
    } catch (error) {
      console.error('Error updating project:', error);
      return false;
    } finally {
      operationLoading.value = false;
    }
  };

  const deleteProject = async (
    projectData: Project,
    paginationInfo: { current: number; pageSize: number; total: number },
    currentProjectId?: string
  ): Promise<boolean> => {
    return new Promise((resolve) => {
      modal.confirm({
        content: t('actions.tips.confirmDelete', { name: projectData.name }),
        async onOk () {
          try {
            operationLoading.value = true;
            const [error] = await project.deleteProject(projectData.id!);

            if (error) {
              console.error('Failed to delete project:', error);
              resolve(false);
              return;
            }

            notification.success(t('actions.tips.deleteSuccess'));

            // Calculate new current page after deletion (used for pagination update)
            options.getCurrentPage
              ? options.getCurrentPage(paginationInfo.current, paginationInfo.pageSize, paginationInfo.total)
              : getCurrentPage(paginationInfo.current, paginationInfo.pageSize, paginationInfo.total);

            const tabsToClose = [
              projectData.id!,
              `${projectData.id}_detail`,
              `${projectData.id}_project`
            ];
            delTabPane(tabsToClose);

            if (projectData.id === currentProjectId) {
              changeProjectInfo('', false);
            } else {
              getNewCurrentProject();
            }

            options.onDataChange?.();
            resolve(true);
          } catch (error) {
            console.error('Error deleting project:', error);
            resolve(false);
          } finally {
            operationLoading.value = false;
          }
        },
        onCancel () {
          resolve(false);
        }
      });
    });
  };

  const canEditProject = (projectData: Project, userId: string, isAdmin: boolean): boolean => {
    if (isAdmin) return true;
    return String(userId) === projectData.ownerId || String(userId) === projectData.createdBy;
  };

  const canDeleteProject = (projectData: Project, userId: string, isAdmin: boolean): boolean => {
    return canEditProject(projectData, userId, isAdmin);
  };

  return {
    modalVisible,
    editProjectData,
    operationLoading,
    openCreateModal,
    openEditModal,
    closeModal,
    addProjectTab,
    editProjectTab,
    openDetailTab,
    createProject,
    updateProject,
    deleteProject,
    canEditProject,
    canDeleteProject
  };
}

export type UseActionsReturn = ReturnType<typeof useActions>;
