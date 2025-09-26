<script setup lang="ts">
import { inject, onMounted, ref, watch, defineAsyncComponent, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Arrow, Colon, Hints, Icon, IconRequired, Input, notification, Select, Spin, AsyncComponent, VuexHelper } from '@xcan-angus/vue-ui';
import { Button, Radio, RadioGroup, Switch, Tooltip } from 'ant-design-vue';
import { services } from '@/api/tester';
import { regexpUtils, utils, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

import { AuthObj, SyncObj } from './PropsType';

type CheckedType = typeof Switch.props.checked.type;

const EditModal = defineAsyncComponent(() => import('./editModal.vue'));

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

// 示例数据
const demoData:SyncObj = {
  id: utils.uuid('api'),
  syncSource: 'OpenAPI',
  projectId: '',
  name: 'demo',
  apiDocsUrl: 'http://dev-api.xxx.com/v3/api-docs',
  strategyWhenDuplicated: {
    value: 'COVER',
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
  saveloading: false,
  nameErr: false,
  apiDocsUrlErr: {
    emptyUrl: false,
    errUrl: false
  }
};

// Empty data
const newData:SyncObj = {
  id: utils.uuid('api'),
  syncSource: 'OpenAPI',
  projectId: '',
  name: '',
  apiDocsUrl: '',
  strategyWhenDuplicated: {
    value: 'COVER',
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
  saveloading: false,
  nameErr: false,
  apiDocsUrlErr: {
    emptyUrl: false,
    errUrl: false
  }
};

const loading = ref(false);
const syncList = ref<SyncObj[]>([]);
const oldSyncList = ref<SyncObj[]>([]);
// Initialize synchronization configuration
const getSynchronizationList = async () => {
  loading.value = true;
  const [error, { data }] = await services.getServicesSyncConfig(props.id);
  loading.value = false;
  if (error) {
    return;
  }
  // If there is no historical data, an empty data is displayed by default
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
    saveloading: false,
    nameErr: false,
    apiDocsUrlErr: {
      emptyUrl: false,
      errUrl: false
    }
  }));
  // Record historical data
  oldSyncList.value = JSON.parse(JSON.stringify(syncList.value));
  // Enable Add
  addBtnDisabled.value = false;
};

// Add button disabled status
const addBtnDisabled = ref(false);

// Add a new sync configuration
const addSyncInfo = () => {
  currEditData.value = undefined;

  if (getChenkUpdateRes()) {
    return;
  }

  if (syncList.value[0].isAdd) {
    syncList.value[0] = { ...JSON.parse(JSON.stringify(newData)), id: syncList.value[0].id };
    return;
  }
  // 列表开始位置添加一条新数据
  syncList.value.unshift(JSON.parse(JSON.stringify(newData)));
  // 记录正在编辑的数据(编辑逻辑需要)
  currEditData.value = syncList.value[0];
  // 追加后禁用添加按钮
  addBtnDisabled.value = true;
  setEditFalseExceptId(syncList.value, currEditData.value.id);
};

// 示例按钮禁用启用 目前的逻辑限制只有添加一条数据 可以添加示例，修改旧数据不允许替换示例
const addDemoBtnDisabled = ref(false);
// 添加示例数据
const addSyncInfoDemo = () => {
  if (syncList.value[0].isAdd) {
    syncList.value[0] = { ...JSON.parse(JSON.stringify(demoData)), id: utils.uuid('api') };
    currEditData.value = syncList.value[0];
    return;
  }
  if (getChenkUpdateRes()) {
    return;
  }
  // 列表开始位置添加一条新数据
  syncList.value.unshift({ ...JSON.parse(JSON.stringify(demoData)), id: utils.uuid('api') });
  // 记录展开状态
  lastIsExpandState.value = syncList.value[0].isExpand;
  // 记录正在编辑的数据(编辑逻辑需要)
  currEditData.value = syncList.value[0];
  // 启用添加示例
  addDemoBtnDisabled.value = false;
  setEditFalseExceptId(syncList.value, currEditData.value.id);
};

