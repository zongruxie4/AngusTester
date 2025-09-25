<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, provide, reactive, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  AsyncComponent,
  Grid,
  Icon,
  Input,
  notification,
  Select,
  SelectUser
} from '@xcan-angus/vue-ui';
import { Button, Tag, TypographyParagraph } from 'ant-design-vue';
import { TESTER, appContext } from '@xcan-angus/infra';
import SelectEnum from '@/components/enum/SelectEnum.vue';

import { apis } from '@/api/tester';
import DescriptionModal from '@/views/apis/services/components/markdownDescModal/index.vue';

const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const Security = defineAsyncComponent(() => import('@/views/apis/services/components/security/index.vue'));
const ExternalDocs = defineAsyncComponent(() => import('@/views/apis/services/components/externalDoc/index.vue'));

interface Props {
  disabled:boolean;
  id: string;
  type: string;
  serviceId: string;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: ''
});

const updateApiGroup = inject('updateApiGroup', () => undefined);

const appInfo = ref(appContext.getAccessApp()) as Ref<Record<string, any>>;

const state = reactive({
  info: {
    fullName: '',
    operationId: '',
    source: {
      value: '',
      message: ''
    },
    createdDate: '',
    lastModifiedDate: '',
    owner: '',
    ownerName: '',
    testerName: '',
    status: {
      value: '',
      message: ''
    },
    summary: '',
    auth: false,
    security: [],
    description: '',
    externalDocs: {
      url: ''
    },
    deprecated: false,
    serviceId: '',
    ownerId: '',
    syncName: ''
  },
  interfaceAuthVisible: false,
  description: ''
});

const showSecurity = ref(true);
const securityRef = ref();

const columns = computed(() => {
  return [
    [
      { label: t('common.id'), dataIndex: 'id' },
      { label: t('service.groupApiDetail.columns.summary'), dataIndex: 'summary' },
      { label: t('service.groupApiDetail.columns.operationId'), dataIndex: 'operationId' },
      { label: t('common.source'), dataIndex: 'source', type: '1' },
      { label: t('common.status'), dataIndex: 'status' },
      { label: t('service.groupApiDetail.columns.auth'), dataIndex: 'auth' },
      { label: t('service.groupApiDetail.columns.deprecated'), dataIndex: 'deprecated' },
      // !state.info.protocol?.value?.includes('ws') && { label: '用例数量', dataIndex: 'apiCaseNum' },
      { label: t('service.groupApiDetail.columns.createdByName'), dataIndex: 'createdByName' },
      { label: t('service.groupApiDetail.columns.ownerName'), dataIndex: 'ownerName' },
      { label: t('service.groupApiDetail.columns.tags'), dataIndex: 'tags' },
      { label: t('service.groupApiDetail.columns.createdDate'), dataIndex: 'createdDate' },
      { label: t('service.groupApiDetail.columns.lastModifiedDate'), dataIndex: 'lastModifiedDate' },
      { label: t('common.description'), dataIndex: 'description' },
      { label: t('service.groupApiDetail.columns.securityTitle'), dataIndex: 'securityTitle' },
      { dataIndex: 'security', fullWidthContent: true },
      { label: t('service.groupApiDetail.columns.externalDocsTitle'), dataIndex: 'externalDocsTitle' },
      { dataIndex: 'externalDocs', fullWidthContent: true }
    ].filter(Boolean)
  ];
});

watch(() => props.id, () => {
  if (props.id && props.type === 'API') {
    loadInfo();
  }
});

const loadInfo = async () => {
  if (!props.id) {
    notification.error(t('service.groupApiDetail.messages.selectApiFirst'));
    return;
  }
  const params = props.id;
  const [error, res] = await apis.getApiDetail(params);
  if (error) {
    return;
  }
  state.info = res.data;
  state.info.security = state.info.security || [];
  state.info.externalDocs = res.data.externalDocs || { url: '' };
};

provide('loadInfo', loadInfo);

// 编辑接口状态
const editStatus = ref(false);
const toggleEditStatus = () => {
  editStatus.value = !editStatus.value;
};

// 编辑接口名称
const editName = ref(false);
const nameInputRef = ref();
const toggleEditName = () => {
  editName.value = !editName.value;
  if (editName.value) {
    nextTick(() => {
      nameInputRef.value.focus();
    });
  }
};

// summray 失焦
const handleNameBlur = async (event) => {
  if (!event.target.value || !event.target.value.trim() || event.target.value === state.info.summary) {
    toggleEditName();
    return;
  }
  const [error] = await apis.updateApi([{ id: props.id, summary: event.target.value }]);
  if (error) {
    return;
  }
  state.info.summary = event.target.value;
  toggleEditName();
  updateApiGroup(state.info.serviceId);
};

