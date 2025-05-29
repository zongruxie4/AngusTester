<script setup lang="ts">
import { defineAsyncComponent, inject, nextTick, onMounted, reactive, ref } from 'vue';
import { useRoute } from 'vue-router';
import { DropdownSort, Icon, IconRefresh, NoData, SearchPanel, Spin } from '@xcan-angus/vue-ui';
import { site, utils, enumLoader } from '@xcan-angus/tools';
import { Button, Pagination, Switch } from 'ant-design-vue';

import { sortOpt } from './interface';
import { pubStore } from '@/api/store';
import { node } from 'src/api/tester';
import { pureParams } from '@/utils/common';

const NodeItems = defineAsyncComponent(() => import('./components/nodeItems/index.vue'));
const NodeTip = defineAsyncComponent(() => import('@/views/config/node/components/nodeTip/index.vue'));

const isAdmin = inject('isAdmin', ref(false));

const state = reactive<{nodeList: Array<Record<string, any>>}>({
  nodeList: []
});

const route = useRoute();
const nodeListRef = ref();
const editionType = ref<string>();
const disabledRoles = ['MANAGEMENT', 'CONTROLLER'];
const loading = ref(false); // loading list
const autoRefresh = ref(false); // 是否开启自动刷新磁盘数据

// 保存参数
const params = reactive<{filters: {value: string, op: string, key: string}[], orderBy?: string, orderSort?: string}>({
  filters: []
});

const searchOpt = [
  {
    type: 'input',
    allowClear: true,
    valueKey: 'name',
    label: '节点',
    placeholder: '查询节点名称、ID',
    maxlength: 100
  },
  {
    type: 'input',
    allowClear: true,
    valueKey: 'ip',
    label: 'IP',
    placeholder: '查询节点IP',
    op: 'EQUAL',
    maxlength: 100
  },
  {
    type: 'select-enum',
    allowClear: true,
    valueKey: 'role',
    enumKey: 'NodeRole',
    label: '角色',
    op: 'IN',
    placeholder: '选择角色'
  },
  {
    type: 'select-user',
    allowClear: true,
    valueKey: 'createdBy',
    label: '添加人',
    placeholder: '选择添加人',
    maxlength: 100
  },
  {
    type: 'select-enum',
    allowClear: true,
    valueKey: 'source',
    enumKey: 'NodeSource',
    label: '来源',
    placeholder: '选择来源'
  }
];

const pagination = reactive({
  current: 1,
  pageSize: 5,
  showTotal: total => `共${total}条`,
  showSizeChanger: true,
  total: 0
});

const handlePageChange = (page) => {
  pagination.current = page;
  handleRefreshList();
};

// 获取 list 数据
const loadList = async () => {
  const { pageSize, current } = pagination;
  const param = pureParams({
    ...params,
    pageSize,
    pageNo: current
  });
  loading.value = true;
  const [error, res] = await node.loadList(param);
  loading.value = false;
  if (error) {
    return;
  }
  state.nodeList = res.data.list.map(i => {
    let standard = '';
    if (i.spec) {
      // standard = `${i.spec.cpu} / ${i.spec.memory} / ${i.spec.disk} / ${i.spec.network || ''}`;
      standard = i.spec.showLabel;
    }
    return { ...i, editable: false, sourceName: i.source.message, spec: { ...i.spec }, standard, monitorData: {} };
  });
  pagination.total = +res.data.total;
};

const roleOptions = ref<{name: string, label: string, value: string, message: string, disabled?: boolean}[]>([]);

const loadEnums = async () => {
  const [error, data] = await enumLoader.load('NodeRole');
  if (error) {
    return;
  }
  roleOptions.value = (data || []).map(i => ({ ...i, label: i.message, disabled: disabledRoles.includes(i.value) }));
};

const changeForm = (val) => {
  params.filters = val;
  handleSearch();
};

const handleSearch = () => {
  pagination.current = 1;
  handleRefreshList();
};