// 添加认证
const addAuth = (sync:SyncObj) => {
  const hasEmpty = getAuthCheckRes(sync.auths);
  if (hasEmpty) {
    return;
  }

  if (sync.auths.length > 1) {
    // 判断最后添加的keyname是否已经存在
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
// 删除认证
const deleteAuth = (sync:SyncObj, authIndex:number) => {
  sync.auths.splice(authIndex, 1);
  if (!sync.auths.length) {
    sync.auth = false;
  }
};

// 删除同步配置
const handleDelSync = async (sync:SyncObj) => {
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
  // 如果列表没有数据 删除后添加一条添加的数据
  if (syncList.value.length === 0) {
    syncList.value.unshift({ ...JSON.parse(JSON.stringify(newData)), id: utils.uuid('api') });
  }
};

// 测试
const handleTest = async (sync:SyncObj) => {
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

// 同步配置
const handleSync = async (sync:SyncObj) => {
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

// 保存
const handleSave = async (sync:SyncObj) => {
  // 校验有没有空项
  const checkRes = getCheckDataResult(sync);
  if (checkRes) {
    return;
  }
  // 如果是添加数据 判断名称有没有重复
  if (sync.isAdd) {
    const len = syncList.value.filter(item => item.name === sync.name)?.length;
    if (len >= 2) {
      notification.warning(t('service.syncConfig.messages.nameExists'));
      sync.nameErr = true;
      return;
    }
  } else {
    // 如果是旧数据 判断数据有没有修改
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

// 检查提交的数据有没有空项 有空项返回true 否则返回false
const getCheckDataResult = (_data:SyncObj):boolean => {
  let hasEmpty = false;
  if (!_data.name) {
    _data.nameErr = true;
    hasEmpty = true;
  }
  if (!_data.apiDocsUrl) {
    _data.apiDocsUrlErr.emptyUrl = true;
    hasEmpty = true;
  }

  // 检查认证里有没有空数据
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

onMounted(() => {
  getSynchronizationList();
});

// 记录正在编辑的数据 同时只有一个编辑
const currEditData = ref<SyncObj>();

// 展开收起 开启关闭编辑
const handleExpand = (event, sync:SyncObj) => {
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

// 提起公共代码 校验数据未保存
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
// 开启关闭编辑 同时修改展开收起
const handleEdit = (event, sync:SyncObj) => {
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

// 取消编辑
const cancelEdit = (event, sync:SyncObj) => {
  event.stopPropagation();
  // 如果取消的是添加的数据
  if (sync.isAdd) {
    // 如果列表仅有一条数据 且是添加的 禁止取消，保持展开并且编辑状态
    if (syncList.value.length === 1) {
      return;
    }
    // 如果列表有多条数据 取消后删除添加的数据 并启用添加按钮
    syncList.value = syncList.value.filter(item => item.id !== sync.id);
    addBtnDisabled.value = false;
    currEditData.value = undefined;
    return;
  }

  //  如果取消的是历史数据 判断数据有没有修改，然后收起详情并取消编辑状态
  const hasUpdate = chenkUpdate(sync);
  //  如果有修改取消编辑先恢复数据
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

// 判断编辑的数据有无改变
const chenkUpdate = (newData:SyncObj) => {
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

// 收起当前数据之外的数据并取消编辑
const setEditFalseExceptId = (arr:SyncObj[], id:string):void => {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i].id !== id) {
      arr[i].isEdit = false;
      arr[i].isExpand = false;
    }
  }
};

const inOptions = ref([{ label: 'header', value: 'header' }, { label: 'query', value: 'query' }]);

const getShowEmptyingexample = (sync:SyncObj) => {
  return utils.deepCompare(sync, { ...demoData, id: sync.id });
};

// 清空示例数据
const emptyingExample = e => {
  e.stopPropagation();
  syncList.value[0] = { ...JSON.parse(JSON.stringify(newData)), id: syncList.value[0].id };
};

// 开启认证
const openAuth = (checked:CheckedType, sync:SyncObj) => {
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

// 同步名称校验
const syncNameChange = (value:string, sync:SyncObj):void => {
  sync.nameErr = !value;
};
// 同步url校验
const syncUrlChange = debounce(duration.search, (value:string, sync:SyncObj):void => {
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

// 认证名称校验
const keyNameChange = (value:string, auth:AuthObj):void => {
  auth.keyNameErr = !value;
};

// 认证值校验
const authValueChange = (value:string, auth:AuthObj):void => {
  auth.valueErr = !value;
};

// 判断整个认证里有没有空项 返回ture有空项 false没有空项
const getAuthCheckRes = (auths:AuthObj[]):boolean => {
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
watch(() => notify.value, () => {
  if (props.source === 'modal') {
    return;
  }
  getSynchronizationList();
});

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

const openEditModal = (sync = undefined) => {
  editVisible.value = true;
  editSyncData.value = sync;
};

const handleSaveOk = () => {
  editVisible.value = false;
  emit('saveSuccess');
  getSynchronizationList();
};

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
          {{ t('common.add') }}
        </Button>
        <Button
          size="small"
          class="flex items-center"
          :disabled="props.disabled || syncList.filter(f=>!f.isAdd).length > 9 || addDemoBtnDisabled"
          @click="addSyncInfoDemo">
          <Icon icon="icon-jia" class="mr-1" />
          {{ t('service.syncConfig.actions.demo') }}
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
            <Tooltip :title="t('common.edit')" placement="top">
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
                :placeholder="t('service.syncConfig.form.addressPlaceholder')"
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
              <Radio value="COVER">{{ t('actions.cover') }}</Radio>
              <Radio value="IGNORE">{{ t('actions.ignore') }}</Radio>
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
              <span class="mr-3.5">{{ t('common.auth') }}</span>
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
                    <div class="h-7 leading-7">{{ t('service.syncConfig.form.paramName') }}</div>
                    <div class="h-7 leading-7">{{ t('service.syncConfig.form.paramPosition') }}</div>
                    <div class="h-7 leading-7">{{ t('service.syncConfig.form.paramValue') }}</div>
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
                      :placeholder="t('service.syncConfig.form.positionPlaceholder')" />
                    <Input
                      v-model:value="auth.value"
                      :disabled="!sync.isEdit"
                      :error="auth.valueErr"
                      :maxlength="1024"
                      :placeholder="t('service.syncConfig.form.valuePlaceholder')"
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
                  :loading="sync.saveloading"
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
                  {{ t('common.cancel') }}
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
