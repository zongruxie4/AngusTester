<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Grid } from '@xcan-angus/vue-ui';
import { Tag } from 'ant-design-vue';

import TaskPriority from '@/components/TaskPriority/index.vue';

const { t } = useI18n();

interface Props {
  caseInfo?: {[key: string]: any}
}

withDefaults(defineProps<Props>(), {
  caseInfo: undefined
});

const infoColumns = computed(() => [
  [
    {
      label: t('common.name'),
      dataIndex: 'name'
    },
    {
      label: t('common.id'),
      dataIndex: 'id'
    },
    {
      label: t('common.code'),
      dataIndex: 'code'
    },
    {
      label: t('common.version'),
      dataIndex: 'version'
    },
    {
      label: t('common.softwareVersion'),
      dataIndex: 'softwareVersion'
    },
    {
      label: t('common.priority'),
      dataIndex: 'priority'
    },
    {
      label: t('common.tags'),
      dataIndex: 'tags'
    },
    {
      label: t('common.plan'),
      dataIndex: 'planName'
    },
    {
      label: t('common.module'),
      dataIndex: 'moduleName'
    },
    {
      label: t('common.evalWorkload'),
      dataIndex: 'evalWorkload',
      customRender: ({ text }) => text || '--'
    },
    {
      label: t('common.actualWorkload'),
      dataIndex: 'actualWorkload',
      customRender: ({ text }) => text || '--'
    }
  ]
]);

</script>
<template>
  <div class="bg-white rounded-lg p-6">
    <Grid
      :columns="infoColumns"
      :dataSource="caseInfo"
      :spacing="24"
      :marginBottom="6"
      labelSpacing="12px"
      font-size="14px"
      class="case-basic-info-grid">
      <template #version="{text}">
        <span v-if="text" class="px-2 py-1 bg-gray-100 text-gray-700 rounded">{{ text }}</span>
        <span v-else class="text-gray-400 text-sm">--</span>
      </template>

      <template #softwareVersion="{text}">
        <span v-if="text" class="px-2 py-1 bg-gray-100 text-gray-700 rounded">{{ text }}</span>
        <span v-else class="text-gray-400 text-sm">--</span>
      </template>

      <template #priority="{text}">
        <TaskPriority :value="text" />
      </template>

      <template #planName="{text}">
        <div class="flex items-center text-gray-700">
          <Icon icon="icon-jihua" class="mr-2 text-blue-500" />
          <span class="font-medium">{{ text }}</span>
        </div>
      </template>

      <template #moduleName="{text}">
        <div v-if="text" class="flex items-center">
          <Tag class="px-2 bg-gray-100 text-gray-700 border-gray-200 rounded-full flex items-center">
            <Icon icon="icon-mokuai" class="mr-2 text-gray-500" />
            <span>{{ text }}</span>
          </Tag>
        </div>
        <span v-else class="text-gray-400 text-sm">--</span>
      </template>

      <template #tags="{text}">
        <div class="flex flex-wrap gap-2">
          <Tag
            v-for="(tag,index) in (text || [])"
            :key="tag.id"
            class="px-2 text-sm font-medium bg-blue-50 text-blue-700 border-blue-200 rounded-full">
            {{ tag.name }}
          </Tag>
          <span v-if="!text?.length" class="text-gray-400 text-sm">--</span>
        </div>
      </template>

      <template #evalWorkload="{text}">
        {{ text || '--' }}
      </template>

      <template #actualWorkload="{text}">
        {{ text || '--' }}
      </template>
    </Grid>
  </div>
</template>

<style scoped>
:deep(.case-basic-info-grid) {
  .ant-descriptions-item-label {
    @apply text-gray-600 font-medium;
  }

  .ant-descriptions-item-content {
    @apply text-gray-900;
  }
}
</style>
