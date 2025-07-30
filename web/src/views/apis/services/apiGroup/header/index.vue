<script setup lang="ts">
import { Button } from 'ant-design-vue';
import { computed, inject, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { debounce, throttle } from 'throttle-debounce';
import { Dropdown, DropdownGroup, DropdownSort, Icon, Input } from '@xcan-angus/vue-ui';
import { duration, utils } from '@xcan-angus/infra';
import elementResizeDetector from 'element-resize-detector';

interface Props{
  name:string;
  serviceId:string;
  showGroupList:boolean;
  disabled?: boolean;
  orderBy?: string;
  orderSort?:string;
  loading: boolean;
  projectAuths: string[];
  serviceName: string;
  groupBy: string;
  projectTargetType: {value: string};
}

const props = withDefaults(defineProps<Props>(), {
  name: undefined,
  serviceId: undefined,
  showGroupList: false,
  disabled: false,
  orderBy: undefined,
  orderSort: undefined,
  groupBy: '-',
  projectAuths: () => ([])
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'loadInteface'):void;
  (e:'update:name', value:string|undefined):void;
  (e:'update:showGroupList', value:boolean):void;
  (e:'share'):void;
  (e:'update:orderBy', value:string): void;
  (e:'update:orderSort', value:string): void;
  (e:'grouped', value: string): void;
  (e:'config', value:string):void;
}>();

const erd = elementResizeDetector({ strategy: 'scroll' });
const containerRef = ref();
const showMoreIcon = ref(false);
const userInfo = inject('tenantInfo', ref());
// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

const enumList = reactive({
  sortList: [{
    key: 'createdDate',
    name: '按添加时间',
    orderSort: 'DESC'
  },
  {
    key: 'summary',
    name: '按名称',
    orderSort: 'ASC'
  },
  {
    key: 'createdByName',
    name: '按添加人',
    orderSort: 'ASC'
  }],
  groupList: [
    {
      key: '',
      name: '不分组'
    },
    {
      key: 'createdBy',
      name: '按添加人分组'
    },
    {
      key: 'method',
      name: '按请求方法分组'
    },
    {
      key: 'ownerId',
      name: '按负责人分组'
    },
    {
      key: 'tag',
      name: '按标签分组'
    }
  ]
});

const projectState = inject('api', reactive<{id: string, type?: string}>({ id: '' }));

const groupedKey = ref('');

interface StateType{
  hostModalVisible: boolean,
  visible:boolean,
  name:string,
  host: string | null,
}

const state:StateType = reactive({
  hostModalVisible: false,
  visible: false,
  host: null,
  name: ''
});

// 查询
const loadInteface = debounce(duration.search, (event:ChangeEvent) => {
  const value = event.target.value?.trim();
  emit('update:name', value);
});

const addApi = (type) => {
  if (type === 'API') {
    const param = {
      summary: 'api' + new Date().getTime(),
      ownerId: userInfo.value.id,
      serviceId: props.serviceId,
      assertions: [],
      authentication: null,
      host: '',
      method: 'GET',
      parameters: [],
      requestBody: {},
      secured: false,
      endpoint: '',
      protocol: 'http'
    };

    addTabPane({
      name: param.summary,
      value: 'API',
      _id: utils.uuid('api') + 'API',
      unarchived: true,
      serviceId: props.serviceId,
      serviceName: props.serviceName
    });
  } else {
    const params = {
      summary: 'socket' + new Date().getTime(),
      ownerId: userInfo.value.id,
      serviceId: props.serviceId,
      parameters: [],
      protocol: 'wss',
      method: 'GET',
      status: 'UNKNOWN'
    };

    addTabPane({
      name: params.summary,
      value: 'socket',
      _id: utils.uuid('socket') + 'socket',
      unarchived: true,
      serviceId: props.serviceId,
      serviceName: props.serviceName
    });
  }
};

// 打开配置安全方案
const configuringAuthHeader = () => {
  projectState.id = '';
  projectState.type = undefined;
  setTimeout(() => {
    emit('config', 'security');
  }, 50);
};

const setSortType = ({ orderBy, orderSort }:{orderBy:string;orderSort:string}):void => {
  emit('update:orderBy', orderBy);
  emit('update:orderSort', orderSort);
  localStorage.setItem(`${props.serviceId}_order`, JSON.stringify({ orderBy, orderSort }));
};

const handleAddHost = () => {
  projectState.id = '';
  projectState.type = undefined;
  setTimeout(() => {
    if (state.host) {
      return;
    }
    emit('config', 'serverConfig');
  });
};

const grouped = (groupKey: string) => {
  groupedKey.value = groupKey;
  localStorage.setItem(`${props.serviceId}_groupBy`, groupedKey.value);
  emit('grouped', groupKey);
  if (groupKey !== '') {
    emit('update:showGroupList', true);
  } else {
    emit('update:showGroupList', false);
  }
};

const moreBtnsConfig = computed(() => [
  {
    key: 'addApi',
    name: '添加Http接口',
    permission: 'ADD'
  },
  {
    key: 'addSocket',
    name: '添加WebSocket接口',
    permission: 'ADD'
  },
  {
    key: 'serverConfig',
    name: '服务器配置',
    permission: 'CONFIG'
  },
  {
    key: 'authConfig',
    name: '安全方案配置',
    permission: 'CONFIG'
  }
]);

const onDropBtnClick = (btn: {key: string; name: string}) => {
  switch (btn.key) {
    case 'addApi':
      addApi('API');
      break;
    case 'addSocket':
      addApi('websocket');
      break;
    case 'serverConfig':
      handleAddHost();
      break;
    case 'authConfig':
      configuringAuthHeader();
      break;
  }
};

const getBtnPermission = computed(() => {
  const keys: string[] = [];
  if (props.projectAuths.includes('ADD')) {
    keys.push('ADD');
  }
  if (!projectState.id) {
    keys.push('CONFIG');
  }
  return keys;
});

const resizeHandler = throttle(duration.resize, () => {
  if (containerRef.value) {
    const containerWidth = containerRef.value.clientWidth;
    if (containerWidth < 800) {
      showMoreIcon.value = true;
    } else {
      showMoreIcon.value = false;
    }
  }
});

onMounted(() => {
  state.name = props.name;
  erd.listenTo(containerRef.value, resizeHandler);
  watch(() => props.groupBy, newValue => {
    groupedKey.value = newValue;
  }, {
    immediate: true
  });
});

onBeforeUnmount(() => {
  erd.removeListener(containerRef.value, resizeHandler);
});
</script>
<template>
  <div ref="containerRef" class="w-full flex justify-between items-center">
    <div class="w-75">
      <Input
        v-model:value="state.name"
        placeholder="查询接口ID、名称、路径"
        class="rounded"
        :maxlength="100"
        :trim="true"
        :allowClear="true"
        @change="loadInteface" />
    </div>
    <div class="flex-1 flex justify-end items-center space-x-3.5">
      <template v-if="!showMoreIcon">
        <Button
          :disabled="!projectAuths.includes('ADD')"
          size="small"
          class="px-0"
          type="link"
          @click="addApi('API')">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-jia" />
            <span>添加Http接口</span>
          </div>
        </Button>
        <Button
          :disabled="!projectAuths.includes('ADD')"
          size="small"
          class="px-0"
          type="link"
          @click="addApi('websocket')">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-jia" />
            <span>添加WebSocket接口</span>
          </div>
        </Button>
        <Button
          :disabled="!!projectState.id"
          size="small"
          class="px-0"
          type="link"
          @click="handleAddHost">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-host" />
            <span>服务器配置</span>
          </div>
        </Button>
        <Button
          :disabled="!!projectState.id"
          size="small"
          class="px-0"
          type="link"
          @click="configuringAuthHeader">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-renzhengtou" />
            <span>安全方案配置</span>
          </div>
        </Button>
      </template>
      <template v-else>
        <Dropdown
          :menuItems="moreBtnsConfig"
          :permissions="getBtnPermission"
          @click="onDropBtnClick">
          <Icon icon="icon-gengduo" />
        </Dropdown>
      </template>
      <DropdownSort
        v-model:orderSort="props.orderSort"
        v-model:orderBy="props.orderBy"
        :menuItems="enumList.sortList"
        @click="setSortType">
        <Button
          type="link"
          size="small"
          class="px-0">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-shunxu" />
            <span>排序</span>
          </div>
        </Button>
      </DropdownSort>
      <DropdownGroup
        v-model:activeKey="groupedKey"
        :menuItems="enumList.groupList"
        @click="grouped">
        <Button
          type="link"
          size="small"
          class="px-0">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-fenzu" />
            <span>分组</span>
          </div>
        </Button>
      </DropdownGroup>
      <Button
        type="link"
        size="small"
        class="px-0"
        :disabled="props.loading"
        @click="emit('loadInteface')">
        <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
          <Icon icon="icon-shuaxin" />
          <span>刷新</span>
        </div>
      </Button>
    </div>
  </div>
</template>
<style scoped>
.sort-list:hover > div {
  @apply visible;
}

.host-select :deep(.ant-select-selector) {
  @apply rounded h-7;
}

.host-select :deep(.ant-select-selector) .ant-select-selection-item {
  @apply leading-7;
}

.host-select.ant-select-disabled :deep(input.ant-select-selection-search-input) {
  @apply cursor-pointer;
}

.btn-wrapper > button {
  color: rgb(82, 90, 101);

  @apply px-0 flex items-center text-3;
}

.btn-wrapper > button .icon {
  @apply mr-1.5 text-3;
}
</style>
