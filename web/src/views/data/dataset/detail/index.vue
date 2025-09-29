<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, modal, notification, Spin } from '@xcan-angus/vue-ui';
import { toClipboard, utils, ExtractionSource } from '@xcan-angus/infra';
import { dataSet } from '@/api/tester';
import { BasicProps } from '@/types/types';

import { DataSetDetail } from '../types';

const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  visible: false,
  data: undefined
});

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));

const StaticDataSet = defineAsyncComponent(() => import('@/views/data/dataset/detail/StaticDataset.vue'));
const FileDataSet = defineAsyncComponent(() => import('@/views/data/dataset/detail/FileDataset.vue'));
const JdbcDataSet = defineAsyncComponent(() => import('@/views/data/dataset/detail/JdbcDataset.vue'));
const ExportDataSetModal = defineAsyncComponent(() => import('@/views/data/dataset/export/index.vue'));

const loading = ref(false);
const loaded = ref(false);
const dataSource = ref<DataSetDetail>();

const exportDataSetModalVisible = ref(false);
const exportDataSetId = ref<string>();

const ok = (data: DataSetDetail, isEdit = false) => {
  const { id, name } = data;
  if (!isEdit) {
    const _id = props.data?._id;
    replaceTabPane(_id, { _id: id, uiKey: id, name, data: { _id: id, id } });
  } else {
    updateTabPane({ _id: id, name, data: { id } });

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
    content: t('actions.tips.confirmDelete', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await dataSet.deleteDataSet([id]);
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
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

  notification.success(t('actions.tips.cloneSuccess'));
  nextTick(() => {
    updateTabPane({ _id: 'dataSetList', notify: utils.uuid() });
  });
};

const toCopyLink = (id: string) => {
  toClipboard(window.location.origin + `/data#dataSet?id=${id}`).then(() => {
    notification.success(t('actions.tips.copyLinkSuccess'));
  }).catch(() => {
    notification.error(t('actions.tips.copyFail'));
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

  const data = res?.data as DataSetDetail;
  if (!data) {
    return;
  }

  dataSource.value = data;
  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id });
  }
};

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
          return ExtractionSource.VALUE;
        }
        return extraction.source;
      }
      return undefined;
    }
    return undefined;
  }
  return source;
});

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
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="max-w-242.5">
      <AsyncComponent :visible="source === ExtractionSource.VALUE">
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

      <AsyncComponent :visible="source === ExtractionSource.FILE">
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

      <AsyncComponent :visible="source === ExtractionSource.JDBC">
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
