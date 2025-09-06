<script setup lang="ts">
import { defineAsyncComponent, ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';
import type { MyScenariosProps, TabConfig, ScenarioQueryParams } from './types';

const { t } = useI18n();

// Component props with proper typing
const props = withDefaults(defineProps<MyScenariosProps>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

// Async component loading
const Table = defineAsyncComponent(() => import('./MyScenariosTable.vue'));

// Reactive state for notifications and totals
const deletedNotify = ref<string>();
const createByMeTotal = ref(0);
const followTotal = ref(0);
const favoriteTotal = ref(0);

/**
 * Generate query parameters for different tab types
 */
const createQueryParams = (type: 'createdBy' | 'followBy' | 'favouriteBy'): ScenarioQueryParams => {
  const baseParams: ScenarioQueryParams = {};
  
  switch (type) {
    case 'createdBy':
      return { createdBy: props.userInfo?.id };
    case 'followBy':
      return { followBy: props.userInfo?.id };
    case 'favouriteBy':
      return { favouriteBy: props.userInfo?.id };
    default:
      return baseParams;
  }
};

/**
 * Tab configuration with computed properties
 */
const tabConfigs = computed((): TabConfig[] => [
  {
    key: 'create',
    label: t('scenarioHome.myScenarios.added'),
    params: createQueryParams('createdBy'),
    total: createByMeTotal.value
  },
  {
    key: 'follow',
    label: t('scenarioHome.myScenarios.followed'),
    params: createQueryParams('followBy'),
    total: followTotal.value
  },
  {
    key: 'favorite',
    label: t('scenarioHome.myScenarios.favorited'),
    params: createQueryParams('favouriteBy'),
    total: favoriteTotal.value
  }
]);

/**
 * Get the appropriate total ref based on tab key
 */
const getTotalRef = (key: string) => {
  switch (key) {
    case 'create':
      return createByMeTotal;
    case 'follow':
      return followTotal;
    case 'favorite':
      return favoriteTotal;
    default:
      return createByMeTotal;
  }
};
</script>

<template>
  <div>
    <!-- Page title -->
    <div class="text-3.5 font-semibold mb-1">{{ t('scenarioHome.myScenarios.title') }}</div>
    
    <!-- Tab navigation -->
    <Tabs size="small">
      <TabPane 
        v-for="tab in tabConfigs" 
        :key="tab.key" 
        forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ tab.label }}</span>
            <span>(</span>
            <span>{{ tab.total }}</span>
            <span>)</span>
          </div>
        </template>
        
        <!-- Table component with dynamic props -->
        <Table
          :total="getTotalRef(tab.key).value"
          @update:total="(value) => getTotalRef(tab.key).value = value"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="tab.params" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
.ant-tabs {
  line-height: 20px;
}

:deep(.ant-tabs-content-holder) {
  min-height: 166px;
}

.ant-tabs-top>:deep(.ant-tabs-nav),
.ant-tabs-bottom>:deep(.ant-tabs-nav),
.ant-tabs-top>:deep(div)>.ant-tabs-nav,
.ant-tabs-bottom>:deep(div)>.ant-tabs-nav {
  margin-bottom: 14px;
}
</style>
