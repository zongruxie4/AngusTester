<script lang="ts" setup>
import { onMounted, ref, computed, watch, inject, nextTick, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Select, notification, Icon, Spin, Hints, IconRequired } from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Textarea, Tabs, TabPane, RadioGroup, Radio } from 'ant-design-vue';
import { utils, TESTER, GM } from '@xcan-angus/infra';
import { scenario } from '@/api/tester';

import { FormState } from './PropsType';
import { MonitorInfo } from '../PropsType';

const { t } = useI18n();

type Props = {
  projectId: string;
  userInfo: { id: string; fullName: string};
  appInfo: { id: string; };
  _id: string;
  data: {
    _id: string;
    id: string | undefined;
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

const activeTabKey = ref('time');
const formRef = ref();
const createdDateRef = ref();
const CreatedDate = defineAsyncComponent(() => import('@/views/scenario/monitor/edit/createdDate/index.vue'));
const isHttpPlugin = ref(false);

const formState = ref<FormState>({
  scenarioId: undefined,
  description: '',
  name: ''
});

// 创建时间
const createTimeSetting = ref<MonitorInfo['timeSetting']>({
  createdAt: 'NOW'
});

// 通知配置
const noticeSetting = ref<MonitorInfo['noticeSetting']>({
  enabled: true,
  orgType: 'USER',
  orgs: [props.userInfo.id]
});

// 服务器配置
const serverSetting = ref<{url: string; description?: string; variables?: {[key: string]: {[key: string]: string}}}[]>([]);
const loadServerSetting = async () => {
  const [error, { data = [] }] = await scenario.getTestSchemaServer(formState.value.scenarioId);
  if (error) {
    return;
  }
  serverSetting.value = data;
};

const changeVaribleDefaultValue = (idx: number, key: string, en: string) => {
  if (serverSetting.value?.[idx]?.variables?.[key]) {
    serverSetting.value[idx].variables[key].default = en;
  }
};

// 场景选择
const handleSceneChange = (_id: string, option) => {
  isHttpPlugin.value = option.plugin === 'Http';
};

// 组织部门数组
const orgs = ref<{name: string; id: string}[]>([{ id: props.userInfo?.id, name: props.userInfo?.fullName }]);
const validateOrgs = () => {
  if (!orgs.value.length) {
    return Promise.reject(t('common.pleaseSelect'));
  }
  return Promise.resolve();
};

// 组织类型变更
const handleChangeOrgType = () => {
  noticeSetting.value.orgs = [];
  orgs.value = [];
};

// 组织变更
const handleChangeOrgs = (_value: string[], valueObjs: {name: string; id: string; fullName}[]) => {
  orgs.value = (valueObjs || []).map(i => ({ name: i.fullName || i.name, id: i.id }));
};

const hasVariable = (variables = {}) => {
  if (!variables) {
    return false;
  }
  return !!Object.keys(variables || {}).length;
};

const loading = ref(false);

const getParams = () => {
  const { scenarioId, description, name } = formState.value;
  return {
    scenarioId,
    description,
    name,
    id: props.data?.id || undefined,
    projectId: props.projectId,
    timeSetting: createdDateRef.value.getData(),
    noticeSetting: {
      ...noticeSetting.value,
      orgs: noticeSetting.value.enabled ? orgs.value : []
    },
    serverSetting: serverSetting.value
  };
};

const refreshList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'monitorList', notify: utils.uuid() });
  });
};

const editOk = async () => {
  const params = getParams();
  loading.value = true;
  const [error] = await scenario.updateMonitor(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('common.modifySuccess'));
  deleteTabPane([props._id]);
};

const addOk = async () => {
  const params = getParams();
  loading.value = true;
  const [error] = await scenario.addMonitor(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('common.addSuccess'));
  deleteTabPane([props._id]);
};

const ok = async () => {
  if (!orgs.value.length && noticeSetting.value?.enabled) {
    activeTabKey.value = 'notice';
  }
  formRef.value.validate().then(async () => {
    if (!editFlag.value) {
      await addOk();
    } else {
      await editOk();
    }
    refreshList();
  });
};

const cancel = () => {
  deleteTabPane([props._id]);
};

const loadData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await scenario.getMonitorDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as MonitorInfo;
  if (!data) {
    return;
  }

  setFormData(data);

  if (data.scenarioId) {
    loadScenarioPlugin(data.scenarioId);
  }
  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id });
  }
};

