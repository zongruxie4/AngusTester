import { ref, reactive, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { upload, cookieUtils } from '@xcan-angus/infra';
import { formatBytes } from '@/utils/common';
import type { UploadFileStatus } from '../../components/type';

interface Props {
  parentDirectoryId: string;
}

/**
 * Composable for managing file upload operations
 * Handles file selection, upload progress tracking, and upload status management
 *
 * @param props - Component props containing parent directory ID
 * @returns Object containing reactive state and methods for file upload management
 */
export function useUpload (props: Props, emit: any) {
  const route = useRoute();

  // Space ID from route parameters
  const spaceId = ref<string>('');

  // Reactive state for file uploads
  const state = reactive<{
    fileList: {
      [key: string]: UploadFileStatus
    };
    fileId: string;
  }>({
    fileList: {},
    fileId: '-1'
  });

  // Authorization headers for upload requests
  const headers = computed(() => ({
    authorization: `Bearer ${cookieUtils.getTokenInfo().access_token}`
  }));

  // Reference to the upload input element
  const uploadInput = ref<HTMLInputElement | null>(null);

  /**
   * Trigger file upload by clicking the hidden input element
   */
  const triggerUpload = (): void => {
    uploadInput.value?.click();
  };

  /**
   * Close the upload component
   */
  const closeUpload = (): void => {
    // This will be implemented by the component
  };

  /**
   * Handle file upload process
   *
   * @param fileObj - Object containing the file to upload
   */
  const handleUpload = async (fileObj: { file: any }): Promise<void> => {
    const { file } = fileObj;

    // Prepare upload options
    const options = {
      bizKey: 'angusTesterDataFiles',
      parentDirectoryId: +props.parentDirectoryId > -1 ? props.parentDirectoryId : undefined,
      spaceId: spaceId.value
    };

    // Format file size for display
    const size = formatBytes(file.size);

    // Initialize file status in the list
    const fileId = file.uid || file.name + Date.now();
    state.fileList[fileId] = {
      name: file.name,
      size,
      progress: 0,
      status: 0 // 0 = uploading, 1 = failed, 2 = completed
    };

    // Configure upload progress tracking
    const config = {
      onUploadProgress: (progressEvent: { loaded: number; total: number }) => {
        const { loaded, total } = progressEvent;
        const progress = Math.round((loaded * 100) / total);
        state.fileList[fileId].progress = progress;
      }
    };

    // Execute the upload
    const [error] = await upload(file, options, config);

    // Update file status based on result
    if (error) {
      state.fileList[fileId].status = 1; // Failed
      return;
    }

    state.fileList[fileId].status = 2; // Completed

    emit('success');
  };

  /**
   * Computed property for the number of files being uploaded
   */
  const uploadFileLength = computed(() => {
    return Object.keys(state.fileList).length;
  });

  /**
   * Initialize composable - watch for route changes to update space ID
   */
  const init = (): void => {
    watch(() => route.params.id, (val) => {
      if (val) {
        spaceId.value = val as string;
      }
    }, {
      immediate: true
    });
  };

  return {
    // State
    state,
    headers,
    uploadInput,

    // Computed
    uploadFileLength,

    // Methods
    triggerUpload,
    closeUpload,
    handleUpload,
    init
  };
}
