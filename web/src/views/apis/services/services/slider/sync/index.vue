<script setup lang="ts">
import { inject, onMounted, ref, watch, defineAsyncComponent, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Arrow, Colon, Hints, Icon, IconRequired, Input, notification, Select, Spin, AsyncComponent, VuexHelper } from '@xcan-angus/vue-ui';
import { Button, Radio, RadioGroup, Switch, Tooltip } from 'ant-design-vue';
import { services } from '@/api/tester';
import { regexpUtils, utils, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { SyncAuthInfo, SyncConfigInfo } from './types';
import { StrategyWhenDuplicated } from '@/enums/enums';

const EditModal = defineAsyncComponent(() => import('./SyncEditModal.vue'));

type CheckedType = typeof Switch.props.checked.type;

interface Props {
  id: string;
  disabled: boolean;
  source:'modal' | 'home' | 'right';
  vertical: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  disabled: false,
  source: 'right',
  vertical: true
});

const { t } = useI18n();

const { useState } = VuexHelper;

const emit = defineEmits<{(e: 'deleteSuccess'): void, (e: 'saveSuccess'): void}>();

// Demo sync configuration (pre-filled for users to try quickly)
const demoData:SyncConfigInfo = {
  id: utils.uuid('api'),
  syncSource: 'OpenAPI',
  projectId: '',
  name: 'demo',
  apiDocsUrl: 'http://dev-api.xxx.com/v3/api-docs',
  strategyWhenDuplicated: {
    value: StrategyWhenDuplicated.COVER,
    message: ''
  },
  deleteWhenNotExisted: false,
  auths: [
    {
      keyName: 'Authorization',
      value: 'basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==',
      in: { value: 'header', message: 'header' },
      keyNameErr: false,
      valueErr: false
    }
  ],
  lastModifiedBy: '',
  lastModifiedByName: '',
  lastModifiedDate: '',
  auth: true,
  isExpand: true,
  isEdit: true,
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

// Blank template for new sync configuration
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

const loading = ref(false);
const syncList = ref<SyncConfigInfo[]>([]);
const oldSyncList = ref<SyncConfigInfo[]>([]);
// Load synchronization configurations for current service
const getSynchronizationList = async () => {
  loading.value = true;
  const [error, { data }] = await services.getServicesSyncConfig(props.id);
  loading.value = false;
  if (error) {
    return;
  }
  // If no history data exists, show one new blank row by default
  if (!data?.length) {
    syncList.value = [JSON.parse(JSON.stringify(newData))];
    // Record the data being edited
    currEditData.value = syncList.value[0];
    addBtnDisabled.value = true;
    return;
  }

  syncList.value = data.map(item => ({
    ...item,
    id: utils.uuid('api'),
    syncSource: 'OpenAPI',
    strategyWhenDuplicated: item.strategyWhenDuplicated,
    auths: item.auths || [],
    auth: !!item.auths?.length,
    isEdit: false,
    isAdd: false,
    isExpand: false,
    testLoading: false,
    delLoading: false,
    syncLoading: false,
    saveLoading: false,
    nameErr: false,
    apiDocsUrlErr: {
      emptyUrl: false,
      errUrl: false
    }
  }));
  // Snapshot current list for change detection later
  oldSyncList.value = JSON.parse(JSON.stringify(syncList.value));
  // Enable Add button
  addBtnDisabled.value = false;
};

// Add button disabled status
const addBtnDisabled = ref(false);

// Add a new sync configuration row at the top
const addSyncInfo = () => {
  currEditData.value = undefined;

  if (getChenkUpdateRes()) {
    return;
  }

  if (syncList.value[0].isAdd) {
    syncList.value[0] = { ...JSON.parse(JSON.stringify(newData)), id: syncList.value[0].id };
    return;
  }
  // Insert a new row at the list top
  syncList.value.unshift(JSON.parse(JSON.stringify(newData)));
  // Mark the current row as editing (required by edit logic)
  currEditData.value = syncList.value[0];
  // Disable add button after insertion
  addBtnDisabled.value = true;
  setEditFalseExceptId(syncList.value, currEditData.value.id);
};

// Demo button enable/disable rule: Only the first add row can be replaced by demo
const addDemoBtnDisabled = ref(false);
// Append demo configuration as first row
const addSyncInfoDemo = () => {
  if (syncList.value[0].isAdd) {
    syncList.value[0] = { ...JSON.parse(JSON.stringify(demoData)), id: utils.uuid('api') };
    currEditData.value = syncList.value[0];
    return;
  }
  if (getChenkUpdateRes()) {
    return;
  }
  // Insert a demo row at the beginning
  syncList.value.unshift({ ...JSON.parse(JSON.stringify(demoData)), id: utils.uuid('api') });
  // Remember previous expand state
  lastIsExpandState.value = syncList.value[0].isExpand;
  // Mark the current row as editing (required by edit logic)
  currEditData.value = syncList.value[0];
  // Enable demo add button
  addDemoBtnDisabled.value = false;
  setEditFalseExceptId(syncList.value, currEditData.value.id);
};

// Add an auth pair (key/value)
const addAuth = (sync:SyncConfigInfo) => {
  const hasEmpty = getAuthCheckRes(sync.auths);
  if (hasEmpty) {
    return;
  }

  if (sync.auths.length > 1) {
    const len = sync.auths.filter(item => item.keyName === sync.auths[sync.auths.length - 1].keyName)?.length;
    if (len >= 2) {
      notification.warning(t('service.syncConfig.messages.authNameExists'));
      return;
    }
  }
  sync.auths.push({
    keyName: '',
    value: '',
    in: { value: 'header', message: 'header' },
    keyNameErr: false,
    valueErr: false
  });
};
// Remove an auth pair by index
const deleteAuth = (sync:SyncConfigInfo, authIndex:number) => {
  sync.auths.splice(authIndex, 1);
  if (!sync.auths.length) {
    sync.auth = false;
  }
};

// Delete a sync configuration
const handleDelSync = async (sync:SyncConfigInfo) => {
  if (sync.delLoading || sync.isAdd) {
    return;
  }
  loading.value = true;
  const [error] = await services.delSyncInfo(props.id, [sync.name]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.deleteSuccess'));
  emit('deleteSuccess');
  syncList.value = syncList.value.filter(item => item.id !== sync.id);
  oldSyncList.value = oldSyncList.value.filter(item => item.id !== sync.id);
  // If list is empty after deletion, add one blank row
  if (syncList.value.length === 0) {
    syncList.value.unshift({ ...JSON.parse(JSON.stringify(newData)), id: utils.uuid('api') });
  }
};

// Test the connectivity of apiDocsUrl (and auth headers if provided)
const handleTest = async (sync:SyncConfigInfo) => {
  if (!sync.apiDocsUrl) {
    sync.apiDocsUrlErr.emptyUrl = true;
    return;
  }

  const params:{apiDocsUrl:string, auths?: {in: 'header' |'query'; keyName: string; value: string;}[]} = { apiDocsUrl: sync.apiDocsUrl };

  if (sync.auths.length) {
    const hasEmpty = getAuthCheckRes(sync.auths);
    if (hasEmpty) {
      return;
    }
    params.auths = sync.auths.map(auth => ({ in: auth.in.value, keyName: auth.keyName, value: auth.value }));
  }

  loading.value = true;
  const [error] = await services.loadSynchronizationTest(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('service.syncConfig.messages.testSuccess'));
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/no-unused-vars
const updateApiGroup = inject('updateApiGroup', (_id) => undefined);

// Execute synchronization for a single configuration by name
const handleSync = async (sync:SyncConfigInfo) => {
  loading.value = true;
  const [error] = await services.postSynchronizationExec(props.id, sync.name);
  loading.value = false;

  if (error) {
    return;
  }

  notification.success(t('service.syncConfig.messages.syncSuccess'));
  updateApiGroup(props.id);
  getSynchronizationList();
};

// Save a configuration (create or update)
const handleSave = async (sync:SyncConfigInfo) => {
  // Validate empties
  const checkRes = getCheckDataResult(sync);
  if (checkRes) {
    return;
  }
  // For new rows, ensure name is unique
  if (sync.isAdd) {
    const len = syncList.value.filter(item => item.name === sync.name)?.length;
    if (len >= 2) {
      notification.warning(t('service.syncConfig.messages.nameExists'));
      sync.nameErr = true;
      return;
    }
  } else {
    // For existing rows, skip save if nothing changed
    if (!chenkUpdate(sync)) {
      sync.isEdit = false;
      sync.isExpand = lastIsExpandState.value;
      return;
    }
  }

  const params = {
    apiDocsUrl: sync.apiDocsUrl,
    deleteWhenNotExisted: sync.deleteWhenNotExisted,
    name: sync.name,
    strategyWhenDuplicated: sync.strategyWhenDuplicated.value,
    auths: sync.auths.length
      ? sync.auths.map(item => ({
        keyName: item.keyName,
        value: item.value,
        in: item.in.value
      }))
      : null
  };

  loading.value = true;
  const [error] = await services.putServicesSyncConfig(props.id, params);
  loading.value = false;
  if (error) {
    return false;
  }
  currEditData.value = undefined;
  notification.success(t('actions.tips.saveSuccess'));
  emit('saveSuccess');
  getSynchronizationList();
};

// Validate required fields; return true if any invalid
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

  // Check if any auth entries are empty
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

// Execute synchronization for all existing configurations
const handleSyncAll = async () => {
  const hasSyncData = syncList.value.filter(item => !item.isAdd);
  if (!hasSyncData.length) {
    notification.warning(t('service.syncConfig.messages.noValidConfig'));
    return;
  }
  loading.value = true;
  const [error] = await services.postSynchronizationExec(props.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('service.syncConfig.messages.syncAllSuccess'));
  updateApiGroup(props.id);
  getSynchronizationList();
};

// Track the currently editing item (only one at a time allowed)
const currEditData = ref<SyncConfigInfo>();

// Expand/collapse row and toggle edit
const handleExpand = (event, sync:SyncConfigInfo) => {
  event.stopPropagation();
  const hasEditData = syncList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    if (hasEditData[0].isAdd) {
      syncList.value = syncList.value.filter(item => item.id !== hasEditData[0].id);
    } else {
      const checkRes = getCheckDataResult(hasEditData[0]);
      if (checkRes) {
        return;
      }
      if (getChenkUpdateRes()) {
        return;
      }
    }
  }
  sync.isExpand = !sync.isExpand;
  if (!sync.isExpand) {
    sync.isEdit = false;
  }
  setEditFalseExceptId(syncList.value, sync.id);
  currEditData.value = undefined;
  addBtnDisabled.value = false;
};

// Guard: warn if there are unsaved changes
const getChenkUpdateRes = () => {
  if (currEditData.value) {
    const hasUpdate = chenkUpdate(syncList.value.filter(item => item.id === currEditData.value?.id)[0]);
    if (hasUpdate) {
      notification.warning(t('service.syncConfig.messages.dataNotSaved'));
      return true;
    }
  }
  return false;
};

const lastIsExpandState = ref(false);
// Enter edit mode and keep last expand state
const handleEdit = (event, sync:SyncConfigInfo) => {
  // if (!props.vertical) {
  //   openEditModal(sync);
  //   return;
  // }
  event.stopPropagation();
  const hasEditData = syncList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    if (hasEditData[0].isAdd) {
      syncList.value = syncList.value.filter(item => item.id !== hasEditData[0].id);
    } else {
      const checkRes = getCheckDataResult(hasEditData[0]);
      if (checkRes) {
        return;
      }
      if (getChenkUpdateRes()) {
        return;
      }
    }
  }
  lastIsExpandState.value = sync.isExpand;
  sync.isEdit = true;
  sync.isExpand = true;
  currEditData.value = sync;
  setEditFalseExceptId(syncList.value, sync.id);
  addBtnDisabled.value = false;
};

// Cancel editing; if new row, remove it, else restore snapshot
const cancelEdit = (event, sync:SyncConfigInfo) => {
  event.stopPropagation();
  // If cancelling a newly added row
  if (sync.isAdd) {
    // If this is the only row and is new, do not allow cancel; keep expanded and editing
    if (syncList.value.length === 1) {
      return;
    }
    // Otherwise, remove the new row and re-enable add button
    syncList.value = syncList.value.filter(item => item.id !== sync.id);
    addBtnDisabled.value = false;
    currEditData.value = undefined;
    return;
  }

  // If cancelling an existing row: check changes, then collapse and exit edit
  const hasUpdate = chenkUpdate(sync);
  // If changed, restore from snapshot
  if (hasUpdate) {
    const oldSync = oldSyncList.value.find(item => item.id === sync.id);
    for (let i = 0; i < syncList.value.length; i++) {
      if (sync.id === syncList.value[i].id) {
        syncList.value[i] = oldSync ? JSON.parse(JSON.stringify(oldSync)) : sync;
        break;
      }
    }
  }
  sync.isEdit = false;
  sync.isExpand = lastIsExpandState.value;
  currEditData.value = undefined;
};

// Check whether current row data differs from the snapshot
const chenkUpdate = (newData:SyncConfigInfo) => {
  const _oldDataList = oldSyncList.value.filter(item => item?.id === newData?.id);
  if (!_oldDataList?.length) {
    return true;
  }
  const _oldData = _oldDataList[0];
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

// Collapse and disable edit for all rows except the given id
const setEditFalseExceptId = (arr:SyncConfigInfo[], id:string):void => {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i].id !== id) {
      arr[i].isEdit = false;
      arr[i].isExpand = false;
    }
  }
};

