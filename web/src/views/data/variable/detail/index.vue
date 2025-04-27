<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { AsyncComponent, modal, notification, Spin } from '@xcan-angus/vue-ui';
import { clipboard, http, utils, TESTER } from '@xcan-angus/tools';

import { VariableItem } from '../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  visible: boolean;
  data: {
    _id: string;
    id: string | undefined;
    source: 'STATIC' | 'FILE' | 'http' | 'JDBC' | undefined;
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

const StaticVariable = defineAsyncComponent(() => import('@/views/data/variable/detail/staticVariable/index.vue'));
const FileVariable = defineAsyncComponent(() => import('@/views/data/variable/detail/fileVariable/index.vue'));
const HttpVariable = defineAsyncComponent(() => import('@/views/data/variable/detail/httpVariable/index.vue'));
const JdbcVariable = defineAsyncComponent(() => import('@/views/data/variable/detail/jdbcVariable/index.vue'));
const ExportVariables = defineAsyncComponent(() => import('@/views/data/variable/export/index.vue'));

const loading = ref(false);
const loaded = ref(false);
const dataSource = ref<VariableItem>();

const exportModalVisible = ref(false);
const exportId = ref<string>();

const ok = (data: VariableItem, isEdit = false) => {
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
    updateTabPane({ _id: 'variableList', notify: utils.uuid() });
  });
};

const toDelete = () => {
  const data = dataSource.value;
  if (!data) {
    return;
  }

  modal.confirm({
    content: `确定删除变量【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const [error] = await http.del(`${TESTER}/variable`, { ids: [id] });
      if (error) {
        return;
      }

      notification.success('变量删除成功');
      deleteTabPane([id]);

      nextTick(() => {
        updateTabPane({ _id: 'variableList', notify: utils.uuid() });
      });
    }
  });
};

const toExport = (id: string) => {
  exportModalVisible.value = true;
  exportId.value = id;
};

const toClone = async () => {
  const data = dataSource.value;
  if (!data) {
    return;
  }

  loading.value = true;
  const [error] = await http.post(`${TESTER}/variable/clone`, [data.id]);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('变量克隆成功');
  nextTick(() => {
    updateTabPane({ _id: 'variableList', notify: utils.uuid() });
  });
};

const toCopyLink = (id: string) => {
  clipboard.toClipboard(window.location.origin + `/data#variables?id=${id}`).then(() => {
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
  const [error, res] = await http.get(`${TESTER}/variable/${id}`);
  loading.value = false;
  loaded.value = true;
  if (error) {
    notification.error(error.message);
    deleteTabPane([id]);
    return;
  }

  const data = res?.data as VariableItem;
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
        <StaticVariable
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
        <FileVariable
          :projectId="props.projectId"
          :dataSource="dataSource"
          @ok="ok"
          @delete="toDelete"
          @export="toExport"
          @clone="toClone"
          @copyLink="toCopyLink"
          @refresh="toRefresh" />
      </AsyncComponent>

      <AsyncComponent :visible="source === 'HTTP'">
        <HttpVariable
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
        <JdbcVariable
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

  <AsyncComponent :visible="exportModalVisible">
    <ExportVariables
      :id="exportId"
      v-model:visible="exportModalVisible"
      :projectId="props.projectId" />
  </AsyncComponent>
</template>
