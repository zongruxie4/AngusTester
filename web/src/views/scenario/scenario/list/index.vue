<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Dropdown, DropdownGroup, DropdownSort, Icon, NoData, SearchPanel, Spin } from '@xcan-angus/vue-ui';
import { utils, ScriptType } from '@xcan-angus/infra';
import { scenario } from '@/api/tester';

import { GroupedKey, SceneInfo } from './PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

type SortKey = 'createdDate' | 'name' | 'createdByName';
type OrderSortKey = 'ASC' | 'DESC';
type FilterItem = { key: string; op: string; value: string; };

const Drawer = defineAsyncComponent(() => import('@/views/scenario/scenario/list/drawer/index.vue'));
const SceneList = defineAsyncComponent(() => import('./list.vue'));
const SceneGroup = defineAsyncComponent(() => import('./group.vue'));

// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });
const deleteTabPane = inject<(data: string[]) => void>('deleteTabPane', () => { });

const loaded = ref(false);
const loading = ref(false);
const errorMessage = ref<string>();

const groupedKey = ref<GroupedKey>('none');
const orderBy = ref<string>('createdDate');
const orderSort = ref<OrderSortKey>('DESC');
const filters = ref<FilterItem[]>();

const dataList = ref<SceneInfo[]>([]);

const searchChange = (value: FilterItem[]): void => {
  filters.value = value;
  loadData();
};

const createHttpScene = () => {
  const name = 'scene' + new Date().getTime();
  addTabPane({ name, _id: utils.uuid(), value: 'Http', sceneInfo: { name, projectId: props.projectId } });
};

const buttonDropdownClick = ({ key }: { key: 'Jdbc' | 'WebSocket' | 'Ftp' | 'Ldap' | 'Mail' | 'Smtp' | 'Tcp' }) => {
  const name = 'scene' + new Date().getTime();
  const id = utils.uuid();
  const sceneInfo = { name, projectId: props.projectId };

  if (key === 'Jdbc') {
    addTabPane({ name, _id: id, value: 'Jdbc', sceneInfo });
    return;
  }

  if (key === 'Ftp') {
    addTabPane({ name, _id: id, value: 'Ftp', sceneInfo });
    return;
  }

  if (key === 'Ldap') {
    addTabPane({ name, _id: id, value: 'Ldap', sceneInfo });
    return;
  }

  if (key === 'Mail') {
    addTabPane({ name, _id: id, value: 'Mail', sceneInfo });
    return;
  }

  if (key === 'Smtp') {
    addTabPane({ name, _id: id, value: 'Smtp', sceneInfo });
    return;
  }

  if (key === 'Tcp') {
    addTabPane({ name, _id: id, value: 'Tcp', sceneInfo });
    return;
  }

  if (key === 'WebSocket') {
    addTabPane({ name, _id: id, value: 'WebSocket', sceneInfo });
  }
};

const addSceneAuthorize = () => {
  addTabPane({ name: '场景权限', _id: utils.uuid(), value: 'authorization' });
};

const sortHandler = (value: {
  orderBy: SortKey;
  orderSort: 'DESC' | 'ASC';
}): void => {
  orderBy.value = value.orderBy;
  orderSort.value = value.orderSort;
  loadData();
};

const groupingHandler = (value: GroupedKey): void => {
  groupedKey.value = value;
};

const refreshHandler = (): void => {
  loadData();
};

const cloneHandler = (): void => {
  loadData();
};

const deleteScenario = (scenaridId: string):void => {
  deleteTabPane([scenaridId, scenaridId + '-detail']);
};

const loadData = async (): Promise<void> => {
  const params: {
    pageNo: number;
    pageSize: number;
    projectId: string;
    infoScope: 'DETAIL';
    filters?: FilterItem[];
    orderSort?: string;
    orderBy?: string;
  } = {
    projectId: props.projectId,
    pageNo: 1,
    pageSize: 2000,
    infoScope: 'DETAIL'
  };

  if (filters.value?.length) {
    params.filters = filters.value;
  }

  if (orderBy.value) {
    params.orderBy = orderBy.value;
  }

  if (orderSort.value) {
    params.orderSort = orderSort.value;
  }

  loading.value = true;
  const [error, res] = await scenario.getScenarioList(params);
  loaded.value = true;
  loading.value = false;
  errorMessage.value = undefined;
  if (error) {
    dataList.value = [];
    errorMessage.value = error.message;
    return;
  }

  const data = res?.data || { total: 0, list: [] };
  dataList.value = data.list.map((item) => {
    return {
      ...item,
      nameLinkUrl: `/scenario#scenario?id=${item.id}&name=${item.name}&plugin=${item.plugin}`,
      detailLink: `/scenario#scenario?id=${item.id}&name=${item.name}&plugin=${item.plugin}&type=detail`
    };
  });
};

onMounted(() => {
  watch(() => props.projectId, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  });
});

