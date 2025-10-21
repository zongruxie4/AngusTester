<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, provide, reactive, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Grid, Icon, Input, notification, Select, SelectUser } from '@xcan-angus/vue-ui';
import { Button, Tag, TypographyParagraph } from 'ant-design-vue';
import { TESTER, appContext } from '@xcan-angus/infra';
import { ApiStatus } from '@/enums/enums';
import { apis } from '@/api/tester';

import SelectEnum from '@/components/form/enum/SelectEnum.vue';

const DescriptionModal = defineAsyncComponent(() => import('@/views/apis/services/components/MarkdownDescModal.vue'));
const AuthorizeModal = defineAsyncComponent(() => import('@/components/auth/AuthorizeModal.vue'));
const Security = defineAsyncComponent(() => import('@/views/apis/services/components/Security.vue'));
const ExternalDocs = defineAsyncComponent(() => import('@/views/apis/services/components/ExternalDoc.vue'));

interface Props {
  disabled:boolean;
  id: string;
  type: string;
  serviceId: string;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  id: ''
});

const { t } = useI18n();

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

// Load API basic info and normalize optional fields
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
  // Keep reactivity: assign into existing state.info object
  Object.assign(state.info, res.data);
  state.info.security = state.info.security || [];
  state.info.externalDocs = res.data.externalDocs || { url: '' };
};

// Edit API status
const editStatus = ref(false);
const toggleEditStatus = () => {
  editStatus.value = !editStatus.value;
};

// Edit API name
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

// On summary input blur, persist when changed
const handleNameBlur = async (event: any) => {
  const target = event?.target as HTMLInputElement | null;
  const value = target?.value ?? '';
  if (!value || !value.trim() || value === state.info.summary) {
    toggleEditName();
    return;
  }
  const [error] = await apis.updateApi([{ id: props.id, summary: value }]);
  if (error) {
    return;
  }
  state.info.summary = value;
  toggleEditName();
  updateApiGroup();
};

// Edit operationId
const editOperationId = ref(false);
const operationIdInputRef = ref();
const toggleEditOperationId = () => {
  editOperationId.value = !editOperationId.value;
  if (editOperationId.value) {
    nextTick(() => {
      operationIdInputRef.value.focus();
    });
  }
};

// On operationId input blur, persist when changed
const handleOperationIdBlur = async (event: any) => {
  const target = event?.target as HTMLInputElement | null;
  const value = target?.value ?? '';
  if (value === state.info.operationId) {
    toggleEditOperationId();
    return;
  }

  const [error] = await apis.updateApi([{ id: props.id, operationId: value }]);
  if (error) {
    return;
  }

  state.info.operationId = value;
  toggleEditOperationId();
};

// Edit owner info
const editOwnerName = ref(false);
const ownerSelectRef = ref();
const toggleOwnerName = () => {
  editOwnerName.value = !editOwnerName.value;
  if (editOwnerName.value) {
    setTimeout(() => {
      ownerSelectRef.value.focus();
    });
  }
};

const ownerNameBlur = () => {
  toggleOwnerName();
};

const handleOwnerIdChange = async (value: string, option: { fullName: string }) => {
  const [error] = await apis.updateApi([{ id: props.id, ownerId: value }]);
  if (error) {
    return;
  }
  state.info.ownerId = value;
  state.info.ownerName = option.fullName;
};

// Description modal visibility and editing state
const descriptionModalVisible = ref(false);
const editDescription = ref(false); // whether editing

// Open description editor
const handleEditDescription = () => {
  descriptionModalVisible.value = true;
  editDescription.value = true;
};

// Preview description
const previewDescription = () => {
  descriptionModalVisible.value = true;
  editDescription.value = false;
};

// Save description if changed
const handleSaveDesc = async (value: string) => {
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

// Deprecated flag
const deprecatedOpt: any[] = [{ value: true, label: t('status.yes') }, { value: false, label: t('status.no') }];
const editDeprecated = ref(false);
const deprecatedInputRef = ref();
let defaultDeprecated: boolean | undefined;
const handleDeprecatedChange = async (value: any) => {
  const boolVal = value === true || value === 'true';
  const [error] = await apis.updateApi([{ id: props.id, deprecated: boolVal }]);
  if (error) {
    return;
  }
  state.info.deprecated = boolVal;
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
      updateApiGroup();
    }
  }
};

