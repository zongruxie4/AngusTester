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
  <div class="bg-white rounded-lg border border-gray-200 p-6">
    <div v-if="!hideTitle" class="flex items-center mb-4">
      <Icon icon="icon-gongnengyongli" class="text-cyan-500 mr-2" />
      <h3 class="text-lg font-semibold text-gray-900">
        {{ t('caseReview.comp.assocCase.title') }}
      </h3>
    </div>

    <div v-if="dataList.length" class="space-y-2">
      <RouterLink
        v-for="item in dataList"
        :key="item.id"
        :to="item.linkUrl"
        target="_blank"
        class="flex items-center p-3 bg-gray-50 rounded-lg hover:bg-cyan-50 hover:border-cyan-200 border border-transparent transition-all group">
        <Icon icon="icon-gongnengyongli" class="text-gray-400 group-hover:text-cyan-500 mr-3 flex-shrink-0" />
        <span class="text-sm text-gray-700 group-hover:text-cyan-600 truncate">{{ item.name }}</span>
        <Icon icon="icon-wailian" class="text-gray-400 group-hover:text-cyan-500 ml-auto flex-shrink-0" />
      </RouterLink>
    </div>

    <div v-else class="text-center py-8 text-gray-400">
      <Icon icon="icon-kong" class="text-4xl mb-2" />
      <div>{{ t('common.noData') }}</div>
    </div>
  </div>
</template>