const searchOptions = [
  {
    type: 'input',
    placeholder: '查询场景ID、名称、描述',
    valueKey: 'name',
    allowClear: true,
    maxlength: 100,
    trim: true
  },
  {
    type: 'input',
    placeholder: '插件类型',
    valueKey: 'plugin',
    allowClear: true,
    trim: true,
    op: '  EQUAL',
    maxlength: 100,
    trimAll: true
  },
  {
    type: 'select-enum',
    placeholder: '选择脚本类型',
    valueKey: 'scriptType',
    allowClear: true,
    enumKey: ScriptType,
    excludes: ({ value }) => ['MOCK_DATA', 'MOCK_APIS'].includes(value)
  },
  {
    type: 'date-range',
    showTime: true,
    placeholder: ['添加时间开始', '添加时间结束'],
    valueKey: 'createdDate',
    valueType: 'multiple',
    allowClear: true
  }
];

const buttonDropdownMenuItems = [
  {
    name: 'Jdbc场景',
    key: 'Jdbc',
    noAuth: true
  },
  {
    name: 'WebSocket场景',
    key: 'WebSocket',
    noAuth: true
  },
  {
    name: 'Ftp场景',
    key: 'Ftp',
    noAuth: true
  },
  {
    name: 'Ldap场景',
    key: 'Ldap',
    noAuth: true
  },
  {
    name: 'Mail场景',
    key: 'Mail',
    noAuth: true
  },
  {
    name: 'Smtp场景',
    key: 'Smtp',
    noAuth: true
  },
  {
    name: 'Tcp场景',
    key: 'Tcp',
    noAuth: true
  }
];

const sortMenuItems: {
  name: string;
  key: SortKey;
  orderSort: 'DESC' | 'ASC';
}[] = [
  {
    name: '按添加时间',
    key: 'createdDate',
    orderSort: 'DESC'
  }, {
    name: '按名称',
    key: 'name',
    orderSort: 'ASC'
  }, {
    name: '按添加人',
    key: 'createdByName',
    orderSort: 'ASC'
  }
];

const groupingMenuItems: {
  key: GroupedKey;
  name: string;
}[] = [
  {
    key: 'none',
    name: '不分组'
  },
  {
    key: 'createdBy',
    name: '按添加人分组'
  },
  {
    key: 'plugin',
    name: '按插件分组'
  },
  {
    key: 'scriptType',
    name: '按脚本类型分组'
  }
];
</script>

<template>
  <div class="flex h-full ">
    <Spin class="w-full h-full flex flex-col py-5" :spinning="loading">
      <div class="flex-shrink-0 flex items-start text-3 px-5 mb-3.5 space-x-3">
        <SearchPanel
          class="flex-1"
          :options="searchOptions"
          @change="searchChange" />

        <div class="flex-shrink-0 flex items-center flex-nowrap whitespace-nowrap leading-7 space-x-3.5">
          <Button
            class="flex-shrink-0 flex items-center pr-0"
            type="primary"
            size="small"
            @click="createHttpScene">
            <div class="flex items-center">
              <Icon icon="icon-jia" class="text-3.5" />
              <span class="ml-1">添加Http场景</span>
            </div>
            <Dropdown :menuItems="buttonDropdownMenuItems" @click="buttonDropdownClick">
              <div class="w-5 h-5 flex items-center justify-center">
                <Icon icon="icon-more" />
              </div>
            </Dropdown>
          </Button>

          <div
            class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover"
            @click="addSceneAuthorize">
            <Icon icon="icon-quanxian1" />
            <span>场景权限</span>
          </div>

          <DropdownSort
            v-model:orderBy="orderBy"
            v-model:orderSort="orderSort"
            :menuItems="sortMenuItems"
            @click="sortHandler">
            <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
              <Icon icon="icon-shunxu" />
              <span>排序</span>
            </div>
          </DropdownSort>

          <DropdownGroup
            :activeKey="groupedKey"
            :menuItems="groupingMenuItems"
            @click="groupingHandler">
            <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
              <Icon icon="icon-fenzu" />
              <span>分组</span>
            </div>
          </DropdownGroup>

          <div
            class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover"
            @click="refreshHandler">
            <Icon icon="icon-shuaxin" />
            <span>刷新</span>
          </div>
        </div>
      </div>

      <template v-if="loaded">
        <NoData
          v-if="dataList.length === 0"
          style="height: calc(100% - 42px);"
          :text="errorMessage" />

        <template v-else>
          <AsyncComponent :visible="groupedKey !== 'none'">
            <SceneGroup
              v-show="groupedKey !== 'none'"
              :dataSource="dataList"
              :notify="props.notify"
              :userInfo="props.userInfo"
              :appInfo="props.appInfo"
              :projectId="props.projectId"
              :groupedKey="groupedKey" />
          </AsyncComponent>

          <AsyncComponent :visible="groupedKey === 'none'">
            <SceneList
              v-show="groupedKey === 'none'"
              :dataSource="dataList"
              :notify="props.notify"
              :userInfo="props.userInfo"
              :appInfo="props.appInfo"
              :projectId="props.projectId"
              class="px-5 flex-1 overflow-auto"
              @clone="cloneHandler"
              @delete="deleteScenario" />
          </AsyncComponent>
        </template>
      </template>
    </Spin>

    <Drawer :projectId="props.projectId" :userInfo="props.userInfo" />
  </div>
</template>