// Update auth flag from permission modal
const authFlagChange = ({ auth }: { auth: boolean }) => {
  state.info.auth = auth;
};

// Persist API status and update local state
const selectStatus = async (value: string, option?: { label: string; value: string }) => {
  const [error] = await apis.patchApiStatus({ id: props.id, status: value });
  if (error) {
    return;
  }
  state.info.status = { value, message: option?.label || state.info.status?.message } as any;
};

// Add security scheme row
const addSecurity = () => {
  showSecurity.value = true;
  nextTick(() => {
    securityRef.value.addSecurity();
  });
};

// Open permission dialog
const openInterfaceAuthDialog = () => {
  state.interfaceAuthVisible = true;
};

onMounted(() => {
  loadInfo();
});

watch(() => props.id, () => {
  if (props.id && props.type === 'API') {
    loadInfo();
  }
});

provide('loadInfo', loadInfo);

const columns = computed(() => {
  return [
    [
      { label: t('common.id'), dataIndex: 'id' },
      { label: t('service.groupApiDetail.columns.summary'), dataIndex: 'summary' },
      { label: t('service.groupApiDetail.columns.operationId'), dataIndex: 'operationId' },
      { label: t('common.source'), dataIndex: 'source', type: '1' },
      { label: t('common.status'), dataIndex: 'status' },
      { label: t('actions.permission'), dataIndex: 'auth' },
      { label: t('service.groupApiDetail.columns.deprecated'), dataIndex: 'deprecated' },
      { label: t('common.createdBy'), dataIndex: 'createdByName' },
      { label: t('common.owner'), dataIndex: 'ownerName' },
      { label: t('common.tag'), dataIndex: 'tags' },
      { label: t('common.createdDate'), dataIndex: 'createdDate' },
      { label: t('common.lastModifiedDate'), dataIndex: 'lastModifiedDate' },
      { label: t('common.description'), dataIndex: 'description' },
      { label: t('service.groupApiDetail.columns.securityTitle'), dataIndex: 'securityTitle' },
      { label: 'security', dataIndex: 'security', fullWidthContent: true } as any,
      { label: t('service.groupApiDetail.columns.externalDocsTitle'), dataIndex: 'externalDocsTitle' },
      { label: 'externalDocs', dataIndex: 'externalDocs', fullWidthContent: true } as any
    ].filter(Boolean)
  ];
});
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
            v-if="!props.disabled && state.info.status?.value !== ApiStatus.RELEASED"
            icon="icon-shuxie"
            class="ml-2 text-text-link"
            @click="toggleEditName" />
        </template>
      </template>
      <template #operationId="{text}">
        <template v-if="editOperationId">
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
            v-if="!props.disabled && state.info.status?.value !== ApiStatus.RELEASED"
            icon="icon-shuxie"
            class="ml-2 text-text-link"
            @click="toggleEditOperationId" />
        </template>
      </template>
      <template #auth="{text}">
        {{ text ? t('service.groupApiDetail.columns.hasPermission') : t('service.groupApiDetail.columns.noPermission') }}
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
            ref="ownerSelectRef"
            class="w-50"
            size="small"
            :value="state.info.ownerId"
            @change="handleOwnerIdChange"
            @blur="ownerNameBlur" />
        </template>
        <template v-else>
          {{ text }}
          <span v-show="(!disabled && state.info.status?.value !== ApiStatus.RELEASED)">
            <Icon
              icon="icon-shuxie"
              class="ml-2 text-text-link"
              @click="toggleOwnerName" />
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
            v-show="(!disabled && state.info.status?.value !== ApiStatus.RELEASED)"
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
            v-if="!props.disabled && state.info.status?.value !== ApiStatus.RELEASED"
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
        :enumKey="'ApiPermission'"
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
        :isEdit="editDescription"
        :value="state.info.description"
        @ok="handleSaveDesc" />
    </AsyncComponent>
  </div>
</template>