// Options for where to inject auth (header/query)
const inOptions = ref([{ label: 'header', value: 'header' }, { label: 'query', value: 'query' }]);

const getShowEmptyingexample = (sync:SyncConfigInfo) => {
  return utils.deepCompare(sync, { ...demoData, id: sync.id });
};

// Clear demo example and revert to blank template
const emptyingExample = e => {
  e.stopPropagation();
  syncList.value[0] = { ...JSON.parse(JSON.stringify(newData)), id: syncList.value[0].id };
};

// Toggle auth section; when true, provide one empty row
const openAuth = (checked:CheckedType, sync:SyncConfigInfo) => {
  if (checked) {
    sync.auths = [{
      keyName: '',
      value: '',
      in: { value: 'header', message: 'header' },
      keyNameErr: false,
      valueErr: false
    }];
    return;
  }

  sync.auths = [];
};

// Validate sync name non-empty
const syncNameChange = (value:string, sync:SyncConfigInfo):void => {
  sync.nameErr = !value;
};
// Validate apiDocsUrl; debounce user input
const syncUrlChange = debounce(duration.search, (value:string, sync:SyncConfigInfo):void => {
  if (!value) {
    sync.apiDocsUrlErr.emptyUrl = true;
    return;
  }

  sync.apiDocsUrlErr.emptyUrl = false;
  if (regexpUtils.isUrl(value)) {
    sync.apiDocsUrlErr.errUrl = false;
    return;
  }
  sync.apiDocsUrlErr.errUrl = true;
});

