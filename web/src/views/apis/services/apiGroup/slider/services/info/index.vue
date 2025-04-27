<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, ref, Ref, watch } from 'vue';
import { AsyncComponent, AuthorizeModal, Grid, Hints, Icon, Input, SelectEnum } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/tools';

import { services } from '@/api/altester';
import { IInfomation, Status } from './PropsType';

interface Props {
  id: string;
  type: 'SERVICE';
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  type: 'SERVICE',
  disabled: false
});

const Security = defineAsyncComponent(() => import('@/views/apis/services/components/security/index.vue'));

const appInfo = inject('appInfo') as Ref<Record<string, any>>;

// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });

const showSecurity = ref(true);
const columns = computed(() => [
  [
    { label: 'ID', dataIndex: 'id' },
    { label: '名称', dataIndex: 'name' },
    { label: '所属项目', dataIndex: 'parentName' },
    { label: '来源', dataIndex: 'source' },
    { label: '状态', dataIndex: 'status' },
    { label: '权限', dataIndex: 'authFlag' },
    { label: '接口数量', dataIndex: 'apisNum' },
    { label: '用例数量', dataIndex: 'apisCaseNum' }, // @todo
    { label: '添加人', dataIndex: 'createdByName' },
    { label: '添加时间', dataIndex: 'createdDate' },
    { label: '更新时间', dataIndex: 'lastModifiedDate' },
    { label: '安全需求', dataIndex: 'securityTitle' },
    { dataIndex: 'security', fullWidthContent: true }
  ].filter(Boolean)
]);

const name = ref<string>();
const nameError = ref(false);
const editNameFlag = ref(false);
const editName = () => {
  editNameFlag.value = true;
};
const confirmEditName = async () => {
  if (!name.value) {
    nameError.value = true;
    return;
  }
  const [error] = await services.updateServices({ id: props.id, name: name.value });
  if (error) {
    return;
  }
  editNameFlag.value = false;
  infomation.value!.name = name.value;
  updateTabPane({ _id: props.id + 'group', pid: props.id + 'group', name: name.value });
};
const cancelEditName = () => {
  editNameFlag.value = false;
  nameError.value = false;
  name.value = infomation.value?.name;
};
const nameChange = () => {
  nameError.value = false;
};

const status = ref<Status>();
const statusName = ref<string>();
const editStatusFlag = ref(false);
const authFlag = ref(false);
const editStatus = () => {
  editStatusFlag.value = true;
};
const confirmEditStatus = async () => {
  const [error] = await services.patchStatus({ id: props.id, status: status.value });
  if (error) {
    return;
  }

  editStatusFlag.value = false;
  updateTabPane({ _id: props.id + 'group', pid: props.id + 'group', status: { value: status.value, message: statusName.value } });
};
const cancelEditStatus = () => {
  editStatusFlag.value = false;
};
const statusChange = (_value:string, option:{value:Status;message:string}) => {
  statusName.value = option.message;
};

const visible = ref(false);
const editAuth = () => {
  visible.value = true;
};

const infomation = ref<IInfomation>();
const loadInfo = async () => {
  const [error, res] = await services.loadInfo(props.id);
  if (error) {
    return;
  }
  const data = res.data as IInfomation;
  infomation.value = data;
  status.value = data.status?.value;
  statusName.value = data.status?.message;
  name.value = data.name;
  authFlag.value = data.authFlag;
};

const security = ref([]);
const securityRef = ref();
const loadSecurity = async () => {
  const [error, resp] = await services.loadSecurity(props.id);
  if (error) {
    return;
  }
  security.value = resp.data || [];
};
const addSecurity = () => {
  showSecurity.value = true;
  nextTick(() => {
    securityRef.value.addSecurity();
  });
};

const authFlagChange = ({ authFlag }:{authFlag:boolean}) => {
  infomation.value!.authFlag = authFlag;
};

watch(() => props.id, (newValue) => {
  if (newValue && ['PROJECT', 'SERVICE'].includes(props.type)) {
    loadInfo();
    loadSecurity();
  }
}, { immediate: true });

const modelTtileMap = {
  PROJECT: '项目权限',
  SERVICE: '服务权限'
};

