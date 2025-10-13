<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue';
import { Colon, Icon, IconRefresh, SearchPanel } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { appContext, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { LoadingProps } from '@/types/types';
import { formatDateString } from '@/utils/utils';

const { t } = useI18n();

const props = withDefaults(defineProps<LoadingProps>(), {
  loading: false
});

const emits = defineEmits<{(e: 'change', value: {
  orderBy?: string;
  orderSort?: PageQuery.OrderSort;
  filters: SearchCriteria[];
}):void,
 (e: 'refresh'):void;
 (e: 'add'):void}>();
const userInfo = ref(appContext.getUser());

const searchPanelRef = ref();
const selectedMenuMap = ref<{[key: string]: boolean}>({});

const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('common.placeholders.searchKeyword'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'createdBy',
    type: 'select-user',
    allowClear: true,
    placeholder: t('apiShare.searchPanelOptions.sharePersonPlaceholder')
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: t('apiShare.searchPanelOptions.dateRangePlaceholder'),
    showTime: true
  }
];

const menuItems = computed(() => [
  {
    key: '',
    name: t('common.all')
  },
  {
    key: 'createdBy',
    name: t('apiShare.searchPanel.menuItems.myShares')
  },
  {
    key: 'last1Day',
    name: t('quickSearch.last1Day')
  },
  {
    key: 'last3Days',
    name: t('quickSearch.last3Days')
  },
  {
    key: 'last7Days',
    name: t('quickSearch.last7Days')
  }
]);

const orderBy = ref();
const orderSort = ref();
const searchFilters = ref<SearchCriteria[]>([]);
const quickSearchFilters = ref<SearchCriteria[]>([]);
const assocFilters = ref<SearchCriteria[]>([]);
const assocKeys = ['createdDate', 'createdBy'];
const timeKeys = ['last1Day', 'last3Days', 'last7Days'];

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

const searchChange = (data: SearchCriteria[]) => {
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
        if (!filterItem || filterItem.value !== userInfo.value?.id) {
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
  const userId = userInfo.value?.id;
  // let timeFilters: {key: string; op: string; value: string}[] = [];
  const assocFiltersInQuick:{valueKey: string, value: string|string[]}[] = [];
  quickSearchFilters.value = Object.keys(selectedMenuMap.value).map(key => {
    if (key === '') {
      return undefined;
    } else if (['last1Day', 'last3Days', 'last7Days'].includes(key)) {
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
</script>

<template>
  <div class="mt-2.5 mb-3.5">
    <div class="flex">
      <div class="whitespace-nowrap text-3 text-text-sub-content transform-gpu translate-y-0.5">
        <span>{{ t('apiShare.searchPanel.title') }}</span>
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
          <span>{{ t('apiShare.searchPanel.addShare') }}</span>
        </Button>

        <IconRefresh
          :loading="props.loading"
          :disabled="props.loading"
          @click="refresh">
          <template #default>
            <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
              <Icon icon="icon-shuaxin" class="text-3.5" />
              <span class="ml-1">{{ t('actions.refresh') }}</span>
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