// Validate auth key name non-empty
const keyNameChange = (value:string, auth:SyncAuthInfo):void => {
  auth.keyNameErr = !value;
};

// Validate auth value non-empty
const authValueChange = (value:string, auth:SyncAuthInfo):void => {
  auth.valueErr = !value;
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

const { notify } = useState(['notify'], 'apiSyncStore');

// Tooltip container bound to input wrapper
const getPopupContainer = (el: HTMLElement): HTMLElement => {
  if (el.parentElement) {
    return el.parentElement;
  }
  return document.body;
};

const editVisible = ref(false);
const allSyncName = computed(() => {
  if (editSyncData.value) {
    return syncList.value.filter(i => i.name !== editSyncData.value.name).map(i => i.name);
  }
  return syncList.value.map(i => i.name);
});
const editSyncData = ref();

// Open modal editor (currently unused entry preserved for potential non-vertical layout)
const openEditModal = (sync = undefined) => {
  editVisible.value = true;
  editSyncData.value = sync;
};

const handleSaveOk = () => {
  editVisible.value = false;
  emit('saveSuccess');
  getSynchronizationList();
};

onMounted(() => {
  getSynchronizationList();
});

watch(() => notify.value, () => {
  if (props.source === 'modal') {
    return;
  }
  getSynchronizationList();
});
</script>
<template>
  <Spin class="h-full flex flex-col" :spinning="loading">
    <Hints :text="t('service.syncConfig.hints')" />
    <div class="flex justify-between mt-2  pr-2">
      <div class="flex">
        <Button
          size="small"
          type="primary"
          class="flex items-center mr-2"
          :disabled="props.disabled || syncList.length > 9 || addBtnDisabled"
          @click="addSyncInfo">
          <Icon icon="icon-jia" class="mr-1" />
          {{ t('actions.add') }}
        </Button>
        <Button
          size="small"
          class="flex items-center"
          :disabled="props.disabled || syncList.filter(f=>!f.isAdd).length > 9 || addDemoBtnDisabled"
          @click="addSyncInfoDemo">
          <Icon icon="icon-jia" class="mr-1" />
          {{ t('service.syncConfig.tips.addDemo') }}
          <Tooltip :title="t('service.syncConfig.tips.addDemo')" placement="top">
            <Icon icon="icon-tishi1" class="ml-1 text-tips text-3.5" />
          </Tooltip>
        </Button>
      </div>
      <Button
        size="small"
        class="flex items-center"
        :disabled="props.disabled"
        @click="handleSyncAll">
        <Icon icon="icon-huifu" class="mr-1 -mt-0.5" />
        {{ t('actions.syncAll') }}
        <Tooltip :title="t('service.syncConfig.tips.syncAllTip')" placement="top">
          <Icon icon="icon-tishi1" class="ml-1 text-tips text-3.5" />
        </Tooltip>
      </Button>
    </div>
    <div
      style="scrollbar-gutter: stable;"
      class="overflow-y-auto flex text-3 text-text-content "
      :class="[props.vertical ? 'flex-col space-y-2 -mr-3.5 flex-1 pr-1.75' : 'flex-wrap mt-3']">
      <div
        v-for="sync in syncList"
        :key="sync.id"
        :style="{width: props.vertical ? '100%' : 'calc(33.33% - 8px)', height: 'fit-content'}"
        :class="[props.vertical ? 'mt-2' : 'mr-2 flex-shrink-0 mb-2']"
        class="border border-border-divider p-2 rounded ">
        <div v-if="!sync.isAdd && !sync.isEdit" class="flex justify-between">
          <div class="flex items-center relative">
            <div
              class="cursor-pointer truncate text-text-title"
              style="max-width: 220px;"
              :title="sync.name">
              <Tooltip
                :getPopupContainer="getPopupContainer"
                :overlayStyle="{whiteSpace:'pre-wrap'}"
                :title="sync.name"
                placement="topLeft">
                {{ sync.name }}
              </Tooltip>
            </div>
            <template v-if="sync.syncFailureCause">
              <Tooltip :title="sync.syncFailureCause" placement="bottom">
                <Icon
                  icon="icon-jinggao"
                  class="text-3 -mt-0.5 ml-1 text-rule" />
              </Tooltip>
            </template>
          </div>
          <Arrow
            :open="sync.isExpand"
            @click="(e) => handleExpand(e, sync)" />
        </div>
        <div v-if="!sync.isAdd && !sync.isEdit" class="w-full break-all whitespace-pre-wrap text-content mt-2 leading-4">{{ sync.apiDocsUrl }}</div>
        <div v-if="!sync.isAdd && !sync.isEdit" class="flex items-center justify-between mt-2">
          <div class="text-3 text-text-sub-content">
            {{ t('service.syncConfig.tips.lastSync') }}<Colon class="ml-0.5 mr-2" />{{ sync?.lastSyncDate || "--" }}
          </div>
          <div class="text-3 flex-none ml-2 -mt-1">
            <Tooltip :title="t('actions.edit')" placement="top">
              <template v-if="props.disabled">
                <Icon
                  icon="icon-shuxie"
                  class="mr-1 cursor-not-allowed text-text-disabled" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-shuxie"
                  class="mr-1"
                  :class="sync.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                  @click="(e) => handleEdit(e, sync)" />
              </template>
            </Tooltip>
            <Tooltip :title="t('actions.sync')" placement="top">
              <template v-if="props.disabled || sync.syncLoading || sync.isEdit || sync.isAdd">
                <Icon
                  icon="icon-huifu"
                  class="text-text-placeholder cursor-not-allowed mr-1" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-huifu"
                  class="mr-1"
                  :class="sync.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                  @click="handleSync(sync)" />
              </template>
            </Tooltip>
            <Tooltip :title="t('service.syncConfig.actions.test')" placement="top">
              <template v-if="props.disabled">
                <Icon
                  icon="icon-zhihangceshi"
                  class="text-text-placeholder cursor-not-allowed mr-1" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-zhihangceshi"
                  class="mr-1"
                  :class="sync.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                  @click="handleTest(sync)" />
              </template>
            </Tooltip>
            <Tooltip :title="t('actions.delete')" placement="top">
              <template v-if="props.disabled">
                <Icon
                  icon="icon-qingchu"
                  class="text-3.5 mr-1 cursor-not-allowed text-text-disabled" />
              </template>
              <template v-else>
                <Icon
                  icon="icon-qingchu"
                  class="text-3.5"
                  :class="sync.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                  @click="handleDelSync(sync)" />
              </template>
            </Tooltip>
          </div>
        </div>
        <div
          :class="sync.isExpand ? 'open-info' : 'stop-info'"
          class="transition-height duration-500 overflow-hidden leading-3">
          <div class="flex flex-col" :class="!sync.isEdit?'border-t border-dashed border-border-divider pt-2 mt-2':''">
            <span><IconRequired />{{ t('common.name') }}</span>
            <Input
              v-model:value="sync.name"
              :placeholder="t('common.placeholders.searchKeyword')"
              size="small"
              class="mt-2 mb-5"
              :maxlength="100"
              :disabled="!sync.isAdd || !sync.isEdit"
              :error="sync.nameErr"
              @change="(event)=>syncNameChange(event.target.value,sync)" />
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
                :placeholder="t('common.address')"
                size="small"
                :maxlength="400"
                :disabled="!sync.isEdit"
                :error="sync.apiDocsUrlErr.emptyUrl || sync.apiDocsUrlErr.errUrl"
                @change="syncUrlChange($event.target.value,sync)" />
              <div class="absolute text-rule text-3">
                <template v-if="sync.apiDocsUrlErr.errUrl">{{ t('service.syncConfig.messages.inputContent') }}</template>
              </div>
            </div>
            <span>{{ t('service.syncConfig.form.duplicateStrategy') }}</span>
            <RadioGroup
              v-model:value="sync.strategyWhenDuplicated.value"
              class="mt-2 mb-5"
              :disabled="!sync.isEdit">
              <Radio :value="StrategyWhenDuplicated.COVER">{{ t('actions.cover') }}</Radio>
              <Radio :value="StrategyWhenDuplicated.IGNORE">{{ t('actions.ignore') }}</Radio>
            </RadioGroup>
            <span>{{ t('service.syncConfig.form.deleteWhenNotExist') }}</span>
            <RadioGroup
              v-model:value="sync.deleteWhenNotExisted"
              class="mt-2 mb-5"
              :disabled="!sync.isEdit">
              <Radio :value="true">{{ t('status.yes') }}</Radio>
              <Radio :value="false">{{ t('status.no') }}</Radio>
            </RadioGroup>
            <div class="flex items-center">
              <span class="mr-3.5">{{ t('actions.permission') }}</span>
              <Switch
                v-model:checked="sync.auth"
                size="small"
                class="w-8 mr-2"
                :disabled="!sync.isEdit"
                @change="(checked:CheckedType)=>openAuth(checked,sync)" />
            </div>
            <Hints :text="t('service.syncConfig.form.authRequired')" class="mt-0.5" />
            <template v-if="sync.auth">
              <div class="mt-2">
                <div
                  v-for="auth,aindex in sync.auths"
                  :key="aindex"
                  class="flex mb-5 items-center">
                  <div class="flex flex-col space-y-3 flex-none mr-2">
                    <div class="h-7 leading-7">{{ t('common.parameterName') }}</div>
                    <div class="h-7 leading-7">{{ t('service.syncConfig.form.paramPosition') }}</div>
                    <div class="h-7 leading-7">{{ t('common.parameterValue') }}</div>
                  </div>
                  <div class="flex flex-col flex-1 space-y-3">
                    <Input
                      v-model:value="auth.keyName"
                      :disabled="!sync.isEdit"
                      :error="auth.keyNameErr"
                      :maxlength="400"
                      :placeholder="t('common.placeholders.searchKeyword')"
                      size="small"
                      @change="(event)=>keyNameChange(event.target.value,auth)" />
                    <Select
                      v-model:value="auth.in.value"
                      :options="inOptions"
                      :disabled="!sync.isEdit"
                      size="small"
                      :placeholder="t('common.position')" />
                    <Input
                      v-model:value="auth.value"
                      :disabled="!sync.isEdit"
                      :error="auth.valueErr"
                      :maxlength="1024"
                      :placeholder="t('common.value')"
                      size="small"
                      @change="(event)=>authValueChange(event.target.value,auth)" />
                  </div>
                  <div v-if="sync.isEdit" class="flex-none flex flex-col ml-2 space-y-1">
                    <Tooltip :title="t('service.syncConfig.actions.deleteAuth')" placement="top">
                      <Icon
                        icon="icon-jianshao"
                        class="text-5 mr-1"
                        :class="sync.isEdit?'cursor-pointer text-text-sub-content hover:text-text-link-hover':'cursor-not-allowed text-text-placeholder'"
                        @click="deleteAuth(sync,aindex)" />
                    </Tooltip>
                    <template v-if="aindex == sync.auths.length-1 && sync.auths.length <= 9 ">
                      <template v-if="auth.value && auth.keyName">
                        <Tooltip :title="t('service.syncConfig.actions.addAuth')" placement="top">
                          <Icon
                            icon="icon-tianjia"
                            class="text-5"
                            :class="sync.isEdit?'cursor-pointer text-text-sub-content hover:text-text-link-hover':'cursor-not-allowed text-text-placeholder'"
                            @click="addAuth(sync)" />
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
            <div class="flex justify-end" :class="sync.auth?'-mt-2':'mt-3'">
              <template v-if="getShowEmptyingexample(sync)">
                <Button
                  type="link"
                  size="small"
                  class="px-0"
                  :loading="sync.saveLoading"
                  @click="emptyingExample">
                  {{ t('actions.clear') }}
                </Button>
              </template>
              <template v-if="sync.isEdit">
                <Button
                  type="link"
                  size="small"
                  class="px-0 mx-2"
                  :disabled="syncList.length === 1 && sync.isAdd"
                  @click="(e) => cancelEdit(e,sync)">
                  {{ t('actions.cancel') }}
                </Button>
                <Button
                  size="small"
                  type="link"
                  class="px-0"
                  :disabled="props.disabled"
                  @click="handleSave(sync)">
                  {{ t('actions.save') }}
                </Button>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
    <AsyncComponent :visible="editVisible">
      <EditModal
        v-model:visible="editVisible"
        :allNames="allSyncName"
        :syncData="editSyncData"
        :serviceId="props.id"
        @ok="handleSaveOk" />
    </AsyncComponent>
  </Spin>
</template>
<style scoped>
.open-info {
  height: auto;
}

.stop-info {
  height: 0;
}
</style>
