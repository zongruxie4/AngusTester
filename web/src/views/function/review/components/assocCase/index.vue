<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, NoData } from '@xcan-angus/vue-ui';

const { t } = useI18n();

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  caseId: string;
  dataSource: {
    id: string;
    name: string;
  }[];
  taskList:{ id:string; }[];
  hideTitle: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  caseId: undefined,
  dataSource: undefined,
  taskList: undefined,
  hideTitle: false
});
const dataList = computed(() => {
  return props.dataSource?.map(item => {
    return {
      ...item,
      linkUrl: `/function#cases?id=${item.id}&projectId=${props.projectId}`
    };
  }) || [];
});

</script>

<template>
  <div>
    <div v-if="!hideTitle" class="font-semibold text-3.5">
      {{ t('caseReview.comp.assocCase.title') }}
    </div>
    <div v-if="dataList.length" class="text-3 leading-5 pl-5.5 pt-2 space-y-0.5">
      <RouterLink
        v-for="item in dataList"
        :key="item.id"
        :to="item.linkUrl"
        target="_blank"
        class="flex items-center overflow-hidden">
        <Icon icon="icon-gongnengyongli" class="text-4 flex-shrink-0" />
        <span class="truncate ml-1.5">{{ item.name }}</span>
      </RouterLink>
    </div>
    <NoData
      v-else
      size="small"
      class="my-3.5" />
  </div>
</template>
