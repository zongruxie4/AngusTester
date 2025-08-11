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

const currentTabId = ref<'pretty' | 'raw' | 'preview'>('pretty');
const handleSelect = (id: 'pretty' | 'raw' | 'preview'): void => {
  currentTabId.value = id;
};

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

const downloadFile = () => {
  const data = props.dataSource?.data;
  if (!data) {
    return '';
  }

  let blob = data;
  if (!(data instanceof Blob)) {
    if (typeof blob === 'object') {
      blob = JSON.stringify(blob, null, 2);
    }

    blob = new Blob([blob], {
      type: 'application/octet-stream'
    });
  }

  const url = URL.createObjectURL(blob);

  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = getDownloadFilename();
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);

  window.URL.revokeObjectURL(url);
};

const contentType = computed(() => {
  const type = (props.dataSource?.headers?.['Content-Type'] || props.dataSource?.headers?.['content-type'])?.replace(/\s+/gi, '');
  if (!type) {
    return 'plain';
  }

  const temp = type.replace(/\S+\/(\S[^;]+)\s*\S*/, '$1').replace(/;/, '');
  return ['json', 'xml', 'html'].includes(temp) ? temp : type;
});

const downloadFilename = computed(() => {
  const disposition = (props.dataSource?.headers['content-Disposition'] || props.dataSource?.headers['content-disposition']);
  if (!disposition) {
    const contentType = props.dataSource?.headers?.['Content-Type'] || props.dataSource?.headers?.['content-type'] || '';
    if ([
      'image',
      'audio',
      'video',
      // 压缩文件
      'zip',
      'x-rar-compressed',
      'x-7z-compressed',
      'x-tar',
      'gzip',
      'x-bzip2',
      'x-xz',
      // Microsoft 文档
      'pdf',
      'vnd.ms-',
      'msword',
      'vnd.openxmlformats'
    ].some(i => contentType.includes(i))) {
      if (props.dataSource.config.url) {
        try {
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

  return disposition.replace(/"/gi, '').replace(/[\s\S]*filename=(\S+.\S+)[\s\S]*/i, '$1').replace(/(\S\s+);\S*/, '$1').replace(/\s$/, '');
});

const convertBlob = (blob: Blob) => {
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

const showData = ref<any>('');
onMounted(() => {
  watch(() => props.dataSource, async (newValue) => {
    const data = newValue?.data;
    if (data instanceof Blob) {
      if (props.dataSource.contentEncoding === 'base64') {
        return;
      }
      showData.value = await convertBlob(data);
      return;
    }

    showData.value = data || '';
  }, {
    deep: true,
    immediate: true
  });
});

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
  <div class="h-full py-3 overflow-hidden flex flex-col">
    <div class="flex items-center mb-3">
      <div class="flex flex-freeze-auto items-center rounded overflow-hidden text-3 text-text-sub-content">
        <div
          :class="{'res-tab-active':currentTabId==='pretty'}"
          class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-light"
          @click="handleSelect('pretty')">
          {{t('service.apis.response.pretty')}}
        </div>
        <div
          :class="{'res-tab-active':currentTabId==='raw'}"
          class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-light"
          @click="handleSelect('raw')">
          {{t('service.apis.response.raw')}}
        </div>
        <div
          :class="{'res-tab-active':currentTabId==='preview'}"
          class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-light"
          @click="handleSelect('preview')">
          {{t('service.apis.response.preview')}}
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
