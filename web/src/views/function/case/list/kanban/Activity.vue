<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ActivityInfo as ActivityInfoPage, Scroll } from '@xcan-angus/vue-ui';
import { SearchCriteria, TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { CaseInfoEditProps } from '@/views/function/case/list/types';
import { ActivityInfo } from '@/types/types';
import { CombinedTargetType } from '@/enums/enums';

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const dataList = ref<ActivityInfo[]>([]);
const params = ref<{
  mainTargetId:string;
  filters:[{ key: 'targetType', value: CombinedTargetType.FUNC_CASE, op: SearchCriteria.OpEnum.Equal }]
}>();

const change = (data: ActivityInfo[]) => {
  dataList.value = data;
};

const targetId = computed(() => {
  return props.dataSource?.id;
});

onMounted(() => {
  watch(() => targetId.value, (newValue, oldValue) => {
    if (newValue === oldValue) {
      return;
    }

    params.value = {
      mainTargetId: targetId.value,
      filters: [{ key: 'targetType', value: CombinedTargetType.FUNC_CASE, op: SearchCriteria.OpEnum.Equal }]
    };
  }, { immediate: true });
});
</script>
<template>
  <div class="h-full text-3 leading-5 pl-5">
    <div class="text-theme-title mb-2.5 font-semibold">{{ t('functionCase.kanbanView.activity.title') }}</div>

    <Scroll
      v-if="targetId"
      :action="`${TESTER}/activity`"
      :hideNoData="!!dataList.length"
      :params="params"
      :lineHeight="32"
      transition
      style="height:calc(100% - 30px);"
      @change="change">
      <ActivityInfoPage :dataSource="dataList" infoKey="description" />
    </Scroll>
  </div>
</template>
