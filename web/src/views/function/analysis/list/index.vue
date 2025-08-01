<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { Icon, modal, notification } from '@xcan-angus/vue-ui';
import { TemplateIconConfig } from './PropTypes';
import { Button, Tag } from 'ant-design-vue';
import { debounce, throttle } from 'throttle-debounce';
import { analysis } from '@/api/tester';

import Introduce from '@/views/function/analysis/list/introduce/index.vue';
import SearchPanel from '@/views/function/analysis/list/searchPanel/index.vue';

interface Props {
  projectId: string;
  userInfo: { id: string };
  refreshNotify?: number;
  onShow?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: () => ({ id: '' }),
  refreshNotify: 0,
  onShow: false
});

const dataListWrapRef = ref();

const TemplateSelectList = defineAsyncComponent(() => import('@/views/function/analysis/list/templateSelectList/index.vue'));
const addTabPane = inject('addTabPane', (value) => value);

const pagination = {
  pageNo: 1,
  pageSize: 20,
  total: 0
};

const templateDesc = ref<{ value: string; message: string }[]>([]);
const templateData = ref<{ value: string; message: string }[]>([]);
const filters = ref<{ op: string; key: string; value: any }[]>([]);
const orderData = ref({
  orderBy: undefined,
  orderSort: undefined
});
const template = ref('');
const dataList = ref([]);
const loading = ref(false);

const getTemplateName = () => {
  return templateData.value.find(item => item.value === template.value)?.message;
};
const TemplateDescConfig = computed(() => {
  const res = {};
  templateDesc.value.forEach(item => {
    res[item.value] = item.message;
  });
  return res;
});

const getParams = () => {
  const { pageNo, pageSize } = pagination;
  return {
    pageNo,
    pageSize,
    filters: filters.value,
    template: template.value || undefined,
    projectId: props.projectId,
    resource: 'CASE',
    ...orderData.value
  };
};

const handleSearch = (data) => {
  pagination.pageNo = 1;
  filters.value = data;
  loadAnalysisList();
};

const addAnalysis = (template = '') => {
  if (template) {
    addTabPane({ _id: 'analysisEdit', value: 'analysisEdit', name: '添加分析', data: { template } });
  } else {
    addTabPane({ _id: 'analysisEdit', value: 'analysisEdit', name: '添加分析' });
  }
};

// 分析Data
const loadAnalysisList = async () => {
  const params = getParams();

  loading.value = true;
  const [error, { data }] = await analysis.getAnalysisList({
    ...params
  });
  loading.value = false;
  if (error) {
    return;
  }
  if (pagination.pageNo === 1) {
    dataList.value = data.list || [];
    pagination.total = +data.total;
  } else {
    dataList.value.push(...(data.list || []));
    pagination.total = +data.total;
  }
};

const editAnalysis = (data) => {
  addTabPane({ _id: `analysisEdit_${data.id}`, value: 'analysisEdit', name: data.name, data });
};

const handleDetail = (data) => {
  addTabPane({ _id: `analysisDetail_${data.id}`, value: 'analysisDetail', name: data.name, data });
};

// 删除分析
const delAnalysis = (data) => {
  modal.confirm({
    content: `确认删除分析【${data.name}】吗？`,
    onOk () {
      return analysis.deleteAnalysis([data.id])
        .then(() => {
          notification.success('删除成功');
          pagination.pageNo = 1;
          loadAnalysisList();
        });
    }
  });
};

// 更新快照
const updateSnapShot = (data) => {
  modal.confirm({
    content: `确认更新分析【${data.name}】的快照内容吗？`,
    onOk () {
      return analysis.refreshAnalysis(data.id)
        .then(([error]) => {
          if (error) {
            return;
          }
          notification.success('更新成功');
        });
    }
  });
};
const handleScrollList = throttle(300, (event) => {
  if (dataList.value.length === pagination.total || loading.value) {
    return;
  }
  const clientHeight = event.currentTarget.clientHeight;
  const scrollHeight = event.currentTarget.scrollHeight;
  const scrollTop = event.currentTarget.scrollTop;
  if (clientHeight + scrollTop + 100 > scrollHeight) {
    pagination.pageNo += 1;
    loadAnalysisList();
  }
});

let wrapperHeight = 0;
const handleWindowResize = debounce(500, () => {
  if (!props.onShow) {
    return;
  }
  const height = dataListWrapRef.value?.clientHeight || 0;
  if (height <= wrapperHeight) {
    return;
  }
  wrapperHeight = height;
  if (!dataList.value.length) {
    return;
  }
  if ((height / 120) > 4) {
    const rows = Math.floor(height / 120) + 2;
    pagination.pageSize = rows * 5;
  } else {
    pagination.pageSize = 20;
  }
  if (dataList.value.length < pagination.pageSize) {
    pagination.pageNo = 1;
    loadAnalysisList();
  }
});

