<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Hints, Table, Icon, NoData, Spin } from '@xcan-angus/vue-ui';
import { dataSet } from '@/api/tester';

const { t } = useI18n();

type Props = {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const loading = ref(false);
const loaded = ref(false);
const pagination = ref<{ current: number; pageSize: number; total: number; showSizeChanger: false; }>({ current: 1, pageSize: 10, total: 0, showSizeChanger: false });
const dataList = ref<{targetId:string;targetType:string;targetName:string}[]>([]);

const refresh = () => {
  if (loading.value) {
    return;
  }

  loadData();
};

const loadData = async () => {
  loading.value = true;
  const [error, res] = await dataSet.getDataSetTarget(props.id);
  loading.value = false;
  loaded.value = true;
  if (error) {
    pagination.value.total = 0;
    dataList.value = [];
    return;
  }

  const data = res?.data || [];
  pagination.value.total = data.length;
  dataList.value = data;
};

onMounted(() => {
  watch(() => props.id, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});

const columns = [
  {
    title: t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetUseListField.resourceType'),
    dataIndex: 'targetType',
    width: '10%',
    ellipsis: true
  },
  {
    title: t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetUseListField.resourceId'),
    dataIndex: 'targetId',
    width: '25%',
    ellipsis: true
  },
  {
    title: t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetUseListField.resourceName'),
    dataIndex: 'targetName',
    ellipsis: true
  }
];
</script>

<template>
  <Spin :spinning="loading" class="text-3 leading-5">
    <div class="flex items-center justify-between mb-2">
      <Hints :text="t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetUseListField.description')" />
      <Button
        :disabled="loading"
        size="small"
        type="text"
        class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
        @click="refresh">
        <Icon icon="icon-shuaxin" class="text-3.5" />
        <span class="ml-1">{{ t('httPlugin.uiConfig.httpConfigs.parametric.dataset.datasetUseListField.refresh') }}</span>
      </Button>
    </div>

    <template v-if="loaded">
      <NoData
        v-if="!dataList.length"
        size="small"
        class="mt-5 mb-10" />

      <Table
        v-else
        rowKey="targetId"
        class="flex-1 mb-3.5"
        :dataSource="dataList"
        :columns="columns"
        :pagination="pagination">
        <template #bodyCell="{ column, record }">
          <div v-if="column.dataIndex === 'targetType'" class="truncate">
            {{ record.targetType?.message }}
          </div>
        </template>
      </Table>
    </template>
  </Spin>
</template>
<style scoped>
:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
