import { computed, ref, watch, type Ref } from 'vue';
import type { CreatorObjectType, ProjectInfo } from '../types';

/**
 * Manage filter state for Kanban views.
 * <p>
 * Exposes reactive refs for project/user filters and derived safe props that
 * comply with child component prop typing (strings instead of undefined).
 * </p>
 */
export function useKanbanFilters (options: {
  projectInfo: Ref<ProjectInfo | undefined>;
}) {
  // project base
  const projectId = ref<string | undefined>();
  const avatar = ref<string | undefined>();

  // filters
  const dateRange = ref<[string, string] | undefined>();
  const creatorObjectType = ref<CreatorObjectType | undefined>();
  const creatorObjectId = ref<string | undefined>();
  const creatorObjectName = ref<string | undefined>();

  // sprint/plan filters
  const countType = ref<'task' | 'useCase'>('task');
  const selectedSprintId = ref<string | undefined>();
  const selectedPlanId = ref<string | undefined>();

  /**
   * Side-effect: sync project level info when context changes
   */
  watch(
    () => options.projectInfo.value,
    (newValue) => {
      if (newValue?.id) {
        projectId.value = newValue.id;
        avatar.value = newValue.avatar;
        selectedSprintId.value = undefined;
        selectedPlanId.value = undefined;
      }
    },
    { immediate: true }
  );

  // derived safe props for child components which expect required string props
  const projectIdSafe = computed(() => projectId.value ?? '');
  const sprintIdSafe = computed(() => selectedSprintId.value ?? '');
  const planIdSafe = computed(() => selectedPlanId.value ?? '');
  const createdDateStartSafe = computed(() => (dateRange.value?.[0] ?? ''));
  const createdDateEndSafe = computed(() => (dateRange.value?.[1] ?? ''));
  const creatorObjectTypeSafe = computed<CreatorObjectType | ''>(() => creatorObjectType.value ?? '');
  const creatorObjectIdSafe = computed(() => creatorObjectId.value ?? '');

  function clearCreator () {
    creatorObjectType.value = undefined;
    creatorObjectId.value = undefined;
    creatorObjectName.value = undefined;
  }

  function setCreator (creator: { creatorObjectType?: CreatorObjectType; creatorObjectId?: string; creatorObjectName?: string; avatar?: string }) {
    creatorObjectType.value = creator.creatorObjectType;
    creatorObjectId.value = creator.creatorObjectId;
    creatorObjectName.value = creator.creatorObjectName;
    if (creator.avatar) {
      avatar.value = creator.avatar;
    }
  }

  return {
    // state
    projectId,
    avatar,
    dateRange,
    creatorObjectType,
    creatorObjectId,
    creatorObjectName,
    countType,
    selectedSprintId,
    selectedPlanId,

    // derived safe props for children
    projectIdSafe,
    sprintIdSafe,
    planIdSafe,
    createdDateStartSafe,
    createdDateEndSafe,
    creatorObjectTypeSafe,
    creatorObjectIdSafe,

    // actions
    clearCreator,
    setCreator
  } as const;
}
