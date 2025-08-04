<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { Colon, Dropdown, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { useRouter } from 'vue-router';
import { Button } from 'ant-design-vue';
import { appContext } from '@xcan-angus/infra';

interface Props {
  loading: boolean;
  selectedNum?: number;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
});

const router = useRouter();
type OrderByKey = string;
type OrderSortKey = 'ASC' | 'DESC';

const emits = defineEmits<{(e: 'change', value: {
  orderBy?: string;
  orderSort?: 'ASC'|'DESC';
  filters: {key: string; op: string; value: string|string[]}[];
}):void,
 (e: 'refresh'):void;
  (e: 'toBatchDelete'):void;
  (e: 'toImport'):void;
  (e: 'toExport'):void;
  (e: 'toCancelBatchDelete'):void}>();
const userInfo = ref(appContext.getUser());

const searchPanelRef = ref();
const selectedMenuMap = ref<{[key: string]: boolean}>({});

const buttonDropdownMenuItems = [
  {
    name: '文件提取变量',
    key: 'file',
    noAuth: true
  },
  {
    name: 'Http提取变量',
    key: 'http',
    noAuth: true
  },
  {
    name: 'Jdbc提取变量',
    key: 'jdbc',
    noAuth: true
  }
];

const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: '查询名称、描述',
    allowClear: true,
    maxlength: 100
  },
  // {
  //   valueKey: 'status',
  //   type: 'select-enum',
  //   enumKey: 'FuncReviewStatus',
  //   placeholder: '选择状态',
  //   allowClear: true
  // },
  {
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true,
    placeholder: '选择创建人',
    maxlength: 100
  },
  {
    valueKey: 'createdDate',
    type: 'date-range',
    allowClear: true,
    placeholder: ['创建时间从', '创建时间到'],
    maxlength: 100
  }
];

const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: OrderSortKey;
}[] = [
  {
    name: '按名称',
    key: 'name',
    orderSort: 'DESC'
  },
  {
    name: '按添加人',
    key: 'createdBy',
    orderSort: 'ASC'
  },
  {
    name: '按添加时间',
    key: 'createdDate',
    orderSort: 'ASC'
  },
  {
    name: '按最后修改人',
    key: 'lastModifiedBy',
    orderSort: 'ASC'
  },
  {
    name: '按最后修改时间',
    key: 'lastModifiedDate',
    orderSort: 'DESC'
  }
];

const menuItems = computed(() => [
  {
    key: '',
    name: '全部'
  },
  {
    key: 'createdBy',
    name: '我创建的'
  },
  {
    key: 'lastModifiedBy',
    name: '我修改的'
  },
  {
    key: 'lastDay',
    name: '近1天'
  },
  {
    key: 'lastThreeDays',
    name: '近3天'
  },
  {
    key: 'lastWeek',
    name: '近7天'
  }
]);

const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const quickSearchFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const assocFilters = ref<{key: string; op: string; value: string|string[]}[]>([]);
const assocKeys = ['createdBy', 'lastModifiedBy', 'createdDate'];
const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];
const establishedKeys = ['established=1', 'established=0'];

const formatDateString = (key: string) => {
  let startDate: Dayjs | undefined;
  let endDate: Dayjs | undefined;

  if (key === 'lastDay') {
    startDate = dayjs().startOf('date');
    endDate = dayjs();
  }

  if (key === 'lastThreeDays') {
    startDate = dayjs().startOf('date').subtract(3, 'day').add(1, 'day');
    endDate = dayjs();
  }

  if (key === 'lastWeek') {
    startDate = dayjs().startOf('date').subtract(1, 'week').add(1, 'day');
    endDate = dayjs();
  }

  return [startDate ? startDate.format('YYYY-MM-DD HH:mm:ss') : '', endDate ? endDate.format('YYYY-MM-DD HH:mm:ss') : ''];

  // return [
  //   startDate ? {
  //     value: startDate.format('YYYY-MM-DD HH:mm:ss'),
  //     op: 'GREATER_THAN_EQUAL',
  //     key: 'createdDate'
  //   } : '',
  //   endDate ? {
  //     value: endDate.format('YYYY-MM-DD HH:mm:ss'),
  //     op: 'LESS_THAN_EQUAL',
  //     key: 'createdDate'
  //   }  : ''].filter(Boolean);
};

