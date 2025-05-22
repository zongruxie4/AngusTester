<script lang="ts" setup>
import { computed, inject, onMounted, ref } from 'vue';
import { Colon, Icon, IconRefresh, SearchPanel, DropdownSort } from '@xcan-angus/vue-ui';
import dayjs, { Dayjs } from 'dayjs';
import { Button } from 'ant-design-vue';

interface Props {
  loading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
});

// const sortOpt = [
//   {
//     name: '按添加时间',
//     key: 'createdDate',
//     orderSort: 'DESC'
//   },
//   {
//     name: '按名称',
//     key: 'name',
//     orderSort: 'ASC'
//   }
// ];

const emits = defineEmits<{(e: 'change', value: {
  orderBy?: string;
  orderSort?: 'ASC'|'DESC';
  filters: {key: string; op: string; value: string|string[]}[];
}):void,
 (e: 'refresh'):void;
 (e: 'add'):void;
 (e: 'import'):void}>();
const tenantInfo = inject('tenantInfo', ref({ id: '' }));

const searchPanelRef = ref();
const selectedMenuMap = ref<{[key: string]: boolean}>({});

const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: '查询名称、描述',
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true,
    placeholder: '选择添加人'
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: ['添加时间从', '添加时间到'],
    showTime: true
  }
];

const menuItems = computed(() => [
  {
    key: '',
    name: '全部'
  },
  {
    key: 'createdBy',
    name: '我设计的'
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
const assocKeys = ['createdDate', 'createdBy'];
const timeKeys = ['lastDay', 'lastThreeDays', 'lastWeek'];

// // 排序
// const sort = (sorter: { orderBy: string, orderSort: string }) => {
//   orderBy.value = sorter.orderBy;
//   orderSort.value = sorter.orderSort;
//   emits('change', getParams());
// };

const formatDateString = (key: string) => { // TODO 可以提到公共工具方法
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
      if (['createdBy'].includes(key)) {
        const filterItem = assocFilters.value.find(i => i.key === key);
        if (!filterItem || filterItem.value !== tenantInfo.value?.id) {
          delete selectedMenuMap.value[key];
        }
      } else if (key === 'createdDate') {
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

const menuItemClick = (data) => {
  const key = data.key;
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
    selectedMenuMap.value[key] = true;
  }
  if (timeKeys.includes(key)) {
    timeKeys.forEach(timeKey => delete selectedMenuMap.value[timeKey]);
    selectedMenuMap.value[key] = true;
  } else {
    selectedMenuMap.value[key] = true;
  }
  const userId = tenantInfo.value?.id;
  // let timeFilters: {key: string; op: string; value: string}[] = [];
  const assocFiltersInQuick:{valueKey: string, value: string|string[]}[] = [];
  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (['lastDay', 'lastThreeDays', 'lastWeek'].includes(key)) {
      assocFiltersInQuick.push({
        valueKey: 'createdDate',
        value: formatDateString(key)
      });
      return undefined;
    } else if (assocKeys.includes(key)) {
      if (['createdBy'].includes(key)) {
        assocFiltersInQuick.push({ valueKey: key, value: userId });
      }
      return undefined;
    }
  }).filter(Boolean);
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

const add = () => {
  emits('add');
};

const importDesign = () => {
  emits('import');
}
onMounted(() => {
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
        <Button
          type="primary"
          size="small"
          @click="add">
          <Icon icon="icon-jia" class="text-3.5 mr-1" />
          <span>添加设计</span>
        </Button>

        <Button
          type="primary"
          size="small"
          @click="importDesign">
          <Icon icon="icon-shangchuan" class="text-3.5 mr-1" />
          <span>导入设计</span>
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