const setFormData = (data: MonitorInfo) => {
  if (!data) {
    formState.value = {
      scenarioId: '',
      description: '',
      name: ''
    };
    createTimeSetting.value = {
      createdAt: 'NOW'
    };
    noticeSetting.value = {
      enabled: true,
      orgType: 'DEPT',
      orgs: []
    };
    serverSetting.value = [];
    return;
  }

  const {
    scenarioId,
    description,
    name
  } = data;
  formState.value = {
    scenarioId,
    description,
    name
  };
  createTimeSetting.value = data.timeSetting || {
    createdAt: 'NOW'
  };
  noticeSetting.value = data.noticeSetting
    ? {
        ...data.noticeSetting,
        orgType: data.noticeSetting.orgType?.value || 'USER',
        orgs: (data.noticeSetting.orgs || []).map(i => i.id)
      }
    : {
        enabled: true,
        orgType: 'DEPT',
        orgs: []
      };
  serverSetting.value = data.serverSetting || [];
};

// 获取场景详情
const loadScenarioPlugin = async (scenarioId: string) => {
  const [error, { data }] = await scenario.getScenarioDetail(scenarioId);
  if (error) {
    return;
  }
  isHttpPlugin.value = data.plugin === 'Http';
};

// const members = ref<{ fullName: string, id: string; }[]>([]);

// const loadMembers = async () => {
//   const [error, { data }] = await http.get(`${TESTER}/project/${props.projectId}/member/user`);
//   if (error) {
//     return;
//   }
//   members.value = (data || []).map(i => {
//     return {
//       ...i
//     };
//   });
// };

onMounted(async () => {
  watch(() => props.data, async (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    const oldId = oldValue?.id;
    if (id === oldId) {
      return;
    }

    await loadData(id);
  }, { immediate: true });

  watch(() => formState.value.scenarioId, (newValue) => {
    if (newValue) {
      loadServerSetting();
    }
  });
});

const editFlag = computed(() => {
  return !!props.data?.id;
});

