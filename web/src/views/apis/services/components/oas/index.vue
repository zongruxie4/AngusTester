<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref } from 'vue';
import { Arrow, AsyncComponent, Hints, Icon, IconRefresh, Input, Spin } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { services } from 'src/api/tester';
import { enumLoader, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

import { CompObj, ComponentsType } from './PropsType';

const AddMoal = defineAsyncComponent(() => import('./AddModal.vue'));

interface Props {
  id: string;
  disabled: boolean;
  type: string;
  name: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  disabled: true
});
const addTabPane = inject('addTabPane', (value: {[key: string]: any}) => ({ value }));
// const updateTabPane = inject('updateTabPane', (value: {[key: string]: any}) => ({ value }));
const loading = ref(false);
const compList = ref<CompObj[]>([]);
// 获取项目服务组件列表
const getProjectCompList = async () => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error, { data }] = await services.getServicesCompList(props.id);
  loading.value = false;
  if (error) {
    return;
  }
  compList.value = data;
};

const visible = ref(false);
const addComponent = () => {
  // currEditData.value = undefined;
  // modalType.value = 'add';
  // visible.value = true;
  addTabPane({
    _id: props.id + 'model',
    pid: props.id + 'model',
    value: 'model',
    id: props.id,
    name: `${props.name}-组件`,
    type: props.type,
    data: undefined
  });
};

const compListObj = ref<Record<ComponentsType, {
  name:string;
  isExpand:boolean;
  list:CompObj[]
}>>(
  {
    schemas: {
      name: '',
      isExpand: false,
      list: []
    },
    responses: {
      name: '',
      isExpand: false,
      list: []
    },
    parameters: {
      name: '',
      isExpand: false,
      list: []
    },
    examples: {
      name: '',
      isExpand: false,
      list: []
    },
    requestBodies: {
      name: '',
      isExpand: false,
      list: []
    },
    headers: {
      name: '',
      isExpand: false,
      list: []
    }
  }
);

const oldCompListObj = ref<Record<ComponentsType, {
  name:string;
  isExpand:boolean;
  list:CompObj[]
}>>(
  {
    schemas: {
      name: '',
      isExpand: false,
      list: []
    },
    responses: {
      name: '',
      isExpand: false,
      list: []
    },
    parameters: {
      name: '',
      isExpand: false,
      list: []
    },
    examples: {
      name: '',
      isExpand: false,
      list: []
    },
    requestBodies: {
      name: '',
      isExpand: false,
      list: []
    },
    headers: {
      name: '',
      isExpand: false,
      list: []
    }
  }
);

const getCompTypesEnum = async () => {
  const [error, data] = await enumLoader.load('ServicesCompType');
  if (error || !data?.length) {
    return;
  }
  for (let i = 0; i < data.length; i++) {
    if (data[i].value === 'securitySchemes') {
      continue;
    }
    compListObj.value[data[i].value] = {
      name: data[i]?.message,
      list: compList.value?.filter(item => item.type.value === data[i].value).map(item => {
        return {
          ...item,
          copyLoading: false,
          delLoading: false,
          isQuote: false,
          ellipsis: false,
          showEllipsis: false,
          isEdit: false
        };
      }) || []
    };
  }

  oldCompListObj.value = JSON.parse(JSON.stringify(compListObj.value));
};

const handleExpand = (item) => {
  item.isExpand = !item.isExpand;
};

onMounted(() => {
  getData();
});

const getData = async () => {
  await getProjectCompList();
  await getCompTypesEnum();
};

const modalType = ref<'add' | 'edit' | 'view'>('view');

const currEditData = ref<CompObj>();
const childrenRef = ref('');
const handleView = async (compObj:CompObj) => {
  currEditData.value = compObj;
  modalType.value = 'view';
  await getAuthConfigInfo(compObj);
  visible.value = true;
};

