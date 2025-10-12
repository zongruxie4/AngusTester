<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { ActivityInfo as ActivityInfoPage, Scroll } from '@xcan-angus/vue-ui';
import { SearchCriteria, TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { CaseInfoEditProps } from '@/views/test/case/list/types';
import { ActivityInfo } from '@/types/types';
import { CombinedTargetType } from '@/enums/enums';

const { t } = useI18n();

const props = withDefaults(defineProps<CaseInfoEditProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const activityList = ref<ActivityInfo[]>([]);
const queryParams = ref<{
  mainTargetId: number;
  filters: { key: 'targetType'; value: CombinedTargetType; op: SearchCriteria.OpEnum; }[]
}>();

/**
 * Handle activity list updates emitted by the infinite scroll component
 * Keeps local state in sync with server-side paginated results
 */
const handleActivityChange = (data: any[]) => {
  activityList.value = data;
};

const caseId = computed(() => {
  return props.dataSource?.id;
});

/**
 * Initialize reactive watchers for case id changes
 * Recomputes query parameters whenever selected case changes
 */
const initWatchers = () => {
  watch(() => caseId.value, (newValue, oldValue) => {
    if (newValue === oldValue) {
      return;
    }

    queryParams.value = {
      mainTargetId: caseId.value,
      filters: [{ key: 'targetType', value: CombinedTargetType.FUNC_CASE, op: SearchCriteria.OpEnum.Equal }]
    };
  }, { immediate: true });
};

onMounted(() => {
  initWatchers();
});
</script>
<template>
  <div class="h-full text-3 leading-5 pl-5">
    <div class="text-theme-title mb-2.5 font-semibold">
      {{ t('common.activity') }}
    </div>

    <Scroll
      v-if="caseId"
      :action="`${TESTER}/activity`"
      :hideNoData="!!activityList.length"
      :params="queryParams"
      :lineHeight="32"
      transition
      style="height:calc(100% - 30px);"
      @change="handleActivityChange">
      <ActivityInfoPage :dataSource="activityList" infoKey="description" />
    </Scroll>
  </div>
</template>
