<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

import { BasicProps } from '@/types/types';

const { t } = useI18n();

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: {},
  notify: undefined
});

/**
 * Async component for data table
 */
const Table = defineAsyncComponent(() => import('./AddedTable.vue'));

/**
 * Reactive state for tracking deleted notifications and totals
 */
const deletedNotify = ref<string>();
const variableTotal = ref(0);
const dataSetTotal = ref(0);
</script>

<template>
  <div>
    <!-- Section title -->
    <div class="text-3.5 font-semibold mb-1">
      {{ t('dataHome.myCreationSummary.title') }}
    </div>

    <!-- Tab navigation -->
    <Tabs size="small">
      <!-- Variables tab -->
      <TabPane key="variable" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('dataHome.myCreationSummary.addedVariables') }}</span>
            <span>(</span>
            <span>{{ variableTotal }}</span>
            <span>)</span>
          </div>
        </template>

        <!-- Variables table component -->
        <Table
          v-model:total="variableTotal"
          v-model:deletedNotify="deletedNotify"
          type="variable"
          :userId="props.userInfo?.id"
          :notify="props.notify"
          :projectId="props.projectId" />
      </TabPane>

      <!-- Datasets tab -->
      <TabPane key="dataSet" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('dataHome.myCreationSummary.addedDataSets') }}</span>
            <span>(</span>
            <span>{{ dataSetTotal }}</span>
            <span>)</span>
          </div>
        </template>

        <!-- Datasets table component -->
        <Table
          v-model:total="dataSetTotal"
          v-model:deletedNotify="deletedNotify"
          type="dataSet"
          :notify="props.notify"
          :userId="props.userInfo?.id"
          :projectId="props.projectId" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
.ant-tabs {
  line-height: 20px;
}

:deep(.ant-tabs-content-holder) {
  min-height: 225px;
}

.ant-tabs-top>:deep(.ant-tabs-nav),
.ant-tabs-bottom>:deep(.ant-tabs-nav),
.ant-tabs-top>:deep(div)>.ant-tabs-nav,
.ant-tabs-bottom>:deep(div)>.ant-tabs-nav {
  margin-bottom: 14px;
}
</style>