const getAuthConfigInfo = async (compObj:CompObj) => {
  const [error, { data }] = await services.getRefInfo(props.id, compObj.ref);
  if (error) {
    return;
  }
  currEditData.value = {
    ...currEditData.value,
    model: data.model ? JSON.parse(data.model) : undefined
  };
  if (!data?.resolvedRefModels) {
    return;
  }

  const refs = Object.keys(data.resolvedRefModels);
  if (!refs.length) {
    return;
  }

  currEditData.value = {
    ...currEditData.value,
    isQuote: true,
    quoteName: refs[refs.length - 1],
    model: JSON.parse(data.resolvedRefModels[refs[refs.length - 1]])
  };
  childrenRef.value = undefined;
};

const handleSearch = debounce(duration.search, (event:ChangeEvent) => {
  const value = event.target.value;
  if (value) {
    searchCompObj(value);
    return;
  }

  compListObj.value = JSON.parse(JSON.stringify(oldCompListObj.value));
});

function searchCompObj (keyword: string) {
  for (const comp in compListObj.value) {
    if (compListObj.value[comp].list?.length) {
      const result:CompObj[] = [];
      const list = compListObj.value[comp].list;
      for (let i = 0; i < list.length; i++) {
        if (list[i].key.includes(keyword) || (list[i]?.description && list[i].description.includes(keyword))) {
          result.push(list[i]);
        }
      }
      compListObj.value[comp].list = result;
    }
  }
}

const name = ref('');
const handleOk = () => {
  if (modalType.value === 'add' || modalType.value === 'edit') {
    getData();
    name.value = '';
  }
};

const refreshList = () => {
  getProjectCompList();
  if (name.value) {
    handleSearch({ target: { value: name.value } } as ChangeEvent);
  }
};
</script>
<template>
  <Spin class="h-full flex flex-col" :spinning="loading">
    <Hints text="通过组件可以定义用于复用的对象集合。支持的定义组件类型包括：数据模型、响应、参数、示例、请求体、请求体、头和安全方案。注意：组件没有被API属性引用时对API没有任何影响。" />
    <div class="flex items-center justify-between mt-3.5 space-x-2">
      <Input
        :value="name"
        placeholder="查询组件名称、描述"
        :allowClear="true"
        @change="handleSearch" />
      <Button
        size="small"
        type="primary"
        class="flex items-center"
        :disabled="props.disabled"
        @click="addComponent">
        <Icon icon="icon-jia" class="mr-1" />
        设计组件
      </Button>
      <IconRefresh class="text-3.5" @click="refreshList" />
    </div>
    <div
      ref="tagListRef"
      style="scrollbar-gutter: stable;"
      class="overflow-y-auto flex flex-col space-y-2 pr-1.5 -mr-3.5 mb-5 text-3 text-text-content flex-1">
      <div
        v-for="value,key in compListObj"
        :key="key"
        class="mt-2">
        <div class="h-7 leading-7 flex items-center justify-between bg-bg-table-head px-2 rounded-sm cursor-pointer" @click="handleExpand(value)">
          <span>{{ value.name }}&nbsp;({{ value.list.length }})</span>
          <Arrow
            :open="value.isExpand"
            @click="handleExpand(value)" />
        </div>
        <div class="space-y-2 transition-height duration-500 overflow-hidden" :class="value.isExpand ? 'open-info' : 'stop-info'">
          <AsyncComponent :visible="value.isExpand">
            <div
              v-for="item,index in value.list"
              :key="item.id"
              class="border border-border-divider p-2 rounded relative"
              :class="index === 0 ?'mt-2':''">
              <div
                class="text-3.25 text-text-title font-medium leading-4 truncate cursor-pointer"
                :title="item.description"
                @click="handleView(item)">
                {{ item.key }}
              </div>
              <div class="break-all whitespace-break-spaces leading-4 my-1">
                {{ item.description }}
              </div>
              <div class="flex justify-end mt-2">
              </div>
            </div>
          </AsyncComponent>
        </div>
      </div>
    </div>
    <AsyncComponent :visible="visible">
      <AddMoal
        v-if="visible"
        :id="props?.id"
        v-model:visible="visible"
        :component="currEditData"
        :modalType="modalType"
        @ok="handleOk" />
    </AsyncComponent>
  </spin>
</template>
<style scoped>
.open-info {
  height: auto;
}

.stop-info {
  height: 0;
}

:deep(.v-note-wrapper .v-note-read-model) {
  width: 264px;
  padding: 8px;
}
</style>
