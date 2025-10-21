<script setup lang="ts">
import { computed, ref, watch, onMounted, nextTick } from 'vue';
// @ts-ignore - FormatHighlight component exists but type definition may be missing
import { IconCopy, IconDownload, FormatHighlight } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

/**
 * Component props interface
 */
interface Props {
  dataSource: any;  // Response data source with headers, data, and config
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({ data: null })
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Emitted events interface
 */
const emit = defineEmits<{
  (e: 'rendered', value: true): void;  // Emitted when component is fully rendered
}>();

/**
 * Ref: Current active tab/view mode
 * Controls which view format is displayed (pretty/raw/preview)
 */
const currentTabId = ref<'pretty' | 'raw' | 'preview'>('pretty');

/**
 * Handle tab selection
 * Updates the active view mode
 * 
 * @param id - Tab ID to activate
 */
const handleSelect = (id: 'pretty' | 'raw' | 'preview'): void => {
  currentTabId.value = id;
};

/**
 * Get download filename with appropriate extension
 * Falls back to default filename based on content type
 * 
 * Priority:
 * 1. Filename from Content-Disposition header
 * 2. Default filename based on content type
 * 
 * @returns Filename with extension
 */
const getDownloadFilename = (): string => {
  if (downloadFilename.value) {
    return downloadFilename.value;
  }

  if (contentType.value === 'plain') {
    return 'response.txt';
  }

  if (contentType.value === 'javascript') {
    return 'response.js';
  }

  return 'response.' + contentType.value;
};

/**
 * Download response content as file
 * Creates a temporary download link and triggers download
 * Handles both Blob and text/object data
 * 
 * Process:
 * 1. Convert data to Blob if needed
 * 2. Create Object URL from Blob
 * 3. Create temporary anchor element
 * 4. Trigger download
 * 5. Clean up resources
 */
const downloadFile = (): void => {
  const data = props.dataSource?.data;
  if (!data) {
    return;
  }

  let blob = data;
  
  // Convert non-Blob data to Blob
  if (!(data instanceof Blob)) {
    if (typeof blob === 'object') {
      // Format JSON objects with indentation
      blob = JSON.stringify(blob, null, 2);
    }

    // Create Blob with generic binary type
    blob = new Blob([blob], {
      type: 'application/octet-stream'
    });
  }

  // Create temporary URL for the Blob
  const url = URL.createObjectURL(blob);

  // Create and trigger download link
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = getDownloadFilename();
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);

  // Clean up the Object URL to free memory
  window.URL.revokeObjectURL(url);
};

/**
 * Computed: Content type from response headers
 * Extracts and normalizes the content type from Content-Type header
 * Supports case-insensitive header names
 * 
 * Extraction process:
 * 1. Get Content-Type header (case-insensitive)
 * 2. Remove whitespace
 * 3. Extract subtype from MIME type (e.g., "application/json" -> "json")
 * 4. Return recognized types (json/xml/html) or full type
 * 
 * @returns Content type string (json, xml, html, plain, or full MIME type)
 */
const contentType = computed(() => {
  const type = (props.dataSource?.headers?.['Content-Type'] || props.dataSource?.headers?.['content-type'])?.replace(/\s+/gi, '');
  if (!type) {
    return 'plain';
  }

  // Extract subtype from MIME type (e.g., "application/json" -> "json")
  const temp = type.replace(/\S+\/(\S[^;]+)\s*\S*/, '$1').replace(/;/, '');
  return ['json', 'xml', 'html'].includes(temp) ? temp : type;
});

/**
 * Computed: Download filename from headers or URL
 * Extracts filename from Content-Disposition header or URL path
 * 
 * Extraction priority:
 * 1. Content-Disposition header (filename parameter)
 * 2. URL path (for binary file types)
 * 3. Empty string (no filename available)
 * 
 * Binary file types supported:
 * - Images (image/*)
 * - Audio (audio/*)
 * - Video (video/*)
 * - Archives (zip, rar, 7z, tar, gzip, bzip2, xz)
 * - Documents (pdf, MS Office formats)
 * 
 * @returns Filename string or empty if not available
 */
const downloadFilename = computed(() => {
  const disposition = (props.dataSource?.headers['Content-Disposition'] || props.dataSource?.headers['content-disposition']);
  
  if (!disposition) {
    const contentType = props.dataSource?.headers?.['Content-Type'] || props.dataSource?.headers?.['content-type'] || '';
    
    // Check if content type indicates a binary/downloadable file
    if ([
      'image',
      'audio',
      'video',
      // Archive formats
      'zip',
      'x-rar-compressed',
      'x-7z-compressed',
      'x-tar',
      'gzip',
      'x-bzip2',
      'x-xz',
      // Document formats
      'pdf',
      'vnd.ms-',        // MS Office formats
      'msword',
      'vnd.openxmlformats'
    ].some(i => contentType.includes(i))) {
      if (props.dataSource.config.url) {
        try {
          // Extract filename from URL path
          const _url = new URL(props.dataSource.config.url);
          const paths = _url.pathname.split('/');
          return paths[paths.length - 1] || '';
        } catch (error) {
          return 'file';
        }
      }
    }
    return '';
  }

  // Parse filename from Content-Disposition header
  return disposition.replace(/"/gi, '').replace(/[\s\S]*filename=(\S+.\S+)[\s\S]*/i, '$1').replace(/(\S\s+);\S*/, '$1').replace(/\s$/, '');
});

/**
 * Convert Blob to text string
 * Reads Blob content as UTF-8 text using FileReader
 * 
 * @param blob - Blob object to convert
 * @returns Promise that resolves to text content or original Blob on error
 */
const convertBlob = (blob: Blob): Promise<string | Blob> => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    
    reader.onload = () => {
      resolve(reader.result as string);
    };

    reader.onerror = () => {
      resolve(blob);
    };

    reader.readAsText(blob, 'utf-8');
  });
};

