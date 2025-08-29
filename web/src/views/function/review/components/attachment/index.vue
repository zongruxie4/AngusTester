<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { NoData } from '@xcan-angus/vue-ui';
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
  <div>
    <div v-if="!props.hideTitle" class="font-semibold text-3.5">
      {{ t('caseReview.comp.attachment.title') }}
    </div>
    <template v-if="attachmentsData.length">
      <div style="height: 90px;scrollbar-gutter: stable;" class="overflow-hidden hover:overflow-y-auto -mr-2 pr-1">
        <div
          v-for="(item,index) in props.caseInfo?.attachments"
          :key="index"
          :class="{'rounded-b':index===props.caseInfo?.attachments.length-1}"
          class="flex items-center justify-between text-3 leading-3">
          <div
            :title="item.name"
            class="truncate text-theme-sub-content leading-4 cursor-pointer"
            style="width: 250px;"
            @click="handleDownload(item.url)">
            {{ item.name }}
          </div>
        </div>
      </div>
    </template>
    <template v-else>
      <NoData size="small" class="my-4" />
    </template>
  </div>
</template>
