import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { tag } from '@/api/tester';
import { modal, notification } from '@xcan-angus/vue-ui';
import type { TagItem, CreateTagParams, UpdateTagParams } from '../types';

/**
 * Composable for managing tag actions and operations
 * Handles CRUD operations, modal states, and user interactions
 *
 * @param onDataChange - Callback function to refresh data after operations
 * @param onTagAdded - Callback function when a tag is added
 * @param onTagRemoved - Callback function when a tag is removed
 * @param onTagUpdated - Callback function when a tag is updated
 * @returns Object containing action methods and reactive state
 */
export function useActions (
  onDataChange: () => Promise<void>,
  onTagAdded: (newTag: { id: string; name: string }) => void,
  onTagRemoved: (index: number) => Promise<void>,
  onTagUpdated: (index: number, updatedData: { id: string; name: string; hasEditPermission?: boolean }) => void
) {
  const { t } = useI18n();

  // Modal and state management
  const modalVisible = ref(false);

  /**
   * Creates a new tag with the specified parameters
   * Shows success notification and updates local data
   *
   * @param params - Parameters for creating the tag
   * @returns Promise resolving to the created tag ID or undefined on error
   */
  const createTag = async (params: CreateTagParams): Promise<string | undefined> => {
    try {
      const [error, response] = await tag.addTag(params);

      if (error) {
        console.error('Failed to create tag:', error);
        return undefined;
      }

      // Extract the created tag ID from response
      const createdId = response?.data?.[0]?.id;

      // Show success notification
      notification.success(t('actions.tips.addSuccess'));

      // Update local data with the new tag
      if (createdId && params.names.length > 0) {
        onTagAdded({
          id: createdId,
          name: params.names[0]
        });
      }

      return createdId;
    } catch (error) {
      console.error('Unexpected error during tag creation:', error);
      return undefined;
    }
  };

  /**
   * Updates an existing tag with new information
   * Shows success notification and updates local data
   *
   * @param tagData - Array of tag updates to apply
   * @param index - Index of the tag in the data list
   * @returns Promise resolving to success status
   */
  const updateTag = async (tagData: UpdateTagParams[], index: number): Promise<boolean> => {
    try {
      const [error] = await tag.updateTag(tagData);

      if (error) {
        console.error('Failed to update tag:', error);
        return false;
      }

      // Show success notification
      notification.success(t('actions.tips.updateSuccess'));

      // Update local data with the new tag information
      if (tagData.length > 0) {
        onTagUpdated(index, {
          id: tagData[0].id,
          name: tagData[0].name
        });
      }

      return true;
    } catch (error) {
      console.error('Unexpected error during tag update:', error);
      return false;
    }
  };

  /**
   * Deletes a tag with confirmation dialog
   * Shows confirmation before deletion and handles the delete operation
   *
   * @param tagItem - The tag to delete
   * @param index - Index of the tag in the data list
   */
  const deleteTag = (tagItem: TagItem, index: number): void => {
    modal.confirm({
      content: t('actions.tips.confirmDelete', { name: tagItem.name }),
      async onOk () {
        try {
          const [error] = await tag.deleteTag([tagItem.id]);

          if (error) {
            console.error('Failed to delete tag:', error);
            return;
          }

          // Show success notification
          notification.success(t('actions.tips.deleteSuccess'));

          // Update local data by removing the tag
          await onTagRemoved(index);
        } catch (error) {
          console.error('Unexpected error during tag deletion:', error);
        }
      }
    });
  };

  /**
   * Opens the create tag modal
   * Sets up the modal for creating a new tag
   */
  const openCreateModal = (): void => {
    modalVisible.value = true;
  };

  /**
   * Closes the create tag modal
   * Resets modal visibility state
   */
  const closeCreateModal = (): void => {
    modalVisible.value = false;
  };

  /**
   * Handles successful tag creation from modal
   * Updates local data and closes modal
   *
   * @param newTagData - Data of the newly created tag
   */
  const handleCreateSuccess = (newTagData: { id: string; name: string }): void => {
    onTagAdded(newTagData);
    closeCreateModal();
  };

  /**
   * Validates tag name before operations
   * Checks if the name is not empty and meets requirements
   *
   * @param name - Tag name to validate
   * @returns Boolean indicating if the name is valid
   */
  const validateTagName = (name: string): boolean => {
    const trimmedName = name?.trim();
    if (!trimmedName) {
      return false;
    }

    // Additional validation can be added here
    // For example: length limits, special characters, etc.
    return trimmedName.length <= 50;
  };

  /**
   * Checks if a tag name already exists in the current list
   * Prevents duplicate tag names
   *
   * @param name - Tag name to check
   * @param tagList - Current list of tags
   * @param excludeId - ID to exclude from the check (for updates)
   * @returns Boolean indicating if the name already exists
   */
  const checkDuplicateName = (
    name: string,
    tagList: TagItem[],
    excludeId?: string
  ): boolean => {
    const trimmedName = name.trim().toLowerCase();
    return tagList.some(tag =>
      tag.id !== excludeId &&
      tag.name.toLowerCase() === trimmedName
    );
  };

  /**
   * Bulk creates multiple tags
   * Creates multiple tags in a single operation
   *
   * @param tagNames - Array of tag names to create
   * @param projectId - Project ID to create tags in
   * @returns Promise resolving to array of created tag IDs
   */
  const bulkCreateTags = async (
    tagNames: string[],
    projectId: string
  ): Promise<string[]> => {
    try {
      const validNames = tagNames
        .map(name => name.trim())
        .filter(name => validateTagName(name));

      if (validNames.length === 0) {
        return [];
      }

      const params: CreateTagParams = {
        names: validNames,
        projectId
      };

      const [error, response] = await tag.addTag(params);

      if (error) {
        console.error('Failed to bulk create tags:', error);
        return [];
      }

      // Extract created tag IDs
      const createdIds = response?.data?.map(item => item.id) || [];

      // Show success notification
      notification.success(
        t('tag.bulkCreateSuccess', { count: createdIds.length })
      );

      // Update local data for each created tag
      validNames.forEach((name, index) => {
        if (createdIds[index]) {
          onTagAdded({
            id: createdIds[index],
            name
          });
        }
      });

      return createdIds;
    } catch (error) {
      console.error('Unexpected error during bulk tag creation:', error);
      return [];
    }
  };

  /**
   * Bulk deletes multiple tags
   * Deletes multiple tags in a single operation
   *
   * @param tagIds - Array of tag IDs to delete
   * @param indices - Array of indices corresponding to the tags
   * @returns Promise resolving to success status
   */
  const bulkDeleteTags = async (
    tagIds: string[],
    indices: number[]
  ): Promise<boolean> => {
    try {
      const [error] = await tag.deleteTag(tagIds);

      if (error) {
        console.error('Failed to bulk delete tags:', error);
        return false;
      }

      // Show success notification
      notification.success(
        t('tag.bulkDeleteSuccess', { count: tagIds.length })
      );

      // Update local data by removing tags (in reverse order to maintain indices)
      const sortedIndices = [...indices].sort((a, b) => b - a);
      for (const index of sortedIndices) {
        await onTagRemoved(index);
      }

      return true;
    } catch (error) {
      console.error('Unexpected error during bulk tag deletion:', error);
      return false;
    }
  };

  return {
    // Reactive state
    modalVisible,

    // Action methods
    createTag,
    updateTag,
    deleteTag,
    openCreateModal,
    closeCreateModal,
    handleCreateSuccess,
    validateTagName,
    checkDuplicateName,
    bulkCreateTags,
    bulkDeleteTags
  };
}
