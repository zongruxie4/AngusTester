<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { IconCopy, IconDownload, FormatHighlight } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

interface Props {
  dataSource: any;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({ data: null })
});

const { t } = useI18n();

// Reactive reference for tracking the currently selected tab
const currentTabId = ref<'pretty' | 'raw' | 'preview'>('pretty');

/**
 * Handle tab selection change
 * @param id - The ID of the tab to select
 */
const handleSelect = (id: 'pretty' | 'raw' | 'preview'): void => {
  currentTabId.value = id;
};

/**
 * Generate filename for download based on content type or disposition header
 * @returns The filename for the downloaded file
 */
const getDownloadFilename = (): string => {
  // Use filename from disposition header if available
  if (downloadFilename.value) {
    return downloadFilename.value;
  }

  // Generate filename based on content type
  if (contentType.value === 'plain') {
    return 'response.txt';
  }

  if (contentType.value === 'javascript') {
    return 'response.js';
  }
  return 'response.' + contentType.value;
};

/**
 * Download the response data as a file
 */
const downloadFile = () => {
  const data = props.dataSource?.data;
  if (!data) {
    return;
  }

  let blob = data;
  // Convert non-Blob data to Blob
  if (!(data instanceof Blob)) {
    if (typeof blob === 'object') {
      blob = JSON.stringify(blob, null, 2);
    }

    blob = new Blob([blob], {
      type: 'application/octet-stream'
    });
  }

  // Create object URL for the blob
  const url = URL.createObjectURL(blob);

  // Create temporary anchor element for download
  const anchorElement = document.createElement('a');
  anchorElement.style.display = 'none';
  anchorElement.href = url;
  anchorElement.download = getDownloadFilename();
  document.body.appendChild(anchorElement);
  anchorElement.click();
  document.body.removeChild(anchorElement);

  // Clean up object URL
  window.URL.revokeObjectURL(url);
};

/**
 * Computed property to determine content type from response headers
 * @returns The content type of the response
 */
const contentType = computed(() => {
  // Extract content type from headers (case-insensitive)
  const contentTypeHeader = (props.dataSource?.headers?.['Content-Type'] || props.dataSource?.headers?.['content-type'])?.replace(/\s+/gi, '');
  if (!contentTypeHeader) {
    return 'plain';
  }

  // Extract the subtype from the content type
  const subtype = contentTypeHeader.replace(/\S+\/(\S[^;]+)\s*\S*/, '$1').replace(/;/, '');
  return ['json', 'xml', 'html'].includes(subtype) ? subtype : contentTypeHeader;
});

/**
 * Computed property to extract filename from content disposition header
 * @returns The filename extracted from headers or generated based on URL
 */
const downloadFilename = computed(() => {
  // Extract filename from content disposition header
  const contentDisposition = (props.dataSource?.headers['content-Disposition'] || props.dataSource?.headers['content-disposition']);
  if (!contentDisposition) {
    // If no disposition header, try to extract filename from URL for binary content types
    const contentTypeHeader = props.dataSource?.headers?.['Content-Type'] || props.dataSource?.headers?.['content-type'] || '';
    const binaryContentTypes = [
      'image',
      'audio',
      'video',
      // Compression formats
      'zip',
      'x-rar-compressed',
      'x-7z-compressed',
      'x-tar',
      'gzip',
      'x-bzip2',
      'x-xz',
      // Microsoft documents
      'pdf',
      'vnd.ms-',
      'msword',
      'vnd.openxmlformats'
    ];

    // Check if content type is binary
    if (binaryContentTypes.some(type => contentTypeHeader.includes(type))) {
      if (props.dataSource.config?.url) {
        try {
          const url = new URL(props.dataSource.config.url);
          const pathSegments = url.pathname.split('/');
          return pathSegments[pathSegments.length - 1] || 'file';
        } catch (error) {
          return 'file';
        }
      }
    }
    return '';
  }

  // Extract filename from disposition header
  return contentDisposition
    .replace(/"/gi, '')
    .replace(/[\s\S]*filename=(\S+.\S+)[\s\S]*/i, '$1')
    .replace(/(\S\s+);\S*/, '$1')
    .replace(/\s$/, '');
});

/**
 * Convert Blob data to text
 * @param blob - The Blob to convert
 * @returns Promise that resolves with the text content
 */
const convertBlob = (blob: Blob): Promise<any> => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onload = () => {
      resolve(reader.result);
    };

    reader.onerror = () => {
      resolve(blob);
    };

    reader.readAsText(blob, 'utf-8');
  });
};

// Reactive reference for the processed response data to display
const showData = ref<any>('');

/**
 * Computed property to get text for copy functionality
 * @returns The text content that can be copied
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

// Watch for changes in dataSource and update showData accordingly
onMounted(() => {
  watch(() => props.dataSource, async (newValue) => {
    const data = newValue?.data;
    // Handle Blob data
    if (data instanceof Blob) {
      // Skip processing for base64 encoded content
      if (props.dataSource.contentEncoding === 'base64') {
        return;
      }
      showData.value = await convertBlob(data);
      return;
    }

    // Handle other data types
    showData.value = data || '';
  }, {
    deep: true,
    immediate: true
  });
});
</script>

<template>
  <div class="h-full py-3 overflow-hidden flex flex-col">
    <div class="flex items-center mb-3">
      <div class="flex flex-freeze-auto items-center rounded overflow-hidden text-3 text-text-sub-content">
        <div
          :class="{'res-tab-active':currentTabId==='pretty'}"
          class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-light"
          @click="handleSelect('pretty')">
          {{ t('service.apis.response.pretty') }}
        </div>
        <div
          :class="{'res-tab-active':currentTabId==='raw'}"
          class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-light"
          @click="handleSelect('raw')">
          {{ t('service.apis.response.raw') }}
        </div>
        <div
          :class="{'res-tab-active':currentTabId==='preview'}"
          class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-light"
          @click="handleSelect('preview')">
          {{ t('actions.preview') }}
        </div>
      </div>
      <div
        class="flex flex-freeze-auto items-center h-7 px-3 ml-3 text-3 rounded bg-gray-light text-text-sub-content">
        {{ contentType }}
      </div>
      <IconDownload class="ml-7 mr-4 text-4" @click="downloadFile" />
      <IconCopy class="text-4" :copyText="copyText" />
    </div>
    <template v-if="props.dataSource.contentEncoding === 'base64'">
      <Button
        type="link"
        class="self-start px-0"
        @click="downloadFile">
        {{ downloadFilename }}
      </Button>
    </template>
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
