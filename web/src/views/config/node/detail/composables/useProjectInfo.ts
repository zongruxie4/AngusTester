import { inject, computed, Ref, ref } from 'vue';

/**
 * <p>Project information interface</p>
 * <p>Defines the structure of project data</p>
 */
interface ProjectInfo {
  /** Unique identifier for the project */
  id: string;
  /** Name of the project */
  name?: string;
  /** Description of the project */
  description?: string;
}

/**
 * <p>Composable for managing project information</p>
 * <p>Provides access to project data injected from parent components</p>
 *
 * @returns Object containing project information and computed values
 */
export function useProjectInfo () {
  /**
   * <p>Injected project information from parent component</p>
   * <p>Contains project details such as ID, name, and description</p>
   */
  const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({ id: '' }));

  /**
   * <p>Computed project identifier</p>
   * <p>Provides reactive access to the current project ID</p>
   */
  const projectId = computed(() => projectInfo.value?.id || '');

  /**
   * <p>Computed project name</p>
   * <p>Provides reactive access to the current project name</p>
   */
  const projectName = computed(() => projectInfo.value?.name || '');

  /**
   * <p>Computed project description</p>
   * <p>Provides reactive access to the current project description</p>
   */
  const projectDescription = computed(() => projectInfo.value?.description || '');

  /**
   * <p>Checks if project information is available</p>
   * <p>Returns true if project ID exists, false otherwise</p>
   */
  const hasProjectInfo = computed(() => Boolean(projectId.value));

  return {
    // State
    projectInfo,

    // Computed values
    projectId,
    projectName,
    projectDescription,
    hasProjectInfo
  };
}
