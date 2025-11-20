import { ref } from 'vue';

/**
 * Manage Kanban view mode, fullscreen toggle and child refs.
 * <p>
 * Provides helpers to refresh and resize active tab content.
 * </p>
 */
export function useKanbanView () {
  const isMaximized = ref(false);
  const viewMode = ref<'data' | 'effectiveness' | 'evaluation' | 'cto'>('effectiveness');

  // child component refs
  const dataViewRef = ref<any>();
  const effectivenessRef = ref<any>();
  const evaluationRef = ref<any>();
  const ctoRef = ref<any>();

  function refreshActive () {
    if (typeof dataViewRef.value?.refresh === 'function' && viewMode.value === 'data') {
      dataViewRef.value.refresh();
    }
    if (typeof effectivenessRef.value?.refresh === 'function' && viewMode.value === 'effectiveness') {
      effectivenessRef.value.refresh();
    }
    if (typeof evaluationRef.value?.refresh === 'function' && viewMode.value === 'evaluation') {
      evaluationRef.value.refresh();
    }
    if (typeof ctoRef.value?.refresh === 'function' && viewMode.value === 'cto') {
      ctoRef.value.refresh();
    }
  }

  function toggleMaximize () {
    isMaximized.value = !isMaximized.value;
    if (typeof dataViewRef.value?.handleWindowResize === 'function' && viewMode.value === 'data') {
      dataViewRef.value.handleWindowResize();
    }
    if (typeof effectivenessRef.value?.handleWindowResize === 'function' && viewMode.value === 'effectiveness') {
      effectivenessRef.value.handleWindowResize();
    }
    if (typeof evaluationRef.value?.handleWindowResize === 'function' && viewMode.value === 'evaluation') {
      evaluationRef.value.handleWindowResize();
    }
    if (typeof ctoRef.value?.handleWindowResize === 'function' && viewMode.value === 'cto') {
      ctoRef.value.handleWindowResize();
    }
  }

  return {
    isMaximized,
    viewMode,
    dataViewRef,
    effectivenessRef,
    evaluationRef,
    ctoRef,
    refreshActive,
    toggleMaximize
  } as const;
}
