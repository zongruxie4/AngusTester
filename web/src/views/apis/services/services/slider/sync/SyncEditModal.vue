<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Hints, Icon, IconRequired, Input, notification, Select, Modal } from '@xcan-angus/vue-ui';
import { Radio, RadioGroup, Switch, Tooltip } from 'ant-design-vue';
import { services } from '@/api/tester';
import { regexpUtils, utils, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';
import { SyncConfigInfo, SyncAuthInfo } from './types';
import { StrategyWhenDuplicated } from '@/enums/enums';

type CheckedType = typeof Switch.props.checked.type;

interface Props {
  syncData?: SyncConfigInfo;
  visible: boolean;
  serviceId: string;
  allNames: string[]
}
const props = withDefaults(defineProps<Props>(), {
  syncData: undefined,
  visible: false,
  serviceId: '',
  allNames: () => ([])
});

const { t } = useI18n();

const emits = defineEmits<{(e: 'update:visible', boolean): void; (e: 'ok'):void}>();

// Blank template for creating a new sync configuration
const newData:SyncConfigInfo = {
  id: utils.uuid('api'),
  syncSource: 'OpenAPI',
  projectId: '',
  name: '',
  apiDocsUrl: '',
  strategyWhenDuplicated: {
    value: StrategyWhenDuplicated.COVER,
    message: ''
  },
  deleteWhenNotExisted: false,
  auths: [],
  lastModifiedBy: '',
  lastModifiedByName: '',
  lastModifiedDate: '',
  auth: false,
  isEdit: true,
  isExpand: true,
  isAdd: true,
  testLoading: false,
  delLoading: false,
  syncLoading: false,
  saveLoading: false,
  nameErr: false,
  apiDocsUrlErr: {
    emptyUrl: false,
    errUrl: false
  }
};

const inOptions = ref([{ label: 'header', value: 'header' }, { label: 'query', value: 'query' }]);

const loading = ref(false);

// Validate sync name non-empty
const syncNameChange = (value:string):void => {
  sync.value.nameErr = !value;
};

// Validate apiDocsUrl; debounce user input
const syncUrlChange = debounce(duration.search, (value:string):void => {
  if (!value) {
    sync.value.apiDocsUrlErr.emptyUrl = true;
    return;
  }

  sync.value.apiDocsUrlErr.emptyUrl = false;
  if (regexpUtils.isUrl(value)) {
    sync.value.apiDocsUrlErr.errUrl = false;
    return;
  }
  sync.value.apiDocsUrlErr.errUrl = true;
});

// Validate required fields; return true when any invalid
const getCheckDataResult = (_data:SyncConfigInfo):boolean => {
  let hasEmpty = false;
  if (!_data.name) {
    _data.nameErr = true;
    hasEmpty = true;
  }
  if (!_data.apiDocsUrl) {
    _data.apiDocsUrlErr.emptyUrl = true;
    hasEmpty = true;
  }

  hasEmpty = getAuthCheckRes(_data.auths);

  if (!_data.apiDocsUrl) {
    _data.apiDocsUrlErr.emptyUrl = true;
    hasEmpty = true;
  }

  _data.apiDocsUrlErr.emptyUrl = false;
  if (!regexpUtils.isUrl(_data.apiDocsUrl)) {
    _data.apiDocsUrlErr.errUrl = true;
    hasEmpty = true;
  }

  return hasEmpty;
};

// Check whether current data differs from original props
const chenkUpdate = (newData:SyncConfigInfo) => {
  const _oldData = props.syncData;
  if (!_oldData) {
    return true;
  }
  if (_oldData.name !== newData.name) {
    return true;
  }

  if (_oldData.apiDocsUrl !== newData.apiDocsUrl) {
    return true;
  }

  if (_oldData.deleteWhenNotExisted !== newData.deleteWhenNotExisted) {
    return true;
  }
  if (_oldData.strategyWhenDuplicated.value !== newData.strategyWhenDuplicated.value) {
    return true;
  }

  const oldAuth = _oldData.auths.map(item => ({ keyName: item.keyName, value: item.value, in: item.in }));
  const newAuth = newData.auths.map(item => ({ keyName: item.keyName, value: item.value, in: item.in }));
  return !utils.deepCompare(oldAuth, newAuth);
};

// Save current configuration (create/update)
const handleSave = async () => {
  const checkRes = getCheckDataResult(sync.value);
  if (checkRes) {
    return;
  }
  if (props.allNames.includes(sync.value.name)) {
    notification.warning(t('service.syncConfig.messages.nameExists'));
    sync.value.nameErr = true;
    return;
  }
  if (!chenkUpdate(sync.value)) {
    cancel();
    return;
  }

  const params = {
    apiDocsUrl: sync.value.apiDocsUrl,
    deleteWhenNotExisted: sync.value.deleteWhenNotExisted,
    name: sync.value.name,
    strategyWhenDuplicated: sync.value.strategyWhenDuplicated.value,
    auths: sync.value.auths.length
      ? sync.value.auths.map(item => ({
        keyName: item.keyName,
        value: item.value,
        in: item.in.value
      }))
      : null
  };

  loading.value = true;
  const [error] = await services.putServicesSyncConfig(props.serviceId, params);
  loading.value = false;
  if (error) {
    return false;
  }
  notification.success(t('actions.tips.saveSuccess'));
  emits('ok');
};

// Validate auth key name non-empty
const keyNameChange = (value:string, auth:SyncAuthInfo):void => {
  auth.keyNameErr = !value;
};

// Toggle auth section; when on, provide one empty row
const openAuth = (checked:CheckedType) => {
  if (checked) {
    sync.value.auths = [{
      keyName: '',
      value: '',
      in: { value: 'header', message: 'header' },
      keyNameErr: false,
      valueErr: false
    }];
    return;
  }

  sync.value.auths = [];
};

// Validate auth value non-empty
const authValueChange = (value:string, auth:SyncAuthInfo):void => {
  auth.valueErr = !value;
};

// Remove one auth row
const deleteAuth = (authIndex:number) => {
  sync.value.auths.splice(authIndex, 1);
  if (!sync.value.auths.length) {
    sync.value.auth = false;
  }
};
// Check all auth rows; return true if any row has empty fields
const getAuthCheckRes = (auths:SyncAuthInfo[]):boolean => {
  let hasEmpty = false;
  auths.forEach((auth) => {
    if (!auth.keyName) {
      auth.keyNameErr = true;
      hasEmpty = true;
    }
    if (!auth.value) {
      auth.valueErr = true;
      hasEmpty = true;
    }
  });
  return hasEmpty;
};

// Add a new auth row if last row is complete and unique
const addAuth = () => {
  const hasEmpty = getAuthCheckRes(sync.value.auths);
  if (hasEmpty) {
    return;
  }

  if (sync.value.auths.length > 1) {
    const len = sync.value.auths.filter(item => item.keyName === sync.value.auths[sync.value.auths.length - 1].keyName)?.length;
    if (len >= 2) {
      notification.warning(t('service.syncConfig.messages.authNameExists'));
      return;
    }
  }
  sync.value.auths.push({
    keyName: '',
    value: '',
    in: { value: 'header', message: 'header' },
    keyNameErr: false,
    valueErr: false
  });
};

// Close modal
const cancel = () => {
  emits('update:visible', false);
};

const sync = ref({ ...newData });

// Re-initialize form state when modal is opened
watch(() => props.visible, (newValue) => {
  if (newValue) {
    if (props.syncData) {
      sync.value = JSON.parse(JSON.stringify(props.syncData));
    } else {
      sync.value = { ...newData };
    }
  }
}, {
  immediate: true
});
</script>
<template>
  <Modal
    :title="props.syncData ? t('actions.add') : t('actions.edit')"
    :okButtonProps="{
      loading
    }"
    :visible="props.visible"
    @cancel="cancel"
    @ok="handleSave">
    <div
      class="transition-height duration-500 overflow-hidden leading-3">
      <div class="flex flex-col">
        <span><IconRequired />{{ t('common.name') }}</span>
        <Input
          v-model:value="sync.name"
          :placeholder="t('common.placeholders.searchKeyword')"
          size="small"
          class="mt-2 mb-5"
          :maxlength="100"

          :error="sync?.nameErr"
          @change="(event)=>syncNameChange(event.target.value)" />
        <span><IconRequired />{{ t('common.source') }}</span>
        <Select
          value="OpenAPI"
          size="small"
          disabled
          class="mt-2 mb-5"
          :allowClear="false" />
        <span><IconRequired />{{ t('common.address') }}</span>
        <div class="relative mt-2 mb-5">
          <Input
            v-model:value="sync.apiDocsUrl"
            :placeholder="t('service.syncConfig.form.addressPlaceholder')"
            size="small"
            :maxlength="400"
            :error="sync?.apiDocsUrlErr.emptyUrl || sync?.apiDocsUrlErr.errUrl"
            @change="syncUrlChange($event.target.value)" />
          <div class="absolute text-rule text-3">
            <template v-if="sync?.apiDocsUrlErr.errUrl">{{ t('service.syncConfig.messages.inputContent') }}</template>
          </div>
        </div>
        <span>{{ t('service.syncConfig.form.duplicateStrategy') }}</span>
        <RadioGroup
          v-model:value="sync.strategyWhenDuplicated.value"
          class="mt-2 mb-5">
          <Radio :value="StrategyWhenDuplicated.COVER">{{ t('actions.cover') }}</Radio>
          <Radio :value="StrategyWhenDuplicated.IGNORE">{{ t('actions.ignore') }}</Radio>
        </RadioGroup>
        <span>{{ t('service.syncConfig.form.deleteWhenNotExist') }}</span>
        <RadioGroup
          v-model:value="sync.deleteWhenNotExisted"
          class="mt-2 mb-5">
          <Radio :value="true">{{ t('status.yes') }}</Radio>
          <Radio :value="false">{{ t('status.no') }}</Radio>
        </RadioGroup>
        <div class="flex items-center">
          <span class="mr-3.5">{{ t('common.auth') }}</span>
          <Switch
            v-model:checked="sync.auth"
            size="small"
            class="w-8 mr-2"
            @change="(checked:CheckedType)=>openAuth(checked)" />
        </div>
        <Hints :text="t('service.syncConfig.form.authRequired')" class="mt-0.5" />
        <template v-if="sync?.auth">
          <div class="mt-2">
            <div
              v-for="auth,aindex in sync?.auths"
              :key="aindex"
              class="flex mb-5 items-center">
              <div class="flex flex-col space-y-3 flex-none mr-2">
                <div class="h-7 leading-7">{{ t('service.syncConfig.form.paramName') }}</div>
                <div class="h-7 leading-7">{{ t('service.syncConfig.form.paramPosition') }}</div>
                <div class="h-7 leading-7">{{ t('service.syncConfig.form.paramValue') }}</div>
              </div>
              <div class="flex flex-col flex-1 space-y-3">
                <Input
                  v-model:value="auth.keyName"
                  :error="auth.keyNameErr"
                  :maxlength="400"
                  :placeholder="t('service.syncConfig.form.paramName')"
                  size="small"
                  @change="(event)=>keyNameChange(event.target.value,auth)" />
                <Select
                  v-model:value="auth.in.value"
                  :options="inOptions"
                  size="small"
                  :placeholder="t('service.syncConfig.form.paramPosition')" />
                <Input
                  v-model:value="auth.value"
                  :error="auth.valueErr"
                  :maxlength="1024"
                  :placeholder="t('service.syncConfig.form.paramValue')"
                  size="small"
                  @change="(event)=>authValueChange(event.target.value,auth)" />
              </div>
              <div class="flex-none flex flex-col ml-2 space-y-1">
                <Tooltip :title="t('service.syncConfig.actions.deleteAuth')" placement="top">
                  <Icon
                    icon="icon-jianshao"
                    class="text-5 mr-1 cursor-pointer text-text-sub-content hover:text-text-link-hover"
                    @click="deleteAuth(aindex)" />
                </Tooltip>
                <template v-if="aindex == sync?.auths.length-1 && sync?.auths.length <= 9 ">
                  <template v-if="auth.value && auth.keyName">
                    <Tooltip :title="t('service.syncConfig.actions.addAuth')" placement="top">
                      <Icon
                        icon="icon-tianjia"
                        class="text-5 cursor-pointer text-text-sub-content hover:text-text-link-hover"
                        @click="addAuth()" />
                    </Tooltip>
                  </template>
                  <template v-else>
                    <Tooltip :title="t('service.syncConfig.messages.inputContent')" placement="top">
                      <Icon
                        icon="icon-tianjia"
                        class="text-5 cursor-not-allowed text-text-placeholder" />
                    </Tooltip>
                  </template>
                </template>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
  </Modal>
</template>
