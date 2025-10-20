<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { IconCopy, IconDownload, NoData } from '@xcan-angus/vue-ui';
// @ts-ignore - FormatHighlight is available but type definition may be missing
import { FormatHighlight } from '@xcan-angus/vue-ui';

import { ExecContent } from '@/plugins/test/types';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Display format type
 * - pretty: Formatted with syntax highlighting
 * - raw: Plain unformatted text
 * - preview: Visual preview (for images, HTML, etc.)
 */
type DisplayFormat = 'pretty' | 'raw' | 'preview';

/**
 * Component props interface
 */
interface Props {
  url: string;                                 // Request URL (used for filename extraction)
  value: ExecContent['content']['response'];   // Response data containing headers and body
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  url: undefined,
  value: undefined
});

/**
 * Ref: Current display format
 * Controls which tab is active (pretty/raw/preview)
 */
const currentTabId = ref<DisplayFormat>('pretty');

/**
 * Ref: Response data to display
 * Holds the raw content from response
 */
const showData = ref<any>('');

/**
 * Switch between display formats
 * 
 * @param id - Format to switch to ('pretty' | 'raw' | 'preview')
 */
const handleSelect = (id: DisplayFormat): void => {
  currentTabId.value = id;
};

/**
 * Get download filename with appropriate extension
 * Priority:
 * 1. Filename from downloadFilename computed property (from headers or URL)
 * 2. Default filename based on content type
 * 
 * @returns Filename with extension
 */
