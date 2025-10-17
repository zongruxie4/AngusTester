<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, ref, Ref, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Grid, Hints, Icon, Input, Select } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { TESTER, appContext, enumOptionUtils } from '@xcan-angus/infra';
import { ApiStatus, ServicesPermission } from '@/enums/enums';
import { services } from '@/api/tester';
import { ServicesDetail } from '@/views/apis/services/services/types';

const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const Security = defineAsyncComponent(() => import('@/views/apis/services/components/Security.vue'));

interface Props {
  id: string;
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  disabled: false
});

const { t } = useI18n();

const appInfo = ref(appContext.getAccessApp()) as Ref<Record<string, any>>;

// eslint-disable-next-line @typescript-eslint/no-empty-function
const updateTabPane = inject<(data: any) => void>('updateTabPane', () => { });

const showSecurity = ref(true);

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
  serviceInfo.value!.name = name.value;
  updateTabPane({ _id: props.id + 'group', pid: props.id + 'group', name: name.value });
};

const cancelEditName = () => {
  editNameFlag.value = false;
  nameError.value = false;
  name.value = serviceInfo.value?.name;
};

const nameChange = () => {
  nameError.value = false;
};

const status = ref<ApiStatus>();
const statusName = ref<string>();
const editStatusFlag = ref(false);
const auth = ref(false);

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

const statusChange = (_value:string, option:{value:ApiStatus;message:string}) => {
  statusName.value = option.message;
};

const visible = ref(false);
const editAuth = () => {
  visible.value = true;
};

const serviceInfo = ref<ServicesDetail>();
const loadInfo = async () => {
  const [error, res] = await services.loadDetail(props.id);
  if (error) {
    return;
  }
  const data = res.data as ServicesDetail;
  serviceInfo.value = data;
  status.value = data.status?.value;
  statusName.value = data.status?.message;
  name.value = data.name;
  auth.value = data.auth;
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

const authFlagChange = ({ auth }:{auth:boolean}) => {
  serviceInfo.value!.auth = auth;
};

const apiStatusOpt = ref<{value: string; label: string}[]>([]);
const loadApiStatusOpt = () => {
  apiStatusOpt.value = enumOptionUtils.loadEnumAsOptions(ApiStatus);
};

onMounted(() => {
  loadApiStatusOpt();
});

watch(() => props.id, (newValue) => {
  if (newValue) {
    loadInfo();
    loadSecurity();
  }
}, { immediate: true });

const modelTitleMap = {
  PROJECT: t('service.serviceDetail.modal.projectPermission'),
  SERVICE: t('service.serviceDetail.modal.servicePermission')
};

const tipMap = {
  PROJECT: {
    on: t('service.serviceDetail.tips.project.on'),
    off: t('service.serviceDetail.tips.project.off')
  },
  SERVICE: {
    on: t('service.serviceDetail.tips.service.on'),
    off: t('service.serviceDetail.tips.service.off')
  }
};

const columns = computed(() => [
  [
    { label: t('common.id'), dataIndex: 'id' },
    { label: t('common.name'), dataIndex: 'name' },
    { label: t('common.parentName'), dataIndex: 'parentName' },
    { label: t('common.source'), dataIndex: 'source' },
    { label: t('common.status'), dataIndex: 'status' },
    { label: t('common.authControl'), dataIndex: 'auth' },
    { label: t('service.serviceDetail.columns.apisNum'), dataIndex: 'apisNum' },
    { label: t('service.serviceDetail.columns.apisCaseNum'), dataIndex: 'apisCaseNum' }, // @todo
    { label: t('common.createdBy'), dataIndex: 'createdByName' },
    { label: t('common.createdDate'), dataIndex: 'createdDate' },
    { label: t('common.lastModifiedDate'), dataIndex: 'lastModifiedDate' },
    { label: t('common.securityTitle'), dataIndex: 'securityTitle' },
    { dataIndex: 'security', fullWidthContent: true }
  ].filter(Boolean)
]);
</script>
<template>
  <Grid
    :columns="columns"
    :dataSource="serviceInfo"
    marginBottom="18px">
    <template #id>{{ props.id }}</template>
    <template #source="{ text }">
      <span>{{ text?.message }}</span>
      <span v-if="serviceInfo?.importSource" class="ml-1">({{ serviceInfo.importSource.message }})</span>
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
            :placeholder="t('service.serviceDetail.placeholder.nameInput')"
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
        <Hints class="ml-2" :text="t('service.serviceDetail.hints.maxApis')" />
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
          <Select
            v-model:value="status"
            class="w-full"
            :options="apiStatusOpt"
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
    <template #auth="{ text }">
      <div class="flex items-start">
        <div class="flex-1">{{ text ? t('service.serviceDetail.auth.hasPermission') : t('service.serviceDetail.auth.noPermission') }}</div>
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
        type="SERVICE"
        :data="security || {}" />
    </template>
  </Grid>
  <AsyncComponent :visible="visible">
    <AuthorizeModal
      v-model:visible="visible"
      :enumKey="ServicesPermission"
      :appId="appInfo?.id"
      :listUrl="`${TESTER}/services/auth?serviceId=${props.id}`"
      :delUrl="`${TESTER}/services/auth`"
      :addUrl="`${TESTER}/services/${props.id}/auth`"
      :updateUrl="`${TESTER}/services/auth`"
      :enabledUrl="`${TESTER}/services/${props.id}/auth/enabled`"
      :initStatusUrl="`${TESTER}/services/${props.id}/auth/status`"
      :onTips="tipMap[props.type].on"
      :offTips="tipMap[props.type].off"
      :title="modelTitleMap[props.type]"
      @change="authFlagChange" />
  </AsyncComponent>
</template>