</script>
<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="ok">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>{{ t('scenarioMonitor.edit.save') }}</span>
      </Button>
      <Button
        size="small"
        class="flex items-center space-x-1"
        @click="cancel">
        <span>{{ t('scenarioMonitor.edit.cancel') }}</span>
      </Button>
    </div>
    <Form
      ref="formRef"
      :model="formState"
      size="small"
      :labelCol="{ style: { width: '75px' } }"
      class="max-w-242.5"
      layout="horizontal">
      <FormItem
        required
        name="scenarioId"
        :label="t('scenarioMonitor.edit.monitorScene')">
        <Select
          v-model:value="formState.scenarioId"
          :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
          :fieldNames="{label: 'name', value: 'id'}"
          :disabled="!!props?.data?.id"
          :placeholder="t('scenarioMonitor.edit.selectScene')"
          @change="handleSceneChange" />
      </FormItem>
      <FormItem
        required
        :label="t('scenarioMonitor.edit.monitorName')"
        name="name"
        class="flex-1 min-w-0">
        <Input
          v-model:value="formState.name"
          :maxlength="100"
          :placeholder="t('scenarioMonitor.edit.monitorNamePlaceholder')" />
      </FormItem>
      <FormItem
        :label="t('scenarioMonitor.edit.description')"
        name="description"
        class="flex-1 min-w-0">
        <Textarea
          v-model:value="formState.description"
          :maxlength="200"
          :placeholder="t('scenarioMonitor.edit.descriptionPlaceholder')" />
      </FormItem>
      <Tabs v-model:activeKey="activeTabKey" size="small">
        <TabPane key="time">
          <template #tab>
            <div><IconRequired />{{ t('scenarioMonitor.edit.monitorTime') }}</div>
          </template>
          <CreatedDate
            ref="createdDateRef"
            :createTimeSetting="createTimeSetting" />
        </TabPane>

        <TabPane
          v-if="isHttpPlugin"
          key="server"
          :tab="t('scenarioMonitor.edit.serverConfig')">
          <div class="w-100 space-y-3">
            <div v-for="(serverObj, idx) in serverSetting" class="border rounded p-2">
              <div class="font-bold text-text-title flex items-center">
                <Icon icon="icon-fuwuqi" class="mr-1" />{{ serverObj.url }}
              </div>
              <div class="my-3 ">{{ serverObj.description || t('scenarioMonitor.edit.noDescription') }}</div>
              <ul v-if="hasVariable(serverObj.variables)" class="list-disc space-y-1 pl-4">
                <li v-for="(_value, key) in (serverObj.variables || {})" :key="key">
                  <div
                    class="text-3 text-text-title rounded-sm leading-5 truncate cursor-pointer inline font-bold"
                    :title="key + ''"
                    style="max-width: 400px;">
                    {{ key }}
                  </div>
                  <div class="space-y-1">
                    <div
                      v-for="en in _value.enum"
                      :key="en"
                      class="flex items-center justify-between">
                      <div
                        class="truncate cursor-pointer"
                        style="max-width: 400px;"
                        :title="en">
                        {{ en }}
                      </div>
                      <div class="inline-flex items-center space-x-1">
                        <span v-show="_value.default === en">{{ t('scenarioMonitor.edit.default') }}</span>
                        <Radio
                          size="small"
                          :checked="_value.default === en"
                          class="-mt-1.5"
                          @click="changeVaribleDefaultValue(idx,key,en)" />
                      </div>
                    </div>
                  </div>
                </li>
              </ul>
              <div v-else>
                {{ t('scenarioMonitor.edit.noVariables') }}
              </div>
            </div>
          </div>
        </TabPane>

        <TabPane
          key="notice"
          forceRender>
          <template #tab>
            <div><IconRequired />{{ t('scenarioMonitor.edit.notificationConfig') }}</div>
          </template>
          <RadioGroup
            v-model:value="noticeSetting.enabled"
            :options="[{value: false, label: t('scenarioMonitor.edit.notificationOptions.noNotify')}, { value: true, label: t('scenarioMonitor.edit.notificationOptions.notify') }]">
          </RadioGroup>
          <Hints :text="t('scenarioMonitor.edit.notificationHint')" class="mt-3" />
          <template v-if="noticeSetting.enabled">
            <div class="flex space-x-3 mt-3">
              <span>{{ t('scenarioMonitor.edit.recipient') }}</span>

              <RadioGroup
                v-model:value="noticeSetting.orgType"
                :options="[{value: 'USER', label: t('scenarioMonitor.edit.recipientOptions.user')}, {value: 'DEPT', label: t('scenarioMonitor.edit.recipientOptions.dept')}, {value: 'GROUP', label: t('scenarioMonitor.edit.recipientOptions.group')}]"
                @change="handleChangeOrgType">
              </RadioGroup>
              <FormItem
                name="orgs"
                class="w-50"
                :rules="[{validator: validateOrgs}]">
                <Select
                  v-if="noticeSetting.orgType === 'USER'"
                  v-model:value="noticeSetting.orgs"
                  :showSearch="true"
                  defaultActiveFirstOption
                  :lazy="false"
                  mode="multiple"
                  allowClear
                  class="w-50"
                  :placeholder="t('scenarioMonitor.edit.selectUser')"
                  :action="`${GM}/user?fullTextSearch=true`"
                  :fieldNames="{ label: 'fullName', value: 'id' }"
                  @change="handleChangeOrgs">
                </Select>

                <Select
                  v-if="noticeSetting.orgType === 'DEPT'"
                  v-model:value="noticeSetting.orgs"
                  defaultActiveFirstOption
                  :placeholder="t('scenarioMonitor.edit.selectDept')"
                  class="w-50"
                  mode="multiple"
                  allowClear
                  :lazy="false"
                  :showSearch="true"
                  :action="`${GM}/dept?fullTextSearch=true`"
                  :fieldNames="{ label: 'name', value: 'id' }"
                  @change="handleChangeOrgs">
                </Select>

                <Select
                  v-if="noticeSetting.orgType === 'GROUP'"
                  v-model:value="noticeSetting.orgs"
                  defaultActiveFirstOption
                  :placeholder="t('scenarioMonitor.edit.selectGroup')"
                  class="w-50"
                  mode="multiple"
                  allowClear
                  :lazy="false"
                  :showSearch="true"
                  :action="`${GM}/group?fullTextSearch=true`"
                  :fieldNames="{ label: 'name', value: 'id' }"
                  @change="handleChangeOrgs">
                </Select>
              </FormItem>
            </div>
          </template>
        </TabPane>
      </Tabs>
    </Form>
  </Spin>
</template>

<style scoped>
:deep(.ant-form-item-label>label::after) {
  margin-right: 10px;
}

.ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  padding-top: 0;
}
</style>