const getDownloadFilename = (): string => {
  if (downloadFilename.value) {
    return downloadFilename.value;
  }

  // Default filenames for common content types
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
 * Cleans up resources after download
 */
const downloadFile = (): void => {
  const rawContent = props.value?.rawContent;
  if (!rawContent) {
    return;
  }

  // Create blob from response content
  const blob = new Blob([rawContent], {
    type: 'application/octet-stream'
  });

  // Create temporary download link
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = getDownloadFilename();
  
  // Trigger download
  document.body.appendChild(a);
  a.click();
  
  // Clean up
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
};

/**
 * Computed: Normalized response headers
 * Converts all header names to lowercase for consistent access
 * HTTP headers are case-insensitive, lowercase normalization simplifies lookups
 * 
 * @returns Object with lowercase header names as keys
 */
const headers = computed(() => {
  if (!props.value?.headers) {
    return {};
  }

  return Object.entries(props.value.headers).reduce((prev, cur) => {
    prev[cur[0].toLowerCase()] = cur[1];
    return prev;
  }, {} as Record<string, string>);
});

/**
 * Computed: Content type from response headers
 * Extracts and normalizes content type from Content-Type header
 * 
 * Detection logic:
 * 1. Extract MIME subtype (e.g., "application/json" -> "json")
 * 2. Remove charset and other parameters
 * 3. Return recognized types (json, xml, html) or full type string
 * 4. Default to "plain" if no content-type header exists
 * 
 * @returns Content type string (json, xml, html, plain, or full MIME type)
 */
const contentType = computed(() => {
  const type = headers.value['content-type']?.replace(/\s+/gi, '');
  if (!type) {
    return 'plain';
  }

  // Extract subtype from MIME type (e.g., "json" from "application/json")
  const temp = type.replace(/\S+\/(\S[^;]+)\s*\S*/, '$1').replace(/;/, '');
  
  // Return recognized types or full MIME type
  return ['json', 'xml', 'html'].includes(temp) ? temp : type;
});

/**
 * Computed: Download filename from headers or URL
 * Determines the appropriate filename for downloading response content
 * 
 * Priority:
 * 1. Filename from Content-Disposition header
 * 2. Filename from URL pathname (for binary/document types)
 * 3. Empty string (will use default filename)
 * 
 * Supports file types:
 * - Media files: image, audio, video
 * - Compressed files: zip, rar, 7z, tar, gzip, bzip2, xz
 * - Documents: PDF, Microsoft Office documents (Word, Excel, PowerPoint)
 * 
 * @returns Filename string or empty string
 */
const downloadFilename = computed(() => {
  const disposition = headers.value['content-disposition'];
  
  // Try to extract filename from Content-Disposition header
  if (!disposition) {
    // For binary/document types, try to extract filename from URL
    const binaryTypes = [
      'image',
      'audio',
      'video',
      // Compressed files
      'zip',
      'x-rar-compressed',
      'x-7z-compressed',
      'x-tar',
      'gzip',
      'x-bzip2',
      'x-xz',
      // Documents
      'pdf',
      'vnd.ms-',           // Microsoft legacy formats
      'msword',            // .doc files
      'vnd.openxmlformats' // .docx, .xlsx, .pptx files
    ];
    
    if (binaryTypes.includes(contentType.value)) {
      if (props.url) {
        try {
          const _url = new URL(props.url);
          const paths = _url.pathname.split('/');
          return paths[paths.length - 1] || '';
        } catch (error) {
          return '';
        }
      }
    }

    return '';
  }

  // Parse Content-Disposition header to extract filename
  // Format: attachment; filename="example.pdf" or inline; filename=example.pdf
  return disposition
    .replace(/"/gi, '')                                    // Remove quotes
    .replace(/[\s\S]*filename=(\S+.\S+)[\s\S]*/i, '$1')   // Extract filename value
    .replace(/(\S\s+);\S*/, '$1')                          // Remove trailing parameters
    .replace(/\s$/, '');                                    // Trim trailing whitespace
});

/**
 * Lifecycle: Component mounted
 * Sets up watcher to sync response data to display data
 * Watches for changes in response value and updates showData accordingly
 */
onMounted(() => {
  watch(() => props.value, async (newValue) => {
    // Update display data when response value changes
    showData.value = newValue?.rawContent || '';
  }, { immediate: true });
});
</script>

<template>
  <!-- Empty state: no response data -->
  <template v-if="!props.value">
    <NoData size="small" class="my-10" />
  </template>
  
  <!-- Response body display with format tabs and actions -->
  <template v-else>
    <div class="h-full py-3 overflow-hidden flex flex-col">
      <!-- Toolbar: Format tabs, content type, download, and copy -->
      <div class="flex items-center mb-3">
        <!-- Format selection tabs -->
        <div class="flex flex-freeze-auto items-center rounded overflow-hidden text-3 text-theme-sub-content">
          <!-- Pretty format tab: Formatted with syntax highlighting -->
          <div
            :class="{'res-tab-active':currentTabId==='pretty'}"
            class="res-tab flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer"
            @click="handleSelect('pretty')">
            {{ t('httpPlugin.functionTestDetail.http.responseBody.prettyFormat') }}
          </div>
          
          <!-- Raw format tab: Plain unformatted text -->
          <div
            :class="{'res-tab-active':currentTabId==='raw'}"
            class="res-tab flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer"
            @click="handleSelect('raw')">
            {{ t('httpPlugin.functionTestDetail.http.responseBody.rawFormat') }}
          </div>
          
          <!-- Preview tab: Visual preview (for images, HTML, etc.) -->
          <div
            :class="{'res-tab-active':currentTabId==='preview'}"
            class="res-tab flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer"
            @click="handleSelect('preview')">
            {{ t('actions.preview') }}
          </div>
        </div>
        
        <!-- Content type indicator -->
        <div class="res-tab flex flex-freeze-auto items-center h-7 px-3 ml-3 text-3 rounded text-theme-sub-content">
          {{ contentType }}
        </div>
        
        <!-- Download button: Download response content as file -->
        <IconDownload 
          class="ml-7 mr-4 text-4" 
          @click="downloadFile" />
        
        <!-- Copy button: Copy response content to clipboard -->
        <IconCopy 
          class="text-4" 
          :copyText="props.value?.rawContent" />
      </div>
      
      <!-- Content display area with syntax highlighting -->
      <FormatHighlight
        class="flex-1 overflow-y-auto"
        :dataSource="showData"
        :dataType="contentType"
        :format="currentTabId" />
    </div>
  </template>
</template>
<style scoped>
.res-tab {
  background-color: #f7f8fb;
}

.res-tab-active {
  background-color: #eaf8ff;
  color: var(--content-text-content);
}

.pretty-container {
  height: calc(100% - 40px);
  border-radius: 2px;
}

:deep(.jv-container.jv-light) {
  flex: 1 1 0%;
  padding-bottom: 20px;
  overflow-y: auto;
}
</style>
