<script lang="ts" setup>
import { Button, Table } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import ExpandGrid from './ExpandGrid.vue';
import { useEfficiencyData } from './composables';

// Initialize composables
const { t } = useI18n();
const {
  editable,
  columns,
  data,
  toggleEditMode
} = useEfficiencyData();
</script>

<template>
  <ExpandGrid :title="t('indicator.efficiency.title')">
    <template #button>
      <div class="text-3 flex items-center">
        <template v-if="editable">
          <span class="cursor-pointer" @click.stop="toggleEditMode">
            <Icon icon="icon-zhongzhi2" class="mr-1" />{{ t('common.cancel') }}
          </span>
          <Button
            type="text"
            class="ml-2 text-3 py-0 h-5">
            <Icon icon="icon-baocun" class="mr-1" />{{ t('actions.save') }}
          </Button>
        </template>
      </div>
    </template>
    <Table
      :columns="columns"
      :dataSource="data"
      size="small"
      :pagination="false">
    </Table>
  </ExpandGrid>
</template>
