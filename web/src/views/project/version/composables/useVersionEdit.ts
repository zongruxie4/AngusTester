/**
 * Version edit composable
 * Manages version creation and editing functionality
 */

import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { software } from '@/api/tester';
import type { VersionEditProps, VersionFormState } from '../types';

export function useVersionEdit(props: VersionEditProps) {
  const { t } = useI18n();

  // Reactive state
  const formState = ref<VersionFormState>({
    name: undefined,
    startDate: undefined,
    releaseDate: undefined,
    description: undefined
  });

  const loading = ref(false);
  const formRef = ref();

  // Emit definitions
  const emits = {
    cancel: () => {},
    ok: () => {},
    updateVisible: (value: boolean) => {}
  };

  /**
   * Load version data for editing
   * Fetches existing version data when editing mode is active
   * @param id - Version ID to load
   */
  const loadData = async (id: string): Promise<void> => {
    if (loading.value) {
      return;
    }

    loading.value = true;
    const [error, res] = await software.getSoftwareVersionDetail(id);
    loading.value = false;

    if (error) {
      return;
    }

    const data = res?.data || {};
    if (!data) {
      return;
    }

    const { name, startDate, releaseDate, description } = data;
    formState.value = {
      name,
      startDate,
      releaseDate,
      description
    };
  };

  /**
   * Handle form cancellation
   * Closes modal and emits cancel event
   */
  const cancel = (): void => {
    emits.updateVisible(false);
    emits.cancel();
  };

  /**
   * Handle form submission
   * Validates form and calls appropriate save method
   */
  const ok = async (): Promise<void> => {
    formRef.value.validate().then(async () => {
      if (!props.versionId) {
        await addVersion();
      } else {
        await editVersion();
      }
    });
  };

  /**
   * Create new version
   * Submits new version data to API
   */
  const addVersion = async (): Promise<void> => {
    loading.value = true;
    const [error] = await software.addSoftwareVersion({
      ...formState.value,
      projectId: props.projectId
    });
    loading.value = false;

    if (error) {
      return;
    }

    emits.ok();
    emits.updateVisible(false);
  };

  /**
   * Update existing version
   * Submits updated version data to API
   */
  const editVersion = async (): Promise<void> => {
    loading.value = true;
    const [error] = await software.updateSoftwareVersion({
      ...formState.value,
      id: props.versionId
    });
    loading.value = false;

    if (error) {
      return;
    }

    emits.ok();
    emits.updateVisible(false);
  };

  /**
   * Reset form state for new version creation
   * Clears all form fields
   */
  const resetFormState = (): void => {
    formState.value = {
      name: undefined,
      startDate: undefined,
      releaseDate: undefined,
      description: undefined
    };
  };

  /**
   * Watch for modal visibility changes
   * Loads data when editing or resets form when creating
   */
  onMounted(() => {
    watch(() => props.visible, async (newValue) => {
      if (newValue) {
        if (props.versionId) {
          await loadData(props.versionId);
        } else {
          resetFormState();
        }
      }
    }, { immediate: true });
  });

  return {
    formState,
    loading,
    formRef,
    cancel,
    ok
  };
}
