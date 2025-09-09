<script setup lang="ts">
import { computed, watch, ref, inject } from 'vue';
import { Grid, Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  value: Record<string, any>;
  enabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: () => ({}),
  enabled: undefined
});

const projectInfo = inject('projectInfo', ref({ id: '' }));
const columns = computed(() => {
  return [[
    {
      label: t('xcan_httpTestInfo.testResult'),
      dataIndex: 'passed'
    },
    {
      label: t('xcan_httpTestInfo.failureReason'),
      dataIndex: 'failureMessage'
    },
    {
      label: t('xcan_httpTestInfo.testCount'),
      dataIndex: 'testNum'
    },
    {
      label: t('xcan_httpTestInfo.failureCount'),
      dataIndex: 'testFailureNum'
    },
    {
      label: t('xcan_httpTestInfo.scriptId'),
      dataIndex: 'scriptId'
    },
    {
      label: t('xcan_httpTestInfo.scriptName'),
      dataIndex: 'scriptName',
      ellipsis: true
    },
    {
      label: t('xcan_httpTestInfo.executionId'),
      dataIndex: 'execId'
    },
    {
      label: t('xcan_httpTestInfo.executionName'),
      dataIndex: 'execName',
      ellipsis: true
    },
    {
      label: t('xcan_httpTestInfo.lastExecutor'),
      dataIndex: 'execByName'
    },
    {
      label: t('xcan_httpTestInfo.lastExecutionTime'),
      dataIndex: 'lastExecDate'
    }
  ]];
});

const dataSource = ref<Record<string, any>>({});
watch(() => props.value, newValue => {
  dataSource.value = newValue;
}, {
  immediate: true
});
</script>
<template>
  <Grid
    :columns="columns"
    :dataSource="dataSource">
    <template #passed="{text}">
      <div class="flex items-center">
        <template v-if="!dataSource.id">
          {{ t('xcan_httpTestInfo.untested') }}
        </template>
        <template v-else-if="props.enabled === false && !dataSource.id">
          {{ t('xcan_httpTestInfo.disabled') }}
        </template>
        <template v-else-if="text">
          <Icon icon="icon-duihao" class="text-status-success" />
          {{ t('xcan_httpTestInfo.passed') }} {{ props.enabled === false ? `(${t('xcan_httpTestInfo.disabled')})` : '' }}
        </template>
        <template v-else>
          <Icon icon="icon-chahao" class="text-status-error" />
          {{ t('xcan_httpTestInfo.failed') }} {{ props.enabled === false ? `(${t('xcan_httpTestInfo.disabled')})` : '' }}
        </template>
      </div>
    </template>
    <template #execName="{text}">
      <a
        v-if="text"
        :title="text"
        class="text-theme-special"
        :href="`/execution/info/${dataSource.execId}?projectId=${projectInfo.id}`"
        target="_blank">{{ text }}</a>
      <template v-else>--</template>
    </template>
    <template #scriptName="{text}">
      <a
        v-if="text"
        :title="text"
        class="text-theme-special truncate"
        :href="`/scripts/detail/${dataSource.scriptId}?projectId=${projectInfo.id}&type=view`"
        target="_blank">{{ text }}</a>
      <template v-else>--</template>
    </template>
  </Grid>
</template>
<style scoped>
.task-item:hover .del-btn{
  @apply visible;
}
</style>
