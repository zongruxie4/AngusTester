import { ref } from 'vue';

import { UseProjectAvatarReturn } from '@/views/project/project/types';

/**
 * Project avatar management composable
 * Handles avatar upload, cropping, and deletion functionality
 */
export function useAvatar (
  projectDetail: any // Should be Ref<Project> but avoiding circular reference for now
): UseProjectAvatarReturn {
  // ============================================================================
  // State Management
  // ============================================================================

  /** Controls cropper modal visibility */
  const uploadAvatarVisible = ref(false);

  // ============================================================================
  // Avatar Actions
  // ============================================================================

  /**
   * Open avatar cropper modal
   */
  const openCropper = (): void => {
    uploadAvatarVisible.value = true;
  };

  /**
   * Close avatar cropper modal
   */
  const closeCropper = (): void => {
    uploadAvatarVisible.value = false;
  };

  /**
   * Handle successful avatar upload
   * Updates project detail with new avatar URL
   *
   * @param response - Upload response from server
   */
  const uploadImgSuccess = (response: any): void => {
    try {
      // Extract avatar URL from response
      const avatarUrl = response?.data?.[0]?.url;

      if (avatarUrl && projectDetail.value) {
        projectDetail.value.avatar = avatarUrl;

        // Close cropper modal on successful upload
        closeCropper();
      } else {
        console.warn('Avatar upload succeeded but no URL received:', response);
      }
    } catch (error) {
      console.error('Error processing avatar upload response:', error);
    }
  };

  /**
   * Delete current avatar
   * Removes avatar from project detail
   */
  const deleteImage = (): void => {
    if (projectDetail.value) {
      projectDetail.value.avatar = '';
    }
  };

  /**
   * Handle avatar upload error
   * @param error - Upload error information
   */
  const handleUploadError = (error: any): void => {
    console.error('Avatar upload failed:', error);
    closeCropper();
  };

  /**
   * Validate image file before upload
   * @param file - File to validate
   * @returns True if file is valid, false otherwise
   */
  const validateImageFile = (file: File): boolean => {
    // Check file type
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      console.error('Invalid file type:', file.type);
      return false;
    }

    // Check file size (max 5MB)
    const maxSize = 5 * 1024 * 1024; // 5MB in bytes
    if (file.size > maxSize) {
      console.error('File too large:', file.size);
      return false;
    }

    return true;
  };

  /**
   * Get current avatar URL
   * @returns Current avatar URL or empty string
   */
  const getCurrentAvatar = (): string => {
    return projectDetail.value?.avatar || '';
  };

  /**
   * Check if avatar is set
   * @returns True if avatar exists, false otherwise
   */
  const hasAvatar = (): boolean => {
    return Boolean(getCurrentAvatar());
  };

  /**
   * Reset avatar state
   */
  const resetAvatar = (): void => {
    deleteImage();
    closeCropper();
  };

  // ============================================================================
  // Return Interface
  // ============================================================================

  return {
    // State
    uploadAvatarVisible,

    // Methods
    openCropper,
    closeCropper,
    uploadImgSuccess,
    deleteImage,
    handleUploadError,
    validateImageFile,
    getCurrentAvatar,
    hasAvatar,
    resetAvatar
  };
}
