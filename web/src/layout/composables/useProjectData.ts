import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { appContext, localStore, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { notification } from '@xcan-angus/vue-ui';
import { project } from '@/api/tester';
import { ProjectType } from '@/enums/enums';
import type { ProjectInfo, ProjectDisplayInfo, ProjectSearchState } from '../types';

/**
 * Composable for managing project data and operations
 */
export function useProjectData () {
  const { t } = useI18n();
  const route = useRoute();
  const router = useRouter();

  // Constants
  const PROJECT_ID_STORE_KEY = window.btoa('GPID');
  const userInfo = ref(appContext.getContext().user);

  // State
  const loading = ref(false);
  const projectList = ref<ProjectInfo[]>([]);
  const projectSearchState = ref<ProjectSearchState>({
    query: '',
    filteredList: []
  });
  const currentProject = ref<ProjectDisplayInfo>();
  const currentProjectId = ref<string>();
  const addProjectVisible = ref(false);

  /**
   * Load project data from API
   */
  const loadProjectData = async (): Promise<void> => {
    loading.value = true;
    const [error, res] = await project.getJoinedProject(appContext.getContext().user?.id || 0);
    loading.value = false;

    if (error) {
      return;
    }

    const list = res?.data || [];
    projectList.value = list;
    projectSearchState.value.filteredList = list;

    if (list.length < 1) {
      notification.warning(t('project.noJoinedProjects'));
      currentProject.value = undefined;
      addProjectVisible.value = true;
      return;
    }

    await handleProjectSelection(list);
  };

  /**
   * Handle project selection logic
   */
  const handleProjectSelection = async (list: ProjectInfo[]): Promise<void> => {
    const localId = localStore.get(PROJECT_ID_STORE_KEY);

    // Check if projectId is in route query
    if (route.query?.projectId && localId !== route.query?.projectId) {
      const targetProject = list.find(item => item.id === route.query.projectId);
      if (targetProject) {
        selectProject(targetProject);
        return;
      } else {
        notification.error(t('project.projectNotExists'));
      }
    }

    // Check local storage for saved project
    const localTarget = list.find(item => item.id === localId);
    if (localTarget) {
      currentProject.value = list.find(item => item.id === localId);
      currentProjectId.value = currentProject.value?.id;
    } else {
      selectProject(list[0]);
    }
  };

  /**
   * Select a project
   */
  const selectProject = (project: ProjectInfo): void => {
    const { id, avatar, name, createdBy, ownerId, type } = project;
    currentProject.value = { id, avatar, name, createdBy, ownerId, type };
    currentProjectId.value = currentProject.value?.id;
    localStore.set(PROJECT_ID_STORE_KEY, id);
  };

  /**
   * Change project information
   */
  const changeProjectInfo = async (projectId?: string, force = false): Promise<void> => {
    if (!force) {
      const target = projectList.value.find(i => i.id === projectId);
      if (target) {
        selectProject(target);
        return;
      } else {
        localStore.remove(PROJECT_ID_STORE_KEY);
        await loadProjectData();
        return;
      }
    }

    const [error, { data }] = await project.getProjectDetail(projectId || '');
    if (error) {
      notification.warning(t('project.projectNotFound'));
      await loadProjectData();
      return;
    }

    const { avatar, id, name, createdBy, ownerId, type } = data;
    currentProject.value = { avatar, id, name, createdBy, ownerId, type };
    currentProjectId.value = currentProject.value?.id;
  };

  /**
   * Search projects with debounced input
   */
  const searchProjects = debounce(duration.search, (query: string): void => {
    projectSearchState.value.query = query;
    projectSearchState.value.filteredList = projectList.value.filter(item =>
      item.name.includes(query)
    );
  });

  /**
   * Computed property for project type visibility
   */
  const projectTypeVisibility = computed(() => {
    if (currentProject.value?.type?.value === ProjectType.TESTING) {
      return {
        showTask: false,
        showBackLog: false,
        showSprint: false,
        showMeeting: false,
        showTaskStatistics: false
      };
    }

    if (currentProject.value?.type?.value === ProjectType.GENERAL) {
      return {
        showTask: true,
        showBackLog: false,
        showSprint: false,
        showMeeting: false,
        showTaskStatistics: false
      };
    }

    return {
      showTask: true,
      showBackLog: true,
      showSprint: true,
      showMeeting: true,
      showTaskStatistics: true
    };
  });

  /**
   * Handle project type change and route navigation
   */
  const handleProjectTypeChange = (): void => {
    if (currentProject.value?.type?.value === ProjectType.TESTING) {
      if (route.path.includes('task')) {
        router.replace('/project');
      }
    }
  };

  return {
    // State
    loading,
    projectList,
    projectSearchState,
    currentProject,
    currentProjectId,
    addProjectVisible,
    userInfo,

    // Methods
    loadProjectData,
    selectProject,
    changeProjectInfo,
    searchProjects,
    handleProjectTypeChange,

    // Computed
    projectTypeVisibility
  };
}