// 编辑编码
const editOperatId = ref(false);
const operationIdInputRef = ref();
const toggleOperateId = () => {
  editOperatId.value = !editOperatId.value;
  if (editOperatId.value) {
    nextTick(() => {
      operationIdInputRef.value.focus();
    });
  }
};
// 接口编码失焦
const handleOperationIdBlur = async (event) => {
  if (event.target.value === state.info.operationId) {
    toggleOperateId();
    return;
  }

  const [error] = await apis.updateApi([{ id: props.id, operationId: event.target.value }]);
  if (error) {
    return;
  }

  state.info.operationId = event.target.value;
  toggleOperateId();
};
// 编辑负责人
const editOwnerName = ref(false);
const ownerNameIdInputRef = ref();
const toggleownerName = () => {
  editOwnerName.value = !editOwnerName.value;
  if (editOwnerName.value) {
    setTimeout(() => {
      ownerNameIdInputRef.value.focus();
    });
  }
};
const ownerNameBlur = () => {
  toggleownerName();
};
const handleOwnerIdChange = async (value, option) => {
  const [error] = await apis.updateApi([{ id: props.id, ownerId: value }]);
  if (error) {
    return;
  }
  state.info.ownerId = value;
  state.info.ownerName = option.fullName;
};

// 编辑 描述
const descriptionModalVisible = ref(false);
const editdescription = ref(false); // 是否编辑
// 编辑描述
const handleEditDescription = () => {
  descriptionModalVisible.value = true;
  editdescription.value = true;
};
// 预览描述
const previewDescription = () => {
  descriptionModalVisible.value = true;
  editdescription.value = false;
};
// 保存描述
const handleSaveDesc = async (value) => {
  if (value === state.info.description) {
    descriptionModalVisible.value = false;
    return;
  }
  const [error] = await apis.updateApi([{ id: props.id, description: value }]);
  if (error) {
    return;
  }
  state.info.description = value;
};

// 是否弃用
const deprecatedOpt = [{ value: true, label: t('status.yes') }, { value: false, label: t('status.no') }];
const editDeprecated = ref(false);
const deprecatedInputRef = ref();
let defaultDeprecated;
const handleDeprecatedChange = async (value) => {
  const [error] = await apis.updateApi([{ id: props.id, deprecated: value }]);
  if (error) {
    return;
  }
  state.info.deprecated = value;
};
const toggleEditDeprecated = () => {
  editDeprecated.value = !editDeprecated.value;
  if (editDeprecated.value) {
    nextTick(() => {
      deprecatedInputRef.value.focus();
      defaultDeprecated = state.info.deprecated;
    });
  } else {
    if (state.info.deprecated !== defaultDeprecated) {
      updateApiGroup(state.info.serviceId);
    }
  }
};

const authFlagChange = ({ auth }:{auth:boolean}) => {
  state.info.auth = auth;
};

const selectStatus = async (value, options) => {
  const [error] = await apis.patchApiStatus({ id: props.id, status: value });
  if (error) {
    return;
  }
  state.info.status = options;
};

// 添加安全
const addSecurity = () => {
  showSecurity.value = true;
  nextTick(() => {
    securityRef.value.addSecurity();
  });
  // security.value.push({ key: '', value: [], edit: true });
};

onMounted(() => {
  loadInfo();
});

// 打开权限组件弹框
const openInterfaceAuthDialog = () => {
  state.interfaceAuthVisible = true;
};

</script>

