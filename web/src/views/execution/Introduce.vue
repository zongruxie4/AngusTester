<script lang="ts" setup>
import { inject, Ref, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import { useDashboardConfig } from '@/views/execution/composables/useDashboardConfig';
import Dashboard from '@/components/dashboard/Dashboard.vue';

const { t } = useI18n();

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

// Use dashboard config
const { dashboardConfig, dashboardConstants } = useDashboardConfig();
</script>
<template>
  <div class="flex content items-center">
    <!-- Left content area - flexible width -->
    <div class="flex content items-center flex-1 pr-20">
      <img src="../../assets/home/exec.svg" class="w-43 h-35" />
      <div class="ml-3.25 text-content flex-1 font-serif">
        <div class="text-3.5 font-semibold">{{ t('execution.header.title') }}</div>
        <div class="mt-2">
          <Icon icon="icon-duihaolv" class="mr-2.5" />
          <span class="text-3.5">{{ t('execution.header.contentList1') }}</span>
        </div>
        <div class="mt-1">
          <Icon icon="icon-duihaolv" class="mr-2.5" />
          <span class="text-3.5">{{ t('execution.header.contentList2') }}</span>
        </div>
      </div>
    </div>
    <!-- Statistics dashboard panel - fixed width 680px -->
    <div class="w-170">
      <Dashboard
        :config="dashboardConfig"
        :apiRouter="dashboardConstants.apiRouter"
        :resource="dashboardConstants.resource"
        :barTitle="dashboardConstants.barTitle.value"
        :projectId="projectId"
        :showChartParam="dashboardConstants.showChartParam" />
    </div>
  </div>
</template>