onMounted(() => {
  watch(() => template.value, () => {
    pagination.pageNo = 1;
    loadAnalysisList();
  });

  watch(() => props.refreshNotify, () => {
    pagination.pageNo = 1;
    loadAnalysisList();
  });
  watch(() => orderData.value, () => {
    pagination.pageNo = 1;
    loadAnalysisList();
  }, {
    deep: true
  });

  window.addEventListener('resize', handleWindowResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize);
});

</script>
<template>
  <div class="p-5 h-full flex flex-col overflow-x-hidden">
    <Introduce />
    <div class="text-3.5 font-semibold mb-2.5">已添加的分析</div>
    <SearchPanel
      v-model:orderBy="orderData.orderBy"
      v-model:orderSort="orderData.orderSort"
      :userInfo="props.userInfo"
      :projectId="props.projectId"
      @change="handleSearch"
      @add="addAnalysis" />
    <div class="flex-1 min-h-50 flex mt-2.5">
      <TemplateSelectList
        v-model:template="template"
        v-model:templateData="templateData"
        v-model:templateDesc="templateDesc"
        class="w-50 h-full overflow-y-auto bg-gray-1" />

      <div
        v-show="dataList.length"
        ref="dataListWrapRef"
        style="height: fit-content"
        class="flex-1 max-h-full pb-2.5 pl-2.5 flex flex-wrap min-w-0 overflow-y-auto"
        @scroll="handleScrollList">
        <div
          v-for="item in dataList"
          :key="item.id"
          class="pr-2 flex-1/5 min-w-0 flex-grow-0 mb-2.5">
          <div class="border rounded p-2 h-full flex flex-col justify-between">
            <div>
              <div class="flex space-x-2 ">
                <Icon :icon="TemplateIconConfig[item.template]" class="text-8 mt-6" />
                <div class="flex-1 min-w-0">
                  <div class="flex justify-between">
                    <div
                      :title="item.name"
                      class="flex-1 min-w-0 text-3.5 font-semibold mb-1 text-theme-special cursor-pointer truncate"
                      @click="handleDetail(item)">
                      {{ item.name }}
                    </div>
                    <Tag class="relative -top-1 mr-0 px-0.5 h-5" color="geekblue">
                      {{ item.datasource?.value === 'REAL_TIME_DATA' ? '实时' : '快照' }}
                    </Tag>
                  </div>
                  <p class="">{{ TemplateDescConfig[item.template] }}</p>
                </div>
              </div>
              <div class="mt-1  text-right">
                <div><span class="font-semibold mr-1">{{ item.createdByName }}</span>创建于{{ item.createdDate }}</div>
              </div>
            </div>
            <div class="flex justify-end">
              <Button
                v-show="item.datasource?.value === 'SNAPSHOT_DATA'"
                size="small"
                type="text"
                @click="updateSnapShot(item)">
                <Icon icon="icon-shuaxin" />
              </Button>
              <Button
                size="small"
                type="text"
                @click="editAnalysis(item)">
                <Icon icon="icon-bianji" />
              </Button>
              <Button
                size="small"
                type="text"
                @click="delAnalysis(item)">
                <Icon icon="icon-qingchu" />
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div
        v-show="!dataList.length && !template"
        style="height: fit-content"
        class="flex-1 max-h-full pb-2.5 pl-2.5 flex flex-wrap min-w-0 overflow-y-auto">
        <div
          v-for="item in templateData"
          :key="item.value"
          class="pr-2 flex-1/5 flex-grow-0 mb-2.5">
          <div class="border rounded p-2 h-full flex flex-col justify-between">
            <div>
              <div class="flex space-x-2 ">
                <Icon :icon="TemplateIconConfig[item.value]" class="text-8 mt-6" />
                <div>
                  <div
                    class="text-3.5 font-semibold mb-1 text-theme-special cursor-pointer flex items-center"
                    @click="addAnalysis(item.value)">
                    <Icon icon="icon-jia" />
                    {{ item.description }}
                  </div>
                  <p class="">{{ TemplateDescConfig[item.value] }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div
        v-show="!dataList.length && !!template"
        class="flex-1 h-full p-2.5 min-w-0 text-center flex flex-col items-center space-y-3">
        <Icon :icon="TemplateIconConfig[template]" class="text-20 mt-20" />
        <div>{{ TemplateDescConfig[template] }}</div>
        <div>
          还未创建，点击创建
          <Button
            type="link"
            size="small"
            @click="addAnalysis(template)">
            {{ getTemplateName() }}
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
