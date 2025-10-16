<script setup lang="ts">
import { Button } from 'ant-design-vue';
import { computed, inject, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { debounce, throttle } from 'throttle-debounce';
import { Dropdown, DropdownGroup, DropdownSort, Icon, Input } from '@xcan-angus/vue-ui';
import { duration, utils, appContext, HttpMethod } from '@xcan-angus/infra';
import { ServicesPermission, ApiStatus } from '@/enums/enums';
import elementResizeDetector from 'element-resize-detector';

interface Props{
  searchKeyword:string;
  serviceId:string;
  showGroupList:boolean;
  disabled?: boolean;
  orderBy?: string;
  orderSort?:string;
  loading: boolean;
  serviceName: string;
  groupBy: string;
  serviceAuths: string[];
}

const props = withDefaults(defineProps<Props>(), {
  searchKeyword: undefined,
  serviceId: undefined,
  showGroupList: false,
  disabled: false,
  orderBy: undefined,
  orderSort: undefined,
  loading: false,
  serviceName: '',
  groupBy: '-',
  serviceAuths: () => ([])
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'loadApis'):void;
  (e:'update:searchKeyword', value:string|undefined):void;
  (e:'update:showGroupList', value:boolean):void;
  (e:'share'):void;
  (e:'update:orderBy', value:string): void;
  (e:'update:orderSort', value:string): void;
  (e:'grouped', value: string): void;
  (e:'config', value:string):void;
}>();

const { t } = useI18n();
const erd = elementResizeDetector({ strategy: 'scroll' });
const containerRef = ref();
const showMoreIcon = ref(false);
const userInfo = ref(appContext.getUser());
// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

// Local constants to avoid magic numbers/strings
// Responsive threshold to switch actions into a compact dropdown
const RESPONSIVE_WIDTH_DROPDOWN = 800; // px
const getOrderStorageKey = (): string => `${props.serviceId}_order`;
const getGroupByStorageKey = (): string => `${props.serviceId}_groupBy`;

// Sort & Group options (typed as any to align with DropdownSort/MenuItemProps expectations)
const sortAndGroupOptions: any = reactive({
  sort: [{
    key: 'createdDate',
    name: t('service.groupHeader.sort.byCreatedDate'),
    orderSort: 'DESC'
  },
  {
    key: 'summary',
    name: t('service.groupHeader.sort.byName'),
    orderSort: 'ASC'
  },
  {
    key: 'createdByName',
    name: t('service.groupHeader.sort.byCreatedBy'),
    orderSort: 'ASC'
  }],
  group: [
    {
      key: '',
      name: t('actions.noGroup')
    },
    {
      key: 'createdBy',
      name: t('service.groupHeader.group.byCreatedBy')
    },
    {
      key: 'method',
      name: t('service.groupHeader.group.byMethod')
    },
    {
      key: 'ownerId',
      name: t('service.groupHeader.group.byOwner')
    },
    {
      key: 'tag',
      name: t('service.groupHeader.group.byTag')
    }
  ]
});

const mainState = inject('mainState', reactive<{id: string, type?: string}>({ id: '' }));

const groupedKey = ref('');

// Typed two-way models to satisfy DropdownSort expectations
const orderByModel = computed<any>({
  get: () => props.orderBy,
  set: (val) => emit('update:orderBy', (val || ''))
});

const orderSortModel = computed<any>({
  get: () => props.orderSort,
  set: (val) => emit('update:orderSort', (val || ''))
});

// Local state
interface StateType{
  hostModalVisible: boolean,
  visible:boolean,
  searchKeyword:string,
  host: string | null,
}

const state:StateType = reactive({
  hostModalVisible: false,
  visible: false,
  host: null,
  searchKeyword: ''
});

const updateSearchKeyword = debounce(duration.search, (event:ChangeEvent) => {
  const value = event.target.value?.trim();
  emit('update:searchKeyword', value);
});

// Create a new API/WebSocket entry and open an editing tab
const addApi = (type: 'API' | 'websocket') => {
  if (type === 'API') {
    const param = {
      summary: 'api' + new Date().getTime(),
      ownerId: userInfo.value?.id,
      serviceId: props.serviceId,
      assertions: [],
      authentication: null,
      host: '',
      method: HttpMethod.GET,
      status: ApiStatus.UNKNOWN,
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
      ownerId: userInfo.value?.id,
      serviceId: props.serviceId,
      parameters: [],
      protocol: 'wss',
      method: HttpMethod.GET,
      status: ApiStatus.UNKNOWN
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

// Navigate to Security config view
const handleAddSecurity = () => {
  mainState.id = '';
  mainState.type = undefined;
  setTimeout(() => {
    emit('config', 'security');
  }, 50);
};

// Navigate to Server config view (only when no host selected)
const handleAddServer = () => {
  mainState.id = '';
  mainState.type = undefined;
  setTimeout(() => {
    if (state.host) {
      return;
    }
    emit('config', 'serverConfig');
  }, 50);
};

// Apply grouping and persist preference
const handleGroup = (groupKey: string) => {
  groupedKey.value = groupKey;
  localStorage.setItem(getGroupByStorageKey(), groupedKey.value);
  emit('grouped', groupKey);
  if (groupKey !== '') {
    emit('update:showGroupList', true);
  } else {
    emit('update:showGroupList', false);
  }
};

// Apply sort type and persist preference
const setSortType = ({ orderBy, orderSort }:{orderBy:string;orderSort:string}):void => {
  emit('update:orderBy', orderBy);
  emit('update:orderSort', orderSort);
  localStorage.setItem(getOrderStorageKey(), JSON.stringify({ orderBy, orderSort }));
};

const moreBtnConfig = computed(() => [
  {
    key: 'addApi',
    name: t('service.groupHeader.actions.addHttpApi'),
    permission: ServicesPermission.ADD
  },
  {
    key: 'addSocket',
    name: t('service.groupHeader.actions.addWebSocketApi'),
    permission: ServicesPermission.ADD
  },
  {
    key: 'serverConfig',
    name: t('service.groupHeader.actions.serverConfig'),
    permission: ServicesPermission.MODIFY
  },
  {
    key: 'authConfig',
    name: t('service.groupHeader.actions.authConfig'),
    permission: ServicesPermission.MODIFY
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
      handleAddServer();
      break;
    case 'authConfig':
      handleAddSecurity();
      break;
  }
};

// Compute permissions for dropdown action items based on service-level authz
const getActionPermission = computed(() => {
  const keys: string[] = [];
  if (props.serviceAuths.includes(ServicesPermission.ADD)) {
    keys.push(ServicesPermission.ADD);
  }
  if (!mainState.id && props.serviceAuths.includes(ServicesPermission.MODIFY)) {
    keys.push(ServicesPermission.MODIFY);
  }
  return keys;
});

// Resize observer toggles condensed layout when width is below threshold
const resizeHandler = throttle(duration.resize, () => {
  if (containerRef.value) {
    const containerWidth = containerRef.value.clientWidth;
    showMoreIcon.value = containerWidth < RESPONSIVE_WIDTH_DROPDOWN;
  }
});

// Lifecycle
onMounted(() => {
  state.searchKeyword = props.searchKeyword;
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
        v-model:value="state.searchKeyword"
        :placeholder="t('common.placeholders.searchKeyword')"
        class="rounded"
        :maxlength="100"
        :trim="true"
        :allowClear="true"
        @change="updateSearchKeyword" />
    </div>
    <div class="flex-1 flex justify-end items-center space-x-3.5">
      <template v-if="!showMoreIcon">
        <Button
          :disabled="!serviceAuths.includes(ServicesPermission.ADD)"
          size="small"
          class="px-0"
          type="link"
          @click="addApi('API')">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-jia" />
            <span>{{ t('service.groupHeader.actions.addHttpApi') }}</span>
          </div>
        </Button>
        <Button
          :disabled="!serviceAuths.includes(ServicesPermission.ADD)"
          size="small"
          class="px-0"
          type="link"
          @click="addApi('websocket')">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-jia" />
            <span>{{ t('service.groupHeader.actions.addWebSocketApi') }}</span>
          </div>
        </Button>
        <Button
          :disabled="!!mainState.id || !serviceAuths.includes(ServicesPermission.MODIFY)"
          size="small"
          class="px-0"
          type="link"
          @click="handleAddServer">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-host" />
            <span>{{ t('service.groupHeader.actions.serverConfig') }}</span>
          </div>
        </Button>
        <Button
          :disabled="!!mainState.id || !serviceAuths.includes(ServicesPermission.MODIFY)"
          size="small"
          class="px-0"
          type="link"
          @click="handleAddSecurity">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-renzhengtou" />
            <span>{{ t('service.groupHeader.actions.authConfig') }}</span>
          </div>
        </Button>
      </template>
      <template v-else>
        <Dropdown
          :menuItems="moreBtnConfig"
          :permissions="getActionPermission"
          @click="onDropBtnClick">
          <Icon icon="icon-gengduo" />
        </Dropdown>
      </template>
      <DropdownSort
        v-model:orderSort="orderSortModel"
        v-model:orderBy="orderByModel"
        :menuItems="sortAndGroupOptions.sort"
        @click="setSortType">
        <Button
          type="link"
          size="small"
          class="px-0">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-shunxu" />
            <span>{{ t('actions.sort') }}</span>
          </div>
        </Button>
      </DropdownSort>
      <DropdownGroup
        v-model:activeKey="groupedKey"
        :menuItems="sortAndGroupOptions.group"
        @click="handleGroup">
        <Button
          type="link"
          size="small"
          class="px-0">
          <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
            <Icon icon="icon-fenzu" />
            <span>{{ t('actions.group') }}</span>
          </div>
        </Button>
      </DropdownGroup>
      <Button
        type="link"
        size="small"
        class="px-0"
        :disabled="props.loading"
        @click="emit('loadApis')">
        <div class="flex items-center space-x-1 text-text-content hover:text-text-link-hover">
          <Icon icon="icon-shuaxin" />
          <span>{{ t('actions.refresh') }}</span>
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
