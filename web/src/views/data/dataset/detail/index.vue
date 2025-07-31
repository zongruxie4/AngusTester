<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { AsyncComponent, modal, notification, Spin } from '@xcan-angus/vue-ui';
import { toClipboard, utils } from '@xcan-angus/infra';
import { dataSet } from '@/api/tester';

import { DataSetItem } from '../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  visible: boolean;
  data: {
    _id: string;
    id: string | undefined;
    source: 'STATIC' | 'FILE' | 'JDBC' | undefined;
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  visible: false,
  data: undefined
});

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));

const StaticDataSet = defineAsyncComponent(() => import('@/views/data/dataset/detail/staticDataset/index.vue'));
const FileDataSet = defineAsyncComponent(() => import('@/views/data/dataset/detail/fileDataset/index.vue'));
const JdbcDataSet = defineAsyncComponent(() => import('@/views/data/dataset/detail/jdbcDataset/index.vue'));
const ExportDataSetModal = defineAsyncComponent(() => import('@/views/data/dataset/export/index.vue'));

const loading = ref(false);
const loaded = ref(false);
const dataSource = ref<DataSetItem>();

const exportDataSetModalVisible = ref(false);
const exportDataSetId = ref<string>();

const ok = (data: DataSetItem, isEdit = false) => {
  const { id, name } = data;
  if (!isEdit) {
    const _id = props.data?._id;
    replaceTabPane(_id, { _id: id, uiKey: id, name, data: { _id: id, id } });
  } else {
    updateTabPane({ _id: id, name, data: { id } });

    // 更新数据名称
    if (dataSource.value) {
      dataSource.value.name = data.name;
    }
  }

  nextTick(() => {
    updateTabPane({ _id: 'dataSetList', notify: utils.uuid() });
  });
};

const toDelete = () => {
  const data = dataSource.value;
  if (!data) {
    return;
  }

  modal.confirm({
    content: `确定删除数据集【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const [error] = await dataSet.deleteDataSet([id]);
      if (error) {
        return;
      }

      notification.success('数据集删除成功');
      deleteTabPane([id]);

      nextTick(() => {
        updateTabPane({ _id: 'dataSetList', notify: utils.uuid() });
      });
    }
  });
};

const toExport = (id: string) => {
  exportDataSetModalVisible.value = true;
  exportDataSetId.value = id;
};

const toClone = async () => {
  const data = dataSource.value;
  if (!data) {
    return;
  }

  loading.value = true;
  const [error] = await dataSet.cloneDataSet([data.id]);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('数据集克隆成功');
  nextTick(() => {
    updateTabPane({ _id: 'dataSetList', notify: utils.uuid() });
  });
};

const toCopyLink = (id: string) => {
  toClipboard(window.location.origin + `/data#dataSet?id=${id}`).then(() => {
    notification.success('复制链接成功');
  }).catch(() => {
    notification.error('复制链接失败');
  });
};

const toRefresh = (id: string) => {
  loadData(id);
};

const loadData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await dataSet.getDataSetDetail(id);
  loading.value = false;
  loaded.value = true;
  if (error) {
    return;
  }

  const data = res?.data as DataSetItem;
  if (!data) {
    return;
  }

  dataSource.value = data;
  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id });
  }
};

onMounted(() => {
  watch(() => props.data, (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    const oldId = oldValue?.id;
    if (id === oldId) {
      return;
    }

    loadData(id);
  }, { immediate: true });
});

const source = computed(() => {
  const data = props.data;
  if (!data) {
    return;
  }

  const { id, source } = data;
  if (id) {
    if (loaded.value) {
      const data = dataSource.value;
      if (data) {
        const extraction = data.extraction;
        if (!extraction) {
          return 'STATIC';
        }

        return extraction.source;
      }

      return undefined;
    }

    return undefined;
  }

  return source;
});
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="max-w-242.5">
      <AsyncComponent :visible="source === 'STATIC'">
        <StaticDataSet
          :projectId="props.projectId"
          :dataSource="dataSource"
          @ok="ok"
          @delete="toDelete"
          @export="toExport"
          @clone="toClone"
          @copyLink="toCopyLink"
          @refresh="toRefresh" />
      </AsyncComponent>

      <AsyncComponent :visible="source === 'FILE'">
        <FileDataSet
          :projectId="props.projectId"
          :dataSource="dataSource"
          @ok="ok"
          @delete="toDelete"
          @export="toExport"
          @clone="toClone"
          @copyLink="toCopyLink"
          @refresh="toRefresh" />
      </AsyncComponent>

      <AsyncComponent :visible="source === 'JDBC'">
        <JdbcDataSet
          :projectId="props.projectId"
          :dataSource="dataSource"
          @ok="ok"
          @delete="toDelete"
          @export="toExport"
          @clone="toClone"
          @copyLink="toCopyLink"
          @refresh="toRefresh" />
      </AsyncComponent>
    </div>
  </Spin>

  <AsyncComponent :visible="exportDataSetModalVisible">
    <ExportDataSetModal
      :id="exportDataSetId"
      v-model:visible="exportDataSetModalVisible"
      :projectId="props.projectId" />
  </AsyncComponent>
</template>
