<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, modal, notification, Spin } from '@xcan-angus/vue-ui';
import { clipboard, utils } from '@xcan-angus/tools';
import { services } from '@/api/tester';

import { ServerConfig } from './PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  data:{
    _id: string;
    serviceId?: string;
    serverId?: string;
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  data: undefined
});

const EditForm = defineAsyncComponent(() => import('./edit.vue'));

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

const editformRef = ref();
const serverDemo = ref<ServerConfig>();
const serverList = ref<ServerConfig[]>([]);
const loading = ref(false);

const loadData = async () => {
  if (!serviceId.value || !serverId.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await services.getServiceServerByServerId(serviceId.value, serverId.value);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data;
  if (data) {
    updateTabPane({ _id: serverId.value, name: data.url, data: { _id: serverId.value, serviceId: serviceId.value, serverId: serverId.value } });

    const variables = Object.keys(data.variables || {})?.map((key) => {
      const item = data.variables[key];
      return {
        ...item,
        id: utils.uuid(),
        name: key,
        enum: item.enum?.map(_ele => {
          return {
            id: utils.uuid(),
            value: _ele
          };
        }) || []
      };
    }) || [];

    serverDemo.value = {
      id: serverId.value,
      'x-xc-id': serverId.value,
      description: data.description,
      url: data.url,
      variables
    };
  }
};

const toSave = async () => {
  let data;
  if (typeof editformRef.value?.getData === 'function') {
    data = editformRef.value.getData();
  }

  if (!data) {
    return;
  }

  const params = getSaveParams(data);
  loading.value = true;
  const [error] = await services.putServicesServerUrl(data.serviceId, params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('保存成功');

  if (!serverId.value) {
    updateTabPane({ _id: 'serverList', notify: utils.uuid() });
    deleteTabPane([props.data?._id]);
  }
};

const getSaveParams = (data:ServerConfig) => {
  const variables = data.variables.reduce((prev, cur) => {
    prev[cur.name] = {
      default: cur.default,
      description: cur.description,
      enum: cur.enum?.map(item => item.value) || []
    };

    return prev;
  }, {} as {
      [key:string]:{
        default:string;
        description:string;
        enum:string[];
      }
    });
  const params:{
    description?:string;
    url:string;
    variables:{
      [key:string]:{
        default:string;
        description:string;
        enum:string[];
      }
    };
    'x-xc-id'?:string;
  } = {
    'x-xc-id': data['x-xc-id'],
    description: data.description,
    url: data.url,
    variables
  };

  return params;
};

const toAddServerDemo = () => {
  serverDemo.value = getDefaultServer();
};

const getDefaultServer = ():ServerConfig => {
  return {
    id: utils.uuid(),
    'x-xc-id': undefined,
    serviceId: undefined,
    description: '',
    url: 'http://{env}-api.xxx.com/{version}',
    variables: [
      {
        default: 'prod',
        description: '',
        enum: [
          { id: utils.uuid(), value: 'prod' },
          { id: utils.uuid(), value: 'dev' },
          { id: utils.uuid(), value: 'beta' }
        ],
        id: utils.uuid(),
        name: 'env'
      },
      {
        default: 'v1',
        description: '',
        enum: [
          { id: utils.uuid(), value: 'v1' },
          { id: utils.uuid(), value: 'v2' }
        ],
        id: utils.uuid(),
        name: 'version'
      }
    ]
  };
};

const toDelete = () => {
  modal.confirm({
    content: '确定删除服务器吗？',
    async onOk () {
      const id = serverId.value;
      loading.value = true;
      const [error] = await services.delServicesServerUrl(serviceId.value as string, [id] as string[]);
      loading.value = false;
      if (error) {
        return;
      }

      updateTabPane({ _id: 'serverList', notify: utils.uuid() });
      deleteTabPane([props.data?._id]);
    }
  });
};

const toClone = async () => {
  const params:{
    description?:string;
    url:string;
    variables:{
      [key:string]:{
        default:string;
        description:string;
        enum:string[];
      }
    };
  } = getSaveParams(serverDemo.value);
  delete params['x-xc-id'];
  loading.value = true;
  const [error] = await services.putServicesServerUrl(serviceId.value as string, params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('克隆成功');
  updateTabPane({ _id: 'serverList', notify: utils.uuid() });
};

const toCopyLink = () => {
  clipboard.toClipboard(window.location.origin + `/apis#server?serviceId=${serviceId.value}&serverId=${serverId.value}`).then(() => {
    notification.success('复制链接成功');
  }).catch(() => {
    notification.error('复制链接失败');
  });
};

const toRefresh = () => {
  loadData();
};

onMounted(() => {
  loadData();
});

const urlMap = computed(() => {
  return serverList.value.reduce((prev, cur) => {
    if (prev[cur.url]) {
      prev[cur.url].push(cur.id);
    } else {
      prev[cur.url] = [cur.id];
    }

    return prev;
  }, {} as {[key:string]:string[]});
});

const serverId = computed(() => {
  return props.data?.serverId;
});

const serviceId = computed(() => {
  return props.data?.serviceId;
});
</script>

<template>
  <Spin :spinning="loading" class="h-full py-5 pl-5 flex flex-col">
    <div class="flex items-center space-x-2.5 mb-5 pr-5">
      <Button
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="toSave">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>
          保存
        </span>
      </Button>

      <Button
        type="default"
        size="small"
        class="mr-3"
        @click="toAddServerDemo">
        <Icon icon="icon-jia" class="mr-1" />
        <span class="mr-1">服务器示例</span>
      </Button>

      <template v-if="!!serverId">
        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toDelete">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span>
            删除
          </span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toClone">
          <Icon icon="icon-fuzhizujian2" class="text-3.5" />
          <span>
            克隆
          </span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toCopyLink">
          <Icon icon="icon-fuzhi" class="text-3.5" />
          <span>
            复制链接
          </span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toRefresh">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span>
            刷新
          </span>
        </Button>
      </template>
    </div>

    <EditForm
      ref="editformRef"
      class="max-w-200"
      :projectId="props.projectId"
      :serviceId="serviceId"
      :value="serverDemo"
      :urlMap="urlMap" />
  </Spin>
</template>