const getParams = () => {
  return {
    filters: [
      ...quickSearchFilters.value,
      ...searchFilters.value,
      ...assocFilters.value
    ],
    orderBy: orderBy.value,
    orderSort: orderSort.value
  };
};

const searchChange = (data: {key: string; op: string; value: string|string[]}[]) => {
  searchFilters.value = data.filter(item => !assocKeys.includes(item.key));
  assocFilters.value = data.filter(item => assocKeys.includes(item.key));

  if (!assocFilters.value.length) {
    assocKeys.forEach(i => {
      if (i === 'createdDate') {
        timeKeys.forEach(t => delete selectedMenuMap.value[t]);
      } else {
        delete selectedMenuMap.value[i];
      }
    });
  } else {
    assocKeys.forEach(key => {
      if (key === 'createdBy' || key === 'lastModifiedBy') {
        const filterItem = assocFilters.value.find(i => i.key === key);
        if (!filterItem || filterItem.value !== userInfo.value?.id) {
          delete selectedMenuMap.value[key];
        }
      }
      if (key === 'createdDate') {
        const filterItem = assocFilters.value.filter(i => i.key === key);
        const timeKey = timeKeys.find(t => selectedMenuMap.value[t]);
        if (timeKey) {
          const timeValue = formatDateString(timeKey);
          if (timeValue[0] !== filterItem[0].value || timeValue[1] !== filterItem[1].value) {
            delete selectedMenuMap.value[timeKey];
          }
        }
      }
    });
  }

  emits('change', getParams());
};
const toSort = (sortData) => {
  orderBy.value = sortData.orderBy;
  orderSort.value = sortData.orderSort;
  emits('change', getParams());
};

const menuItemClick = (data) => {
  const key = data.key;
  // const statusTypeKeys = planStatusTypeOpt.value.map(i => i.key);
  let searchChangeFlag = false;
  if (selectedMenuMap.value[key]) {
    delete selectedMenuMap.value[key];
    if (timeKeys.includes(key) && assocKeys.includes('createdDate')) {
      searchPanelRef.value.setConfigs([
        { valueKey: 'createdDate', value: undefined }
      ]);
      searchChangeFlag = true;
    } else if (assocKeys.includes(key)) {
      searchPanelRef.value.setConfigs([
        { valueKey: key, value: undefined }
      ]);
      searchChangeFlag = true;
    }
  } else {
    if (key === '') {
      selectedMenuMap.value = { '': true };
      quickSearchFilters.value = [];
      // 清空搜索面板
      if (typeof searchPanelRef.value?.clear === 'function') {
        searchPanelRef.value.clear();
        searchChangeFlag = true;
      }
    } else {
      delete selectedMenuMap.value[''];
    }
    if (timeKeys.includes(key)) {
      timeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
      selectedMenuMap.value[key] = true;
    } else if (establishedKeys.includes(key)) {
      establishedKeys.forEach(statusKey => delete selectedMenuMap.value[statusKey]);
      selectedMenuMap.value[key] = true;
    } else {
      selectedMenuMap.value[key] = true;
    }
  }
  const userId = userInfo.value?.id;
  const timeFilters: {key: string; op: string; value: string}[] = [];
  const assocFiltersInQuick = [];
  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      // timeFilters = formatDateString(key);
      assocFiltersInQuick.push({ valueKey: 'createdDate', value: formatDateString(key) });
      return undefined;
    } else if (assocKeys.includes(key)) {
      if (key === 'createdBy' || key === 'lastModifiedBy') {
        assocFiltersInQuick.push({ valueKey: key, value: userId });
      }
      return undefined;
    } else {
      return {
        key,
        op: 'EQUAL',
        value: userId
      };
    }
  }).filter(Boolean);
  quickSearchFilters.value.push(...timeFilters);
  if (assocFiltersInQuick.length) {
    searchPanelRef.value.setConfigs([
      ...assocFiltersInQuick
    ]);
    searchChangeFlag = true;
  }
  if (!searchChangeFlag) {
    emits('change', getParams());
  }
};

