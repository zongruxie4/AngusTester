import { ref } from 'vue';
import postmanToOpenApi from '@xcan-angus/postman-to-openapi';

/**
 * Composable for managing file upload functionality
 * @returns File upload state and handler functions
 */
export function useFileUpload () {
  // File upload state
  const sizeErr = ref(false);
  const fileErr = ref(false);
  const versionRuleErr = ref(false);

  /**
   * Handle file drop event
   * @param event - Drag event
   */
  const handleFileDrop = (event: DragEvent): void => {
    event.preventDefault();
    const files = event.dataTransfer?.files;
    if (files && files[0]) {
      customRequest(files[0]);
    }
  };

  /**
   * Prevent default drag over behavior
   * @param event - Drag event
   */
  const preventDefault = (event: DragEvent): void => {
    event.preventDefault();
  };

  /**
   * Handle paste event for text content
   * @param event - Clipboard event
   * @returns Pasted text content or undefined if validation fails
   */
  const handlePaste = (event: ClipboardEvent): string | undefined => {
    const text = event.clipboardData?.getData('text') || '';
    const encoder = new TextEncoder();
    const encodedString = encoder.encode(text);
    const byteSize = encodedString.byteLength;
    const maxSizeInMB = 20;
    const maxSizeInBytes = maxSizeInMB * 1024 * 1024;

    if (byteSize > maxSizeInBytes) {
      sizeErr.value = true;
      fileErr.value = false;
      return undefined;
    } else {
      // Return the text for the parent component to handle
      return text;
    }
  };

  /**
   * Custom file request handler
   * Validates file size and handles Postman to OpenAPI conversion
   * @param file - File to process
   * @returns Processed file or null if validation fails
   */
  const customRequest = (file: File): File | null => {
    // Validate file size (20MB limit)
    if (file.size > 20 * 1024 * 1024) {
      sizeErr.value = true;
      fileErr.value = false;
      return null;
    } else {
      sizeErr.value = false;
      fileErr.value = false;

      // Handle JSON files (convert Postman to OpenAPI)
      if (file.type === 'application/json') {
        toOpenapi(file);
        return null; // Will be handled by toOpenapi
      }

      return file;
    }
  };

  /**
   * Convert Postman collection to OpenAPI format
   * @param file - Postman collection file
   * @returns Converted OpenAPI file or null if conversion fails
   */
  const toOpenapi = (file: File): File | null => {
    versionRuleErr.value = false;
    const reader = new FileReader();

    reader.onload = () => {
      try {
        const fileContent = reader.result as string;
        const openapiDoc = postmanToOpenApi(fileContent);
        const yamlFileName = file.name.replace(/\.json$/, '') + '.yml';
        return new File([openapiDoc], yamlFileName, { type: 'text/yaml' });
      } catch (error) {
        versionRuleErr.value = true;
        sizeErr.value = false;
        fileErr.value = false;
        return null;
      }
    };

    reader.readAsText(file);
    return null; // Async operation, actual result will be handled in onload
  };

  /**
   * Delete uploaded file
   */
  const deleteFile = (): void => {
    fileErr.value = true;
  };

  return {
    sizeErr,
    fileErr,
    versionRuleErr,
    handleFileDrop,
    preventDefault,
    handlePaste,
    customRequest,
    deleteFile
  };
}