const gotoBuy = async () => {
  const [error, res] = await pubStore.getGoodsVersion('CloudNode');
  if (error) {
    return;
  }
  const allVersions = (res.data || []).map(i => i.version);
  const maxVersion = utils.maxVersion(allVersions);
  if (maxVersion) {
    const targetGoods = (res.data || []).find(goods => goods.version === maxVersion);
    window.open(targetGoods.pricingUrl);
  }
};

const sort = ({ orderBy, orderSort }) => {
  params.orderBy = orderBy;
  params.orderSort = orderSort;
  handleRefreshList();
};

const handleRefreshList = async () => {
  await loadList();
};

const handleAdd = () => {
  nodeListRef.value.add();
};

const deleteItem = ():void => {
  // state.nodeList.shift();
};

const searchPanelRef = ref();
onMounted(async () => {
  const envContent = await site.getEnvContent();
  editionType.value = envContent?.VITE_EDITION_TYPE;
  loadEnums();
  if (route.query.id) {
    nextTick(() => {
      searchPanelRef.value.setConfigs([{ valueKey: 'name', value: route.query.id }]);
    });
  } else {
    handleRefreshList();
  }
});
</script>
<template>
  <div class="px-5 py-5 h-full overflow-auto">
    <NodeTip class="mb-5" />
    <div class="flex justify-between mb-3.5">
      <SearchPanel
        ref="searchPanelRef"
        class="flex-1 mr-3"
        :options="searchOpt"
        @change="changeForm" />
      <div class="flex items-center space-x-2.5">
        <!-- <ButtonAuth
          code="NodeAdd"
          type="primary"
          icon="icon-jia"
          iconStyle="font-size:14px;"
          @click="handleAdd" /> -->
        <Button
          type="primary"
          size="small"
          class="flex space-x-1"
          @click="handleAdd">
          <Icon icon="icon-jia" />
          添加节点
        </Button>
        <!-- <ButtonAuth
          v-if="editionType === 'CLOUD_SERVICE'"
          code="NodeBuy"
          icon="icon-zaixiangoumai"
          iconStyle="font-size:14px;"
          @click="gotoBuy" /> -->
        <Button
          v-if="editionType === 'CLOUD_SERVICE'"
          size="small"
          class="flex space-x-1"
          @click="gotoBuy">
          <Icon icon="icon-zaixiangoumai" />
          购买节点
        </Button>
        <div>
          <span class="text-3 mr-1">自动刷新</span>
          <Switch v-model:checked="autoRefresh" size="small"></Switch>
        </div>
        <DropdownSort
          :menuItems="sortOpt"
          :disabled="loading"
          @click="sort">
          <Button class="rounded ml-3" size="small">
            <Icon icon="icon-shunxu" class="text-3.5 mr-1" />
            <span>排序</span>
          </Button>
        </DropdownSort>
        <Button
          class="rounded ml-3"
          size="small"
          :disabled="loading"
          @click="handleRefreshList">
          <IconRefresh class="text-3.5 mr-1" :loading="loading" />
          <span>刷新</span>
        </Button>
      </div>
    </div>
    <Spin :spinning="loading" mask>
      <NodeItems
        ref="nodeListRef"
        :roleOptions="roleOptions"
        :nodeList="state.nodeList"
        :autoRefresh="autoRefresh"
        :isAdmin="isAdmin"
        @loadList="handleRefreshList"
        @cancel="deleteItem" />
      <NoData v-show="pagination.total === 0" class="mt-45" />
      <Pagination
        v-show="pagination.total > 5"
        v-bind="pagination"
        class="justify-end mb-4"
        :showSizeChanger="false"
        @change="handlePageChange">
      </Pagination>
    </Spin>
  </div>
</template>

<style>
.input-sm {
  @apply h-7;
}

.install-step {
  @apply px-3 py-1.5 my-2 leading-6;

  background-color: #f6f6f6;
}

.input-sm input {
  @apply text-3 leading-3;
}

.icon-text-3 leading-3 {
  @apply text-3 leading-3 !important;
}
</style>