const tipMap = {
  PROJECT: {
    on: '开启"有权限控制"后，需要手动授权项目权限后才会有项目相应操作权限，默认开启"有权限控制"。注意：授权项目不会授权项目下接口权限。',
    off: '开启"无权限控制"后，将允许账号下所有用户公开查看和操作项目及项目下接口。'
  },
  SERVICE: {
    on: '开启"有权限控制"后，需要手动授权服务权限后才会有项目相应操作权限，默认开启"有权限控制"。注意：授权项目不会授权服务下接口权限，如果授权对象没有父级项目权限将自动授权查看权限。 ',
    off: '开启"无权限控制"后，将允许账号下所有用户公开查看和操作服务及服务下接口。'
  }
};

</script>
<template>
  <Grid
    :columns="columns"
    :dataSource="infomation"
    marginBottom="18px">
    <template #id>{{ props.id }}</template>
    <template #source="{ text }">
      <span>{{ text?.message }}</span>
      <span v-if="infomation?.importSource" class="ml-1">({{ infomation.importSource.message }})</span>
    </template>
    <template #name>
      <template v-if="!editNameFlag">
        <div class="flex items-start">
          <div class="flex-1">{{ name }}</div>
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="editName" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-start -mt-1.5">
          <Input
            v-model:value="name"
            :maxlength="100"
            :error="nameError"
            trim
            class="w-full"
            placeholder="100字符以内"
            @pressEnter="confirmEditName"
            @change="nameChange" />
          <Icon
            class="ml-2 text-4 mt-1.5 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmEditName" />
          <Icon
            class="ml-2 text-3 mt-2 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelEditName" />
        </div>
      </template>
    </template>
    <template #apiNum="{text}">
      <div class="flex items-center">
        {{ text }}
        <Hints class="ml-2" text="最大支持2000个" />
        <!-- <Icon icon="icon-tishi" class="ml-2 text-blue-tips" /> -->
      </div>
    </template>
    <template #status>
      <template v-if="!editStatusFlag">
        <div class="flex items-start">
          <div class="flex-1">{{ statusName }}</div>
          <Icon
            v-if="!props.disabled"
            icon="icon-shuxie"
            class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
            @click="editStatus" />
        </div>
      </template>
      <template v-else>
        <div class="flex items-center -mt-1.5">
          <SelectEnum
            v-model:value="status"
            class="w-full"
            enumKey="ApiStatus"
            defaultActiveFirstOption
            @change="statusChange" />
          <Icon
            class="ml-2 text-4 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-gouxuanzhong"
            @click="confirmEditStatus" />
          <Icon
            class="ml-2 text-3 flex-shrink-0 cursor-pointer text-text-sub-content hover:text-text-link"
            icon="icon-shanchuguanbi"
            @click="cancelEditStatus" />
        </div>
      </template>
    </template>
    <template #authFlag="{ text }">
      <div class="flex items-start">
        <div class="flex-1">{{ text ? '有权限限制' : '无权限限制' }}</div>
        <Icon
          v-if="!props.disabled"
          icon="icon-shuxie"
          class="flex-shrink-0 ml-2 mt-0.5 cursor-pointer text-text-link"
          @click="editAuth" />
      </div>
    </template>
    <template #securityTitle>
      <Button
        type="primary"
        size="small"
        class="px-1 py-0 h-4"
        ghost
        @click="addSecurity">
        <Icon
          icon="icon-jia"
          class="align-baseline cursor-pointer text-3" />
      </Button>
    </template>
    <template #security>
      <Security
        :id="props.id"
        ref="securityRef"
        v-model:showSecurity="showSecurity"
        type="PROJECT"
        :data="security || {}" />
    </template>
  </Grid>
  <AsyncComponent :visible="visible">
    <AuthorizeModal
      v-model:visible="visible"
      enumKey="ServicesPermission"
      :appId="appInfo?.id"
      :listUrl="`${TESTER}/services/auth?serviceId=${props.id}`"
      :delUrl="`${TESTER}/services/auth`"
      :addUrl="`${TESTER}/services/${props.id}/auth`"
      :updateUrl="`${TESTER}/services/auth`"
      :enabledUrl="`${TESTER}/services/${props.id}/auth/enabled`"
      :initStatusUrl="`${TESTER}/services/${props.id}/auth/status`"
      :onTips="tipMap[props.type].on"
      :offTips="tipMap[props.type].off"
      :title="modelTtileMap[props.type]"
      @change="authFlagChange" />
  </AsyncComponent>
</template>
