<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Collapse, CollapsePanel, Popconfirm, TypographyParagraph } from 'ant-design-vue';
import { Arrow, Colon, Icon, notification, Spin, Tooltip } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { services } from '@/api/tester';

import { ServerConfig, ServerInfo } from './PropsType';
import EditForm from './editForm.vue';

type Props = {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const { t } = useI18n();

const loading = ref(false);
const adding = ref(false);
const serverDemo = ref<ServerConfig>();
const activeKey = ref<string[]>([]);
const serverList = ref<ServerConfig[]>([]);
const editSet = ref<Set<string>>(new Set());

const addServerDemo = () => {
  serverDemo.value = getDefaultServer();
  adding.value = true;
};

const getDefaultServer = ():ServerConfig => {
  return {
    id: utils.uuid(),
    'x-xc-id': undefined,
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

const addServer = () => {
  adding.value = true;
};

const cancelAddServer = () => {
  adding.value = false;
  serverDemo.value = undefined;
};

const saveAddServer = async (data:ServerConfig) => {
  const params = getSaveParams(data);
  loading.value = true;
  const [error] = await services.putServicesServerUrl(props.id, params);
  if (error) {
    loading.value = false;
    return;
  }

  adding.value = false;
  serverDemo.value = undefined;
  loadData();
};

const arrowChange = (open: boolean, data: ServerConfig) => {
  if (open) {
    activeKey.value.push(data.id);
    return;
  }

  const index = activeKey.value.findIndex(item => item === data.id);
  activeKey.value.splice(index, 1);
};

const toUpdate = async (data: ServerConfig) => {
  const [error] = await services.updateServiceApisServer(props.id, data['x-xc-id']);
  if (error) {
    return;
  }

  notification.success(t('service.serverConfig.messages.updateToApisSuccess'));
};

const toEdit = (data: ServerConfig) => {
  editSet.value.add(data.id);
};

const toDelete = async (data:ServerConfig, index: number) => {
  loading.value = true;
  const [error] = await services.delServicesServerUrl(props.id, [data['x-xc-id'] as string]);
  loading.value = false;
  if (error) {
    return;
  }

  serverList.value.splice(index, 1);
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

const cancelEdit = (id:string) => {
  editSet.value.delete(id);
};

const save = async (data:ServerConfig, id:string, index:number) => {
  const prevData = serverList.value[index];
  if (isEqual(data, prevData)) {
    editSet.value.delete(id);
    return;
  }

  const editItem = { ...serverList.value[index], ...data };
  const params = getSaveParams(editItem);
  loading.value = true;
  const [error] = await services.putServicesServerUrl(props.id, params);
  loading.value = false;
  if (error) {
    return;
  }

  serverList.value[index] = editItem;
  editSet.value.delete(id);
};

const loadData = async () => {
  loading.value = true;
  const [error, { data }] = await services.getServicesServerUrlInfo(props.id);
  loading.value = false;
  if (error) {
    return;
  }

  serverList.value = ((data || []) as ServerInfo[]).map(item => {
    const variables: ServerConfig['variables'] = [];
    if (item.variables) {
      // 变量排序，与server url变量顺序保持一致
      const matchItems = item.url?.match(/\{[^{}]+\}/g);
      let uniqueNames:string[] = [];
      if (matchItems) {
        // 过滤重复的变量
        uniqueNames = matchItems?.reduce((prev, cur) => {
          if (!prev.includes(cur)) {
            prev.push(cur);
          }
          return prev;
        }, [] as string[]).map(item => item.replace(/\{(.+)\}/, '$1'));

        for (let i = 0, len = uniqueNames.length; i < len; i++) {
          const _name = uniqueNames[i];
          const enumList = item.variables[_name]?.enum?.map(_value => {
            return {
              id: utils.uuid(),
              value: _value
            };
          });
          variables.push({
            ...item.variables[_name],
            enum: enumList,
            name: _name,
            id: utils.uuid()
          });
        }
      }
    }

    return {
      ...item,
      variables,
      id: utils.uuid()
    };
  });

  if (serverList.value?.length === 0) {
    addServer();
  }
};

onMounted(() => {
  if (props.id) {
    loadData();
  }
});

const addServerDisabled = computed(() => {
  return adding.value || serverList.value.length >= 50;
});

const addServerDemoDisabled = computed(() => {
  return serverList.value.length >= 50;
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
</script>
<template>
  <Spin :spinning="loading" class="pb-3 h-full flex flex-col pr-0">
    <div class="flex items-start leading-5 pr-5 text-theme-sub-content">
      <Icon icon="icon-tishi1" class="text-3.5 text-text-tip flex-shrink-0 transform-gpu translate-y-0.5 mr-1" />
      <span class="inline-block">{{ t('service.serverConfig.hints') }}</span>
    </div>
    <div class="flex items-center justify-end mt-3 mb-4 pr-5">
      <Button
        :disabled="addServerDemoDisabled"
        type="default"
        size="small"
        class="mr-3"
        @click="addServerDemo">
        <Icon icon="icon-jia" class="mr-1" />
        <span class="mr-1">{{ t('service.serverConfig.actions.addServerDemo') }}</span>
      </Button>
      <Button
        :disabled="addServerDisabled"
        type="primary"
        size="small"
        @click="addServer">
        <Icon icon="icon-jia" class="mr-1" />
        <span class="mr-1">{{ t('service.serverConfig.actions.addServer') }}</span>
      </Button>
    </div>

    <div class="flex-1 pr-5 overflow-auto">
      <EditForm
        v-if="adding"
        :value="serverDemo"
        :urlMap="urlMap"
        @cancel="cancelAddServer"
        @save="saveAddServer" />

      <div class="space-y-3">
        <template v-for="(item,index) in serverList" :key="item.id">
          <EditForm
            v-if="editSet.has(item.id)"
            :value="item"
            :urlMap="urlMap"
            @cancel="cancelEdit(item.id)"
            @save="save($event,item.id,index)" />

          <Collapse
            v-show="!editSet.has(item.id)"
            v-model:activeKey="activeKey">
            <CollapsePanel
              :key="item.id"
              :showArrow="false"
              style="background-color: #fff;font-size: 12px; line-height: 20px;"
              collapsible="disabled">
              <template #header>
                <div class="w-full px-3 py-3">
                  <div class="flex items-center justify-between w-full leading-5 mb-2">
                    <div class="flex-1 truncate mr-5" :title="item.url">
                      {{ item.url }}
                    </div>
                    <Arrow :open="activeKey.includes(item.id)" @change="arrowChange($event, item)" />
                  </div>
                  <div class="flex items-center justify-end space-x-3">
                    <Tooltip :title="t('service.serverConfig.actions.updateToApis')" placement="top">
                      <Icon
                        icon="icon-shoudongtuisong"
                        class="text-theme-text-hover cursor-pointer text-3.5"
                        @click="toUpdate(item)" />
                    </Tooltip>
                    <Tooltip :title="t('actions.edit')" placement="top">
                      <Icon
                        icon="icon-shuxie"
                        class="text-theme-text-hover cursor-pointer text-3.5"
                        @click="toEdit(item)" />
                    </Tooltip>
                    <!-- <Tooltip title="删除" placement="top"> -->
                    <Popconfirm
                      placement="topRight"
                      :title="t('service.serverConfig.messages.confirmDelete')"
                      @confirm="toDelete(item,index)">
                      <Icon
                        icon="icon-qingchu"
                        class="text-theme-text-hover cursor-pointer text-3.5" />
                    </Popconfirm>
                    <!-- </Tooltip> -->
                  </div>
                </div>
              </template>
              <div>
                <div class="flex items-start leading-4.5 mb-3">
                  <div class="flex-shrink-0 text-theme-sub-content mr-2">
                    <span>{{ t('common.url') }}</span>
                    <Colon />
                  </div>
                  <TypographyParagraph
                    class="break-all"
                    :ellipsis="{ rows: 4, expandable: true, symbol: '更多' }"
                    :content="item.url" />
                </div>

                <div class="flex items-start leading-4.5 mb-3">
                  <div class="flex-shrink-0 text-theme-sub-content mr-2">
                    <span>{{ t('common.description') }}</span>
                    <Colon />
                  </div>
                  <TypographyParagraph
                    class="break-all"
                    :ellipsis="{ rows: 4, expandable: true, symbol: '更多' }"
                    :content="item.description" />
                </div>

                <div v-if="!!item.variables?.length">
                  <div class="text-theme-sub-content mb-0.5">{{ t('common.variables') }}</div>
                  <div class="border border-solid border-theme-text-box rounded px-3 py-3">
                    <div v-for="(_variable, _index) in item.variables" :key="_variable.id">
                      <div class="flex items-start leading-4.5 mb-2">
                        <div class="w-10 flex-shrink-0 text-theme-sub-content">
                          <span>{{ t('common.name') }}</span>
                          <Colon />
                        </div>
                        <div :title="_variable.name" class="flex-1 truncate">{{ _variable.name }}</div>
                      </div>

                      <div class="flex items-start leading-4.5 mb-2">
                        <div class="w-10 flex-shrink-0 text-theme-sub-content">
                          <span>{{ t('common.value') }}</span>
                          <Colon />
                        </div>
                        <div class="flex-1 space-y-1">
                          <div
                            v-for="_enum in _variable.enum"
                            :key="_enum.id"
                            class="flex items-center justify-between">
                            <div :title="_enum.value" class="truncate flex-1">{{ _enum.value }}</div>
                            <div :class="{invisible:_enum.value!==_variable.default}" class="flex-shrink-0">
                              {{ t('common.default') }}
                            </div>
                          </div>
                        </div>
                      </div>

                      <div class="flex items-start leading-4.5">
                        <div class="w-10 flex-shrink-0 text-theme-sub-content">
                          <span>{{ t('common.description') }}</span>
                          <Colon />
                        </div>
                        <TypographyParagraph
                          class="break-all"
                          :ellipsis="{ rows: 4, expandable: true, symbol: '更多' }"
                          :content="_variable.description" />
                      </div>

                      <div
                        v-if="_index < (item.variables?.length - 1)"
                        class="w-full border-t border-dashed border-theme-text-box my-3"></div>
                    </div>
                  </div>
                </div>
              </div>
            </CollapsePanel>
          </Collapse>
        </template>
      </div>
    </div>
  </Spin>
</template>

<style scoped>
/* .ant-collapse-borderless> :deep(.ant-collapse-item) {
  border: none;
} */

.ant-collapse> :deep(.ant-collapse-item)>.ant-collapse-header {
  padding: 0;
}

:deep(.ant-collapse-content) {
  border-style: dashed;
}

/* .ant-collapse {
  line-height: 20px;
} */

/* .ant-collapse-borderless> :deep(.ant-collapse-item) {
  border: none;
  border-radius: 4px;
}

.ant-collapse> :deep(.ant-collapse-item)>.ant-collapse-header {
  align-items: center;
  height: 46px;
  padding: 0 12px 0 38px;
  border-radius: 4px;
  background-color: #fff;
  line-height: 20px;
}

 :deep(.ant-collapse-content)>.ant-collapse-content-box {
  padding: 0 14px 14px;
} */
</style>