<template>
  <div class="mt-2">
    <Grid
      :columns="columns"
      :dataSource="state.info"
      marginBottom="18px">
      <template #summary="{text}">
        <template v-if="editName">
          <Input
            ref="nameInputRef"
            :value="text"
            :maxLength="80"
            class="w-50"
            @blur="handleNameBlur" />
        </template>
        <template v-else>
          {{ text }}
          <Icon
            v-if="!props.disabled && state.info.status?.value !== 'RELEASED'"
            icon="icon-shuxie"
            class="ml-2 text-text-link"
            @click="toggleEditName" />
        </template>
      </template>
      <template #operationId="{text}">
        <template v-if="editOperatId">
          <Input
            ref="operationIdInputRef"
            :value="text"
            :maxLength="80"
            :allowClear="false"
            size="small"
            dataType="mixin-en"
            includes=":_-."
            class="w-50"
            @blur="handleOperationIdBlur" />
        </template>
        <template v-else>
          {{ text || t('common.noData') }}
          <Icon
            v-if="!props.disabled && state.info.status?.value !== 'RELEASED'"
            icon="icon-shuxie"
            class="ml-2 text-text-link"
            @click="toggleOperateId" />
        </template>
      </template>
      <template #auth="{text}">
        {{ text ? t('service.groupApiDetail.auth.hasPermission') : t('service.groupApiDetail.auth.noPermission') }}
        <span v-show="!disabled">
          <Icon
            icon="icon-shuxie"
            class="ml-2 text-text-link"
            @click="openInterfaceAuthDialog" />
        </span>
      </template>
      <template #ownerName="{text}">
        <template v-if="editOwnerName">
          <SelectUser
            ref="ownerNameIdInputRef"
            class="w-50"
            size="small"
            :value="state.info.ownerId"
            @change="handleOwnerIdChange"
            @blur="ownerNameBlur" />
        </template>
        <template v-else>
          {{ text }}
          <span v-show="(!disabled && state.info.status?.value !== 'RELEASED')">
            <Icon
              icon="icon-shuxie"
              class="ml-2 text-text-link"
              @click="toggleownerName" />
          </span>
        </template>
      </template>
      <template #deprecated="{text}">
        <template v-if="editDeprecated">
          <Select
            ref="deprecatedInputRef"
            class="w-25"
            :value="text"
            :options="deprecatedOpt"
            @change="handleDeprecatedChange"
            @blur="toggleEditDeprecated" />
        </template>
        <template v-else>
          {{ text ? t('status.yes') : t('status.no') }}
          <Icon
            v-show="(!disabled && state.info.status?.value !== 'RELEASED')"
            icon="icon-shuxie"
            class="ml-2 text-text-link"
            @click="toggleEditDeprecated" />
        </template>
      </template>
      <template #id>{{ props.id }}</template>
      <template #source="{text}">
        {{ text.message }}
        <template v-if="state.info.syncName">（{{ state.info.syncName }}）</template>
      </template>
      <template #status="{text}">
        <template v-if="editStatus">
          <SelectEnum
            :value="state.info.status?.value"
            enumKey="ApiStatus"
            class="w-25"
            size="small"
            @change="selectStatus" />
          <Icon
            icon="icon-gouxuanzhong"
            class="ml-2 text-4 text-text-link"
            @click="toggleEditStatus" />
        </template>
        <template v-else>
          <span>{{ text?.message }}</span>
          <Icon
            v-show="!disabled"
            icon="icon-shuxie"
            class="ml-2 text-text-link"
            @click="toggleEditStatus" />
        </template>
        <!-- {{ text.message }} -->
      </template>
      <template #tags="{text}">
        <template v-if="text?.length">
          <Tag
            v-for="content in (text || [])"
            :key="content"
            class="py-0 leading-4 my-0.25">
            {{ content }}
          </Tag>
        </template>
        <template v-else>
          {{ t('common.noData') }}
        </template>
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
        <!-- <span class="align-middle">声明可用于当前接口的安全方案</span> -->
      </template>
      <template #security>
        <Security
          :id="props.id"
          ref="securityRef"
          v-model:showSecurity="showSecurity"
          :data="state.info.security"
          type="API" />
      </template>
      <template #description="{text}">
        <div class="flex">
          <TypographyParagraph
            class="flex-1"
            :content="text"
            :ellipsis="{ rows: 3, expandable: false }" />
          <Icon
            icon="icon-spread"
            class="flex-shrink-0 mr-1 cursor-pointer text-text-link"
            @click="previewDescription" />
          <Icon
            v-if="!props.disabled && state.info.status?.value !== 'RELEASED'"
            icon="icon-shuxie"
            class="text-text-link ml-1"
            @click="handleEditDescription" />
        </div>
      </template>
      <template #externalDocsTitle>{{ '' }}</template>
      <template #externalDocs>
        <ExternalDocs
          :id="props.id"
          v-model:data="state.info.externalDocs" />
      </template>
    </Grid>
    <AsyncComponent :visible="state.interfaceAuthVisible">
      <AuthorizeModal
        v-model:visible="state.interfaceAuthVisible"
        enumKey="ApiPermission"
        :appId="appInfo?.id"
        :listUrl="`${TESTER}/apis/auth?apisId=${props.id}`"
        :delUrl="`${TESTER}/apis/auth`"
        :addUrl="`${TESTER}/apis/${props.id}/auth`"
        :updateUrl="`${TESTER}/apis/auth`"
        :enabledUrl="`${TESTER}/apis/${props.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/apis/${props.id}/auth/status`"
        :onTips="t('service.groupApiDetail.modal.onTips')"
        :offTips="t('service.groupApiDetail.modal.offTips')"
        :title="t('service.groupApiDetail.modal.title')"
        @change="authFlagChange" />
    </AsyncComponent>
    <AsyncComponent :visible="descriptionModalVisible">
      <DescriptionModal
        v-model:visible="descriptionModalVisible"
        :isEdit="editdescription"
        :value="state.info.description"
        @ok="handleSaveDesc" />
    </AsyncComponent>
  </div>
</template>
