<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { download } from '@xcan-angus/infra';

const { t } = useI18n();

interface Props {
  caseInfo?: {[key: string]: any};
  hideTitle: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  caseInfo: undefined,
  hideTitle: false
});

const attachmentsData = computed(() => {
  return props.caseInfo?.attachmentsData || [];
});

const handleDownload = (url:string) => {
  download(url);
};

</script>
<template>
  <div class="bg-white rounded-lg border border-gray-200 p-6">
    <div v-if="!props.hideTitle" class="flex items-center mb-4">
      <Icon icon="icon-fujian" class="text-indigo-500 mr-2" />
      <h3 class="text-lg font-semibold text-gray-900">
        {{ t('caseReview.comp.attachment.title') }}
      </h3>
    </div>

    <template v-if="attachmentsData.length">
      <div class="max-h-32 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100">
        <div class="space-y-2">
          <div
            v-for="(item,index) in props.caseInfo?.attachments"
            :key="index"
            class="flex items-center justify-between p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer group"
            @click="handleDownload(item.url)">
            <div class="flex items-center space-x-3">
              <Icon icon="icon-fujian" class="text-gray-400 group-hover:text-indigo-500" />
              <span
                :title="item.name"
                class="text-sm text-gray-700 truncate max-w-48 group-hover:text-indigo-600">
                {{ item.name }}
              </span>
            </div>
            <Icon icon="icon-xiazai" class="text-gray-400 group-hover:text-indigo-500" />
          </div>
        </div>
      </div>
    </template>
    <template v-else>
      <div class="text-center py-8 text-gray-400">
        <Icon icon="icon-kong" class="text-4xl mb-2" />
        <div>{{ t('common.noAttachment') }}</div>
      </div>
    </template>
  </div>
</template>
