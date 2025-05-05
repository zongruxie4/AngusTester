<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Arrow, Colon, Hints, Icon, IconRequired, Input, notification, Select, Spin, Modal } from '@xcan-angus/vue-ui';
import { Button, Radio, RadioGroup, Switch, Tooltip } from 'ant-design-vue';
import { services } from 'src/api/tester';
import { regexp, utils, duration } from '@xcan-angus/tools';
import { debounce } from 'throttle-debounce';

import { SyncObj, AuthObj } from './PropsType';
type CheckedType = typeof Switch.props.checked.type;

interface Props {
  syncData?: SyncObj;
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

const emits = defineEmits<{(e: 'update:visible', boolean): void; (e: 'ok'):void}>();

// 空数据
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
  authFlag: false,
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

const inOptions = ref([{ label: 'header', value: 'header' }, { label: 'query', value: 'query' }]);

const loading = ref(false);

// 同步名称校验
const syncNameChange = (value:string):void => {
  sync.value.nameErr = !value;
};

// 同步url校验
const syncUrlChange = debounce(duration.search, (value:string):void => {
  if (!value) {
    sync.value.apiDocsUrlErr.emptyUrl = true;
    return;
  }

  sync.value.apiDocsUrlErr.emptyUrl = false;
  if (regexp.isUrl(value)) {
    sync.value.apiDocsUrlErr.errUrl = false;
    return;
  }
  sync.value.apiDocsUrlErr.errUrl = true;
});

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
  if (!regexp.isUrl(_data.apiDocsUrl)) {
    _data.apiDocsUrlErr.errUrl = true;
    hasEmpty = true;
  }

  return hasEmpty;
};
// 判断编辑的数据有无改变
const chenkUpdate = (newData:SyncObj) => {
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

// 保存
const handleSave = async () => {
  // 校验有没有空项
  const checkRes = getCheckDataResult(sync.value);
  if (checkRes) {
    return;
  }
  if (props.allNames.includes(sync.value.name)) {
    notification.warning('名称已存在');
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
  notification.success('保存成功');
  emits('ok');
};

// 认证名称校验
const keyNameChange = (value:string, auth:AuthObj):void => {
  auth.keyNameErr = !value;
};
// 开启认证
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

// 认证值校验
const authValueChange = (value:string, auth:AuthObj):void => {
  auth.valueErr = !value;
};

// 删除认证
const deleteAuth = (authIndex:number) => {
  sync.value.auths.splice(authIndex, 1);
  if (!sync.value.auths.length) {
    sync.value.authFlag = false;
  }
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

// 添加认证
const addAuth = () => {
  const hasEmpty = getAuthCheckRes(sync.value.auths);
  if (hasEmpty) {
    return;
  }

  if (sync.value.auths.length > 1) {
    // 判断最后添加的keyname是否已经存在
    const len = sync.value.auths.filter(item => item.keyName === sync.value.auths[sync.value.auths.length - 1].keyName)?.length;
    if (len >= 2) {
      notification.warning('认证名称已存在');
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

const cancel = () => {
  emits('update:visible', false);
};

const sync = ref({ ...newData });

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
    :title="props.syncData ? '编辑' : '添加'"
    :okButtonProps="{
      loading
    }"
    :visible="props.visible"
    @cancel="cancel"
    @ok="handleSave">
    <div
      class="transition-height duration-500 overflow-hidden leading-3">
      <div class="flex flex-col">
        <span><IconRequired />名称</span>
        <Input
          v-model:value="sync.name"
          placeholder="名称"
          size="small"
          class="mt-2 mb-5"
          :maxlength="100"

          :error="sync?.nameErr"
          @change="(event)=>syncNameChange(event.target.value)" />
        <span><IconRequired />来源</span>
        <Select
          value="OpenAPI"
          size="small"
          disabled
          class="mt-2 mb-5"
          :allowClear="false" />
        <span><IconRequired />地址</span>
        <div class="relative mt-2 mb-5">
          <Input
            v-model:value="sync.apiDocsUrl"
            placeholder="地址"
            size="small"
            :maxlength="400"
            :error="sync?.apiDocsUrlErr.emptyUrl || sync?.apiDocsUrlErr.errUrl"
            @change="syncUrlChange($event.target.value)" />
          <div class="absolute text-rule text-3">
            <template v-if="sync?.apiDocsUrlErr.errUrl">请输入正确的url地址</template>
          </div>
        </div>
        <span>遇到重复时处理策略</span>
        <RadioGroup
          v-model:value="sync.strategyWhenDuplicated.value"
          class="mt-2 mb-5">
          <Radio value="COVER">覆盖</Radio>
          <Radio value="IGNORE">忽略</Radio>
        </RadioGroup>
        <span>同步接口不存在时是否删除本地接口</span>
        <RadioGroup
          v-model:value="sync.deleteWhenNotExisted"
          class="mt-2 mb-5">
          <Radio :value="true">是</Radio>
          <Radio :value="false">否</Radio>
        </RadioGroup>
        <div class="flex items-center">
          <span class="mr-3.5">认证</span>
          <Switch
            v-model:checked="sync.authFlag"
            size="small"
            class="w-8 mr-2"
            @change="(checked:CheckedType)=>openAuth(checked)" />
        </div>
        <Hints text="当同步地址受到保护时，它是必需的。最多允许添加10个。" class="mt-0.5" />
        <template v-if="sync?.authFlag">
          <div class="mt-2">
            <div
              v-for="auth,aindex in sync?.auths"
              :key="aindex"
              class="flex mb-5 items-center">
              <div class="flex flex-col space-y-3 flex-none mr-2">
                <div class="h-7 leading-7">参数名</div>
                <div class="h-7 leading-7">参数位置</div>
                <div class="h-7 leading-7">参数值</div>
              </div>
              <div class="flex flex-col flex-1 space-y-3">
                <Input
                  v-model:value="auth.keyName"
                  :error="auth.keyNameErr"
                  :maxlength="400"
                  placeholder="名称"
                  size="small"
                  @change="(event)=>keyNameChange(event.target.value,auth)" />
                <Select
                  v-model:value="auth.in.value"
                  :options="inOptions"
                  size="small"
                  placeholder="位置" />
                <Input
                  v-model:value="auth.value"
                  :error="auth.valueErr"
                  :maxlength="1024"
                  placeholder="值"
                  size="small"
                  @change="(event)=>authValueChange(event.target.value,auth)" />
              </div>
              <div class="flex-none flex flex-col ml-2 space-y-1">
                <Tooltip title="删除" placement="top">
                  <Icon
                    icon="icon-jianshao"
                    class="text-5 mr-1 cursor-pointer text-text-sub-content hover:text-text-link-hover"
                    @click="deleteAuth(aindex)" />
                </Tooltip>
                <template v-if="aindex == sync?.auths.length-1 && sync?.auths.length <= 9 ">
                  <template v-if="auth.value && auth.keyName">
                    <Tooltip title="添加" placement="top">
                      <Icon
                        icon="icon-tianjia"
                        class="text-5 cursor-pointer text-text-sub-content hover:text-text-link-hover"
                        @click="addAuth()" />
                    </Tooltip>
                  </template>
                  <template v-else>
                    <Tooltip title="请输入内容" placement="top">
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