const refresh = () => {
  emits('refresh');
};

const buttonDropdownClick = ({ key }: { key: 'file' | 'http' | 'jdbc' }) => {
  if (key === 'file') {
    router.push('/data#variables?source=FILE');
    return;
  }

  if (key === 'http') {
    router.push('/data#variables?source=HTTP');
    return;
  }

  if (key === 'jdbc') {
    router.push('/data#variables?source=JDBC');
  }
};

const toCreateStaticVariable = () => {
  router.push('/data#variables?source=STATIC');
};

const toBatchDelete = () => {
  emits('toBatchDelete');
};

const toImport = () => {
  emits('toImport');
};

const toExport = () => {
  emits('toExport');
};

const toCancelBatchDelete = () => {
  emits('toCancelBatchDelete');
};

onMounted(() => {
  // loadStatusEnum();
});
</script>
<template>
  <div class="mt-2.5 mb-3.5">
    <div class="flex">
      <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
        <span>快速查询</span>
        <Colon />
      </div>
      <div class="flex  flex-wrap ml-2">
        <div
          v-for="item in menuItems"
          :key="item.key"
          :class="{ 'active-key': selectedMenuMap[item.key] }"
          class="px-2.5 h-6 leading-6 mr-3 mb-3 rounded bg-gray-light cursor-pointer"
          @click="menuItemClick(item)">
          {{ item.name }}
        </div>
      </div>
    </div>
    <div class="flex items-start justify-between ">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchPanelOptions"
        class="flex-1 mr-3.5"
        @change="searchChange" />

      <div class="flex items-center space-x-3">
        <template v-if="typeof props.selectedNum === 'number'">
          <Button
            danger
            size="small"
            class="flex items-center flex-shrink-0"
            @click="toBatchDelete">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <div class="flex items-center">
              <span class="mr-0.5">删除选中</span>
              <span>({{ selectedNum }})</span>
            </div>
          </Button>

          <Button
            size="small"
            class="flex items-center flex-shrink-0"
            @click="toCancelBatchDelete">
            <Icon icon="icon-fanhui" class="mr-1" />
            <span>取消删除</span>
          </Button>
        </template>

        <template v-else>
          <Button
            type="primary"
            size="small"
            class="flex items-center flex-shrink-0 pr-0"
            @click="toCreateStaticVariable">
            <div class="flex items-center">
              <Icon icon="icon-jia" class="text-3.5" />
              <span class="ml-1">添加静态变量</span>
            </div>
            <Dropdown :menuItems="buttonDropdownMenuItems" @click="buttonDropdownClick">
              <div class="w-5 h-5 flex items-center justify-center">
                <Icon icon="icon-more" />
              </div>
            </Dropdown>
          </Button>

          <Button
            type="default"
            size="small"
            class="flex items-center flex-shrink-0"
            @click="toBatchDelete">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <span>批量删除</span>
          </Button>

          <IconRefresh
            :loading="props.loading"
            :disabled="props.loading"
            @click="refresh">
            <template #default>
              <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
                <Icon icon="icon-shuaxin" class="text-3.5" />
                <span class="ml-1">刷新</span>
              </div>
            </template>
          </IconRefresh>

          <Tooltip
            arrowPointAtCenter
            placement="topLeft"
            title="上传变量">
            <Icon
              icon="icon-shangchuan"
              class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0"
              @click="toImport" />
          </Tooltip>

          <Tooltip
            arrowPointAtCenter
            placement="topLeft"
            title="下载变量">
            <Icon
              icon="icon-daochu1"
              class="text-4 cursor-pointer text-theme-content text-theme-text-hover flex-shrink-0"
              @click="toExport()" />
          </Tooltip>
        </template>
      </div>
    </div>
  </div>
</template>
<style scoped>
.active-key {
  background-color: #4ea0fd;
  color: #fff;
}
</style>
