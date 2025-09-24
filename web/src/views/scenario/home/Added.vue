<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';
import type { MyScenariosProps, ScenarioQueryParams } from './types';

const { t } = useI18n();

// Component props with proper typing
const props = withDefaults(defineProps<MyScenariosProps>(), {
  projectId: undefined,
  notify: undefined
});

// Async component loading
const Table = defineAsyncComponent(() => import('./AddedTable.vue'));

// Reactive state for notifications and totals
const deletedNotify = ref<string>();
const createdTotal = ref(0);
const followTotal = ref(0);
const favoriteTotal = ref(0);

/**
 * Generate query parameters for different tab types
 */
const createQueryParams = (type: 'createdBy' | 'followBy' | 'favouriteBy'): ScenarioQueryParams => {
  const baseParams: ScenarioQueryParams = {};

  switch (type) {
    case 'createdBy':
      return { createdBy: props.userInfo?.id } as ScenarioQueryParams;
    case 'followBy':
      return { followBy: props.userInfo?.id } as ScenarioQueryParams;
    case 'favouriteBy':
      return { favouriteBy: props.userInfo?.id } as ScenarioQueryParams;
    default:
      return baseParams;
  }
};
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-1">
      {{ t('scenarioHome.myScenarios.title') }}
    </div>

    <Tabs size="small">
      <TabPane key="create" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.added') }}</span>
            <span>(</span>
            <span>{{ createdTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="createdTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :params="createQueryParams('createdBy')" />
      </TabPane>
      <TabPane key="follow" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.followed') }}</span>
            <span>(</span>
            <span>{{ followTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="followTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :params="createQueryParams('followBy')" />
      </TabPane>
      <TabPane key="favorite" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.favorited') }}</span>
            <span>(</span>
            <span>{{ favoriteTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="favoriteTotal"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :params="createQueryParams('favouriteBy')" />
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
