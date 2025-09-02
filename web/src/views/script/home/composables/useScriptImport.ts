import { ref, computed } from 'vue';
import { script } from '@/api/tester';
import { notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { formatBytes } from '@/utils/common';

/**
 * Composable for managing script import functionality
 * @param projectId - The ID of the current project
 */
export function useScriptImport(projectId: string) {
  const { t } = useI18n();
  
  // Form state
  const formState = ref<{
    name: string;
    content?: string;
    file?: File | undefined;
    description?: string;
  }>({
    name: '',
    content: undefined,
    file: undefined,
    description: ''
  });
  
  // Validation flags
  const emptyFlag = ref(false);
  const maximumLimitFlag = ref(false);
  const loading = ref(false);
  
  // Form reference
  const formRef = ref();
  
  /**
   * Handle cancel action
   */
  const handleCancel = () => {
    emptyFlag.value = false;
    maximumLimitFlag.value = false;
    formState.value = {
      name: '',
      content: undefined,
      file: undefined,
      description: ''
    };
    if (formRef.value) {
      formRef.value.clearValidate();
    }
  };
  
  /**
   * Handle file drop upload
   */
  const handleDropUpload = (event: DragEvent) => {
    event.preventDefault();
    const file = event.dataTransfer?.files[0];
    if (file) {
      customRequest({ file });
    }
  };
  
  /**
   * Custom file upload request
   */
  const customRequest = ({ file }: { file: File }) => {
    if (file.size > 20 * 1024 * 1024) {
      maximumLimitFlag.value = true;
      emptyFlag.value = false;
      return;
    }

    formState.value.file = file;
    formState.value.content = undefined;
    maximumLimitFlag.value = false;
    emptyFlag.value = false;
  };
  
  /**
   * Handle drag over event
   */
  const handleDragover = (event: DragEvent) => {
    event.preventDefault();
  };
  
  /**
   * Handle paste event
   */
  const handlePaste = (event: ClipboardEvent) => {
    const _text = event.clipboardData?.getData('text') || '';
    const encoder = new TextEncoder();
    const encodedString = encoder.encode(_text);
    const byteSize = encodedString.byteLength;
    const maxSizeInMB = 20;
    const maxSizeInBytes = maxSizeInMB * 1024 * 1024;

    if (byteSize > maxSizeInBytes) {
      maximumLimitFlag.value = true;
      emptyFlag.value = false;
    } else {
      formState.value.content = _text;
      formState.value.file = undefined;
      emptyFlag.value = false;
      maximumLimitFlag.value = false;
    }
  };
  
  /**
   * Clear content
   */
  const clearContent = () => {
    formState.value.content = '';
    emptyFlag.value = true;
  };
  
  /**
   * Delete file
   */
  const deleteFile = () => {
    formState.value.file = undefined;
    emptyFlag.value = true;
  };
  
  /**
   * Check if file is provided
   */
  const checkFile = () => {
    emptyFlag.value = !formState.value.file && !formState.value.content;
  };
  
  /**
   * Handle import submission
   */
  const handleImport = async (callback: () => void) => {
    if (!formRef.value) return;
    
    try {
      await formRef.value.validate();
      checkFile();
      
      if (emptyFlag.value || maximumLimitFlag.value) {
        return;
      }

      const formData = new FormData();
      formData.append('name', formState.value.name);
      formData.append('projectId', projectId);

      if (formState.value.file) {
        formData.append('file', formState.value.file);
      }

      if (formState.value.content) {
        formData.append('content', formState.value.content.toString());
      }

      if (formState.value.description) {
        formData.append('description', formState.value.description);
      }

      loading.value = true;
      const [error] = await script.importScript(formData);
      loading.value = false;
      
      if (error) {
        return;
      }

      notification.success(t('tips.importSuccess'));
      handleCancel();
      callback();
    } catch (error) {
      checkFile();
    }
  };
  
  // Computed properties
  const fileEmptyFlag = computed(() => {
    return !formState.value.file && !formState.value.content;
  });

  const errorFlag = computed(() => {
    return emptyFlag.value || maximumLimitFlag.value;
  });
  
  const rules = {
    name: [
      { required: true, message: t('scriptHome.import.messages.nameRequired'), trigger: 'change' }
    ]
  };
  
  return {
    // Reactive data
    formState,
    emptyFlag,
    maximumLimitFlag,
    loading,
    formRef,
    
    // Methods
    handleCancel,
    handleDropUpload,
    customRequest,
    handleDragover,
    handlePaste,
    clearContent,
    deleteFile,
    checkFile,
    handleImport,
    
    // Computed properties
    fileEmptyFlag,
    errorFlag,
    rules
  };
}