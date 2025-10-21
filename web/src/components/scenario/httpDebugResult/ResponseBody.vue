<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue';
import { IconCopy, IconDownload, NoData, FormatHighlight } from '@xcan-angus/vue-ui';

import { useI18n } from 'vue-i18n';
import { ExecContent } from './PropsType';
const { t } = useI18n();

interface Props {
  url:string;
  value: ExecContent['content']['response'];
}

const props = withDefaults(defineProps<Props>(), {
  url: undefined,
  value: undefined
});

const currentTabId = ref<'pretty' | 'raw' | 'preview'>('pretty');
const showData = ref<any>('');

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
  const rawContent = props.value?.rawContent;
  if (!rawContent) {
    return '';
  }

  const blob = new Blob([rawContent], {
    type: 'application/octet-stream'
  });

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

const headers = computed(() => {
  if (!props.value?.headers) {
    return {};
  }

  return Object.entries(props.value.headers).reduce((prev, cur) => {
    prev[cur[0].toLowerCase()] = cur[1];
    return prev;
  }, {});
});

const contentType = computed(() => {
  const type = headers.value['content-type']?.replace(/\s+/gi, '');
  if (!type) {
    return 'plain';
  }

  const temp = type.replace(/\S+\/(\S[^;]+)\s*\S*/, '$1').replace(/;/, '');
  return ['json', 'xml', 'html'].includes(temp) ? temp : type;
});

const downloadFilename = computed(() => {
  const disposition = headers.value['content-disposition'];
  if (!disposition) {
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
    ].includes(contentType.value)) {
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

  return disposition.replace(/"/gi, '').replace(/[\s\S]*filename=(\S+.\S+)[\s\S]*/i, '$1').replace(/(\S\s+);\S*/, '$1').replace(/\s$/, '');
});

onMounted(() => {
  watch(() => props.value, async (newValue) => {
    showData.value = newValue?.rawContent || '';
  }, { immediate: true });
});
</script>
<template>
  <template v-if="!props.value">
    <NoData size="small" class="my-10" />
  </template>
  <template v-else>
    <div class="h-full py-3 overflow-hidden flex flex-col">
      <div class="flex items-center mb-3">
        <div class="flex flex-freeze-auto items-center rounded overflow-hidden text-3 text-theme-sub-content">
          <div
            :class="{'res-tab-active':currentTabId==='pretty'}"
            class="res-tab flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer"
            @click="handleSelect('pretty')">
            {{ t('xcan_debugResponseBody.prettyFormat') }}
          </div>
          <div
            :class="{'res-tab-active':currentTabId==='raw'}"
            class="res-tab flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer"
            @click="handleSelect('raw')">
            {{ t('xcan_debugResponseBody.rawFormat') }}
          </div>
          <div
            :class="{'res-tab-active':currentTabId==='preview'}"
            class="res-tab flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer"
            @click="handleSelect('preview')">
            {{ t('actions.preview') }}
          </div>
        </div>
        <div class="res-tab flex flex-freeze-auto items-center h-7 px-3 ml-3 text-3 rounded text-theme-sub-content">
          {{ contentType }}
        </div>
        <IconDownload class="ml-7 mr-4 text-4" @click="downloadFile" />
        <IconCopy class="text-4" :copyText="props.value?.rawContent" />
      </div>
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
