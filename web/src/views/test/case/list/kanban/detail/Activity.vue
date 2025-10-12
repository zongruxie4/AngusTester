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
  <div class="basic-info-drawer">
    <div class="basic-info-header">
      <h3 class="basic-info-title">
        {{ t('common.activity') }}
      </h3>
    </div>

    <!-- Scrollable Content Area -->
    <div class="scrollable-content">
      <div class="basic-info-content">
        <Scroll
          v-if="caseId"
          :action="`${TESTER}/activity`"
          :hideNoData="!!activityList.length"
          :params="queryParams"
          :lineHeight="32"
          transition
          style="height:calc(100% - 30px);"
          @change="handleActivityChange">
          <ActivityInfoPage
            :dataSource="activityList as any"
            infoKey="description"
            class="pr-5" />
        </Scroll>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Main container styles */
.basic-info-drawer {
  width: 370px;
  height: 100%;
  background: #ffffff;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Header styles */
.basic-info-header {
  padding: 12px 20px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.basic-info-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.2;
}

/* Scrollable content area */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

/* Content area styles */
.basic-info-content {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
