import { ref, nextTick } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { space } from '@/api/storage';
import { SourceType } from '../types';

/**
 * <p>Composable for managing file and directory rename operations.</p>
 * <p>Handles rename state, validation, and API calls for renaming files.</p>
 *
 * @returns Object containing rename state and methods
 */
export function useFileRename () {
  const { t } = useI18n();

  // Reference to create input element
  const createInputRef = ref<any>(null);

  /**
   * <p>Enable rename mode for a file or directory.</p>
   * <p>Sets rename flag and focuses the input field for editing.</p>
   *
   * @param record - File or directory record to rename
   */
  const rename = (record: SourceType) => {
    record.renameFlag = true;
    record.cacheName = record.name;

    nextTick(() => {
      const dom = document.getElementById(record.id + '') as HTMLInputElement;
      if (dom) {
        dom.select();
      }
    });
  };

  /**
   * <p>Handle rename input blur event.</p>
   * <p>Validates input and saves rename if valid, otherwise restores original name.</p>
   *
   * @param record - File or directory record being renamed
   */
  const renameBlur = async (record: SourceType) => {
    const { id, name, cacheName } = record;

    // If name is unchanged or empty, restore original
    if (name === cacheName || !name) {
      if (cacheName) {
        record.name = cacheName;
      }
      record.renameFlag = false;
      return;
    }

    // Save rename to API
    const [error] = await space.renameFile({ id, name });
    if (error) {
      return;
    }

    notification.success(t('fileSpace.fileManagement.messages.renameSuccess'));
    record.renameFlag = false;
  };

  /**
   * <p>Handle rename input enter key event.</p>
   * <p>Triggers blur event to save rename.</p>
   *
   * @param e - Keyboard event
   */
  const renameEnter = (e: KeyboardEvent) => {
    (e.target as HTMLInputElement).blur();
  };

  /**
   * <p>Create new directory with input field.</p>
   * <p>Adds temporary input row to data source and focuses input.</p>
   *
   * @param dataSource - Current data source array
   * @param addTempRow - Function to add temporary row to data source
   */
  const create = (dataSource: SourceType[], addTempRow: () => void) => {
    if (dataSource[0]?.id !== '-1') {
      addTempRow();

      nextTick(() => {
        if (createInputRef.value) {
          const el = createInputRef.value.$el.querySelector('input');
          if (el) {
            el.focus();
          }
        }
      });
    }
  };

  /**
   * <p>Handle directory creation input blur event.</p>
   * <p>Validates input and creates directory if valid, otherwise removes temporary row.</p>
   *
   * @param record - Temporary directory record
   * @param dataSource - Current data source array
   * @param removeTempRow - Function to remove temporary row
   * @param createDirectory - Function to create actual directory
   */
  const createBlur = (
    record: SourceType,
    dataSource: SourceType[],
    removeTempRow: () => void,
    createDirectory: (record: SourceType) => Promise<void>
  ) => {
    if (!record.name) {
      removeTempRow();
      return;
    }
    createDirectory(record);
  };

  /**
   * <p>Handle directory creation input enter key event.</p>
   * <p>Triggers blur event to create directory.</p>
   *
   * @param e - Keyboard event
   */
  const createEnter = (e: KeyboardEvent) => {
    (e.target as HTMLInputElement).blur();
  };

  return {
    // State
    createInputRef,

    // Methods
    rename,
    renameBlur,
    renameEnter,
    create,
    createBlur,
    createEnter
  };
}