/**
 * Ref: Processed data to display
 * Contains the converted/formatted response data ready for display
 */
const showData = ref<any>('');

/**
 * Component mounted lifecycle hook
 * Sets up data processing and emits rendered event
 */
onMounted(() => {
  /**
   * Watch: Process response data when dataSource changes
   * Handles different data types and encodings
   * 
   * Processing logic:
   * 1. Blob with base64 encoding: Skip display (show download link)
   * 2. Blob without base64: Convert to text
   * 3. Other data: Use directly
   */
  watch(() => props.dataSource, async (newValue) => {
    const data = newValue?.data;
    
    if (data instanceof Blob) {
      if (props.dataSource.contentEncoding === 'base64') {
        // Base64 encoded binary - don't convert, show download link instead
        return;
      }
      // Convert Blob to text for display
      showData.value = await convertBlob(data);
      return;
    }

    // Use data directly (string, object, etc.)
    showData.value = data || '';
  }, {
    deep: true,      // Watch for deep changes in nested objects
    immediate: true  // Execute immediately on mount
  });

  // Emit rendered event after component is fully mounted
  nextTick(() => {
    emit('rendered', true);
  });
});

/**
 * Computed: Text to copy to clipboard
 * Converts showData to string format for clipboard
 * 
 * Conversion rules:
 * - Empty/null/undefined: Return empty string
 * - Object: Convert to JSON string
 * - Other types: Use as-is
 * 
 * @returns String content for clipboard
 */
const copyText = computed(() => {
  if (showData.value === '' || showData.value === null || showData.value === undefined) {
    return '';
  }

  if (typeof showData.value === 'object') {
    return JSON.stringify(showData.value);
  }

  return showData.value;
});
</script>
<template>
  <!-- Response Body Component Container -->
  <div class="h-full py-3 overflow-hidden flex flex-col">
    <!-- 
      Toolbar Section
      - View mode tabs (Pretty/Raw/Preview)
      - Content type display
      - Download and copy actions
    -->
    <div class="flex items-center mb-3">
      <!-- 
        View Mode Tabs
        - Pretty: Formatted with syntax highlighting
        - Raw: Unformatted original content
        - Preview: Visual preview when applicable
      -->
      <div class="flex flex-freeze-auto items-center rounded overflow-hidden text-3 text-text-sub-content">
        <!-- Pretty Tab -->
        <div
          :class="{'res-tab-active':currentTabId==='pretty'}"
          class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-light"
          @click="handleSelect('pretty')">
          {{ t('commonComp.xcan_debugResponseBody.prettyFormat') }}
        </div>
        
        <!-- Raw Tab -->
        <div
          :class="{'res-tab-active':currentTabId==='raw'}"
          class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-light"
          @click="handleSelect('raw')">
          {{ t('commonComp.xcan_debugResponseBody.rawFormat') }}
        </div>
        
        <!-- Preview Tab -->
        <div
          :class="{'res-tab-active':currentTabId==='preview'}"
          class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-light"
          @click="handleSelect('preview')">
          {{ t('actions.preview') }}
        </div>
      </div>
      
      <!-- Content Type Badge -->
      <div
        class="flex flex-freeze-auto items-center h-7 px-3 ml-3 text-3 rounded bg-gray-light text-text-sub-content">
        {{ contentType }}
      </div>
      
      <!-- Download Icon -->
      <IconDownload class="ml-7 mr-4 text-4" @click="downloadFile" />
      
      <!-- Copy Icon -->
      <IconCopy class="text-4" :copyText="copyText" />
    </div>
    
    <!-- 
      Base64 Binary File Display
      - Shows download link for base64 encoded binary files
      - Only displayed when contentEncoding is 'base64'
    -->
    <template v-if="props.dataSource.contentEncoding === 'base64'">
      <Button
        type="link"
        class="self-start px-0"
        @click="downloadFile">
        {{ downloadFilename }}
      </Button>
    </template>
    
    <!-- 
      Content Display Area
      - Uses FormatHighlight component for syntax highlighting
      - Supports different formats based on currentTabId
      - Auto-detects content type for appropriate highlighting
    -->
    <FormatHighlight
      class="flex-1 overflow-y-auto"
      :dataSource="showData"
      :dataType="contentType"
      :format="currentTabId" />
  </div>
</template>
<style scoped>
.copied-success {
  @apply text-status-success;
}

.res-tab-active {
  @apply bg-blue-active text-text-content;
}

.pretty-container {
  height: calc(100% - 40px);
  border-radius: 2px;
}

:deep(.jv-container.jv-light) {
  @apply flex-1 overflow-y-auto pb-5;
}
</style>
