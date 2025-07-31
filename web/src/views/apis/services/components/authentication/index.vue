<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Arrow, Hints, Icon, IconRequired, Input, Modal, notification, Select, SelectEnum, Spin } from '@xcan-angus/vue-ui';
import { Button, Radio, RadioGroup, Textarea, Tooltip } from 'ant-design-vue';
import { services } from 'src/api/tester';
import { regexpUtils, utils } from '@xcan-angus/infra';
import { API_EXTENSION_KEY } from '@/views/apis/utils';

import { ApiKeyExtensionFields, AuthConfigObj, FlowKey, ModelObj } from './PropsType';

const { newTokenKey, oAuth2Key, oAuth2Token, basicAuthKey } = API_EXTENSION_KEY;

interface Props {
  visible: boolean;
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: ''
});

const emit = defineEmits<{(e: 'update:visible', value:boolean): void}>();

const loading = ref(false);
const modelData:ModelObj = {
  username: '',
  password: '',
  usernameErr: false,
  passwordErr: false,
  type: 'basic',
  token: '',
  apiKeyList: [
    {
      name: '',
      value: '',
      in: 'header',
      nameErr: false,
      valueErr: false
    }
  ],
  name: '',
  value: '',
  in: 'header',
  [newTokenKey]: false,
  'x-xc-oauth2-clientAuthType': 'BASIC_AUTH_HEADER',
  'x-xc-oauth2-callbackUrl': '',
  authorizationUrl: '',
  tokenUrl: '',
  refreshUrl: '',
  'x-xc-oauth2-clientId': '',
  'x-xc-oauth2-clientSecret': '',
  [oAuth2Key]: 'authorizationCode',
  [oAuth2Token]: '',
  scopes: [],
  tokenErr: false,
  oauth2TokenErr: false,
  refreshUrlErr: {
    isEmpty: false,
    isError: false
  },
  callbackUrlErr: {
    isEmpty: false,
    isError: false
  },
  tokenUrlErr: {
    isEmpty: false,
    isError: false
  },
  authorizationUrlErr: {
    isEmpty: false,
    isError: false
  },
  scopesErr: false
};
// 空数据
const newData:AuthConfigObj = {
  id: utils.uuid('api'),
  projectId: '',
  type: {
    value: 'basic',
    message: ''
  },
  key: '',
  ref: '',
  model: modelData,
  description: '',
  lastModifiedBy: '',
  lastModifiedByName: '',
  lastModifiedDate: '',
  isEdit: true,
  isAdd: true,
  isExpand: true,
  delLoading: false,
  saveloading: false,
  keyErr: false,
  hasModel: false
};

const authConfigList = ref<AuthConfigObj[]>([]);
const oldAuthConfigList = ref<AuthConfigObj[]>([]);
// 初始化服务器URL配置
const loadAuthConfig = async () => {
  loading.value = true;
  const [error, { data }] = await services.getCompData(props.id, ['securitySchemes']);
  loading.value = false;
  if (error) {
    return;
  }
  // // 如果没有历史数据 默认展示一条空数据
  if (!data?.length) {
    authConfigList.value = [JSON.parse(JSON.stringify(newData))];
    // 记录正在编辑的数据 编辑逻辑需要
    currEditData.value = authConfigList.value[0];
    addBtnDisabled.value = true;
    return;
  }

  authConfigList.value = data.map(item => ({
    ...item,
    description: item.description || '',
    isEdit: false,
    isAdd: false,
    isExpand: false,
    delLoading: false,
    saveloading: false,
    keyErr: false,
    hasModel: false,
    model: modelData
  }));
  // 记录历史数据
  oldAuthConfigList.value = JSON.parse(JSON.stringify(authConfigList.value));
  // 启用添加
  addBtnDisabled.value = false;
};

// 添加按钮禁用状态
const addBtnDisabled = ref(false);

// 添加新配置
const addAuthConfig = () => {
  const hasEditData = authConfigList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    const checkRes = getCheckDataResult(hasEditData[0]);
    if (checkRes) {
      return;
    }
    if (getChenkUpdateRes()) {
      return;
    }
  }

  if (authConfigList.value[0]?.isAdd) {
    authConfigList.value[0] = { ...JSON.parse(JSON.stringify(newData)), id: utils.uuid('api') };
    return;
  }
  // 列表开始位置添加一条新数据
  authConfigList.value.unshift({ ...JSON.parse(JSON.stringify(newData)), id: utils.uuid('api') });
  // 记录正在编辑的数据(编辑逻辑需要)
  currEditData.value = authConfigList.value[0];
  // 追加后禁用添加按钮
  addBtnDisabled.value = true;
  setEditFalseExceptId(authConfigList.value, currEditData.value.id);
};

// 删除配置
const hanldeDelete = async (auth:AuthConfigObj) => {
  if (auth.delLoading) {
    return;
  }
  // 如果是添加数据 直接删除
  if (auth.isAdd) {
    // 判断列表是否剩余一条数据 剩余一条数据禁止删除
    if (authConfigList.value.length === 1) {
      return;
    }
    authConfigList.value = authConfigList.value.filter(item => item.id !== auth.id);
    return;
  }

  loading.value = true;
  const [error] = await services.delComponent(props.id, [auth.ref]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('删除成功');
  // 如果删除成功
  authConfigList.value = authConfigList.value.filter(item => item.id !== auth.id);
  oldAuthConfigList.value = oldAuthConfigList.value.filter(item => item.id !== auth.id);

  // 如果列表没有数据 删除后添加一条添加的数据
  if (authConfigList.value.length === 0) {
    authConfigList.value.unshift({ ...JSON.parse(JSON.stringify(newData)), id: utils.uuid('api') });
  }
};

// 保存
const handleSave = async (auth:AuthConfigObj) => {
  // 校验有没有空项
  const checkRes = getCheckDataResult(auth);
  if (checkRes) {
    return;
  }

  // 如果是旧数据 判断数据有没有修改
  if (!auth.isAdd) {
    if (!chenkUpdate(auth)) {
      auth.isEdit = false;
      auth.isExpand = lastIsExpandState.value;
      return;
    }
  }

  let params:any = getQuery(auth);
  if (!params) {
    return;
  }

  if (auth.description) {
    params = { ...params, description: auth.description };
  }
  loading.value = true;
  const [error] = await services.addComponent(props.id, 'securitySchemes', auth.key, params);
  loading.value = false;
  if (error) {
    return false;
  }

  oldAuthConfigList.value = JSON.parse(JSON.stringify(authConfigList.value));
  auth.isEdit = false;
  auth.isExpand = lastIsExpandState.value;
  addBtnDisabled.value = false;
  currEditData.value = undefined;
  auth.hasModel = false;
  notification.success('保存成功');
  if (auth.isAdd) {
    auth.isAdd = false;
  }
};

// 检查提交的数据有没有空项 有空项返回true 否则返回false
const getCheckDataResult = (_data:AuthConfigObj):boolean => {
  let hasEmpty = false;
  if (!_data.key) {
    _data.keyErr = true;
    hasEmpty = true;
  }

  // 检查变量里有没有空数据
  switch (_data.model.type) {
    case 'basic': {
      if (!_data.model.username) {
        _data.model.usernameErr = true;
        hasEmpty = true;
      }
      if (!_data.model.password) {
        _data.model.passwordErr = true;
        hasEmpty = true;
      }
      break;
    }
    case 'bearer': {
      if (!_data.model.token) {
        _data.model.tokenErr = true;
        hasEmpty = true;
      }
      break;
    }
    case 'apiKey': {
      hasEmpty = getVariablesHasEmpty(_data);
      break;
    }
    case 'oauth2': {
      if (_data.model[newTokenKey]) {
        switch (_data.model[oAuth2Key]) {
          case 'authorizationCode':
            if (!_data.model.authorizationUrl) {
              _data.model.authorizationUrlErr.isEmpty = true;
              hasEmpty = true;
            }
            if (!_data.model.tokenUrl) {
              _data.model.tokenUrlErr.isEmpty = true;
              hasEmpty = true;
            }
            break;
          case 'password':
            if (!_data.model.username) {
              _data.model.usernameErr = true;
              hasEmpty = true;
            }
            if (!_data.model.password) {
              _data.model.passwordErr = true;
              hasEmpty = true;
            }
            if (!_data.model.tokenUrl) {
              _data.model.tokenUrlErr.isEmpty = true;
              hasEmpty = true;
            }
            break;
          case 'implicit':
            if (!_data.model.authorizationUrl) {
              _data.model.authorizationUrlErr.isEmpty = true;
              hasEmpty = true;
            }
            break;
          case 'clientCredentials':
            if (!_data.model.tokenUrl) {
              _data.model.tokenUrlErr.isEmpty = true;
              hasEmpty = true;
            }
            break;
        }

        if (!_data.model.scopes.length) {
          _data.model.scopesErr = true;
          hasEmpty = true;
        }
      }
    }
  }
  return hasEmpty;
};

const getQuery = (auth:AuthConfigObj) => {
  let b = false;
  if (!auth.key) {
    auth.keyErr = true;
    b = true;
  }

  switch (auth.model.type) {
    case 'basic':
      if (!auth.model.username) {
        auth.model.usernameErr = true;
        b = true;
      }
      if (!auth.model.password) {
        auth.model.passwordErr = true;
        b = true;
      }

      if (b) {
        return false;
      }
      return {
        type: 'http',
        scheme: 'basic',
        [basicAuthKey]: { username: auth.model.username, password: auth.model.password }
        // 'x-xc-value': 'basic ' + encode(auth.model.username, auth.model.password)
      };
    case 'bearer':
      if (!auth.model.token) {
        auth.model.tokenErr = true;
        b = true;
      }
      if (b) {
        return false;
      }
      return {
        type: 'http',
        scheme: 'bearer',
        'x-xc-value': 'Bearer ' + auth.model.token
      };
    case 'apiKey': {
      const hasEmpty = getVariablesHasEmpty(auth);
      if (hasEmpty) {
        return false;
      }
      const params = {
        type: 'apiKey',
        in: auth.model.apiKeyList[0].in,
        name: auth.model.apiKeyList[0].name,
        'x-xc-value': auth.model.apiKeyList[0].value
      };
      if (auth.model.apiKeyList.length > 1) {
        params['x-xc-apiKey'] = auth.model.apiKeyList.slice(1).map(item => ({ name: item.name, in: item.in, 'x-xc-value': item.value }));
      }
      return params;
    }
    case 'oauth2' : {
      if (auth.model[newTokenKey]) {
        let flowsParams:{
          scopes: Record<string, any>;
          authorizationUrl?: string;
          username?:string;
          password?:string;
          tokenUrl?: string;
          refreshUrl?: string;
          'x-xc-oauth2-clientAuthType'?: string;
          'x-xc-oauth2-callbackUrl'?: string;
          'x-xc-oauth2-clientId'?: string;
          'x-xc-oauth2-clientSecret'?: string;
        } = {
          scopes: auth.model.scopes.reduce((acc, cur) => {
            acc[cur] = '';
            return acc;
          }, {})
        };
        switch (auth.model[oAuth2Key]) {
          case 'authorizationCode':
            flowsParams = {
              ...flowsParams,
              authorizationUrl: auth.model.authorizationUrl,
              tokenUrl: auth.model.tokenUrl
            };
            if (auth.model['x-xc-oauth2-callbackUrl']) {
              flowsParams['x-xc-oauth2-callbackUrl'] = auth.model['x-xc-oauth2-callbackUrl'];
            }
            break;
          case 'password':
            flowsParams = {
              ...flowsParams,
              tokenUrl: auth.model.tokenUrl,
              username: auth.model.username,
              password: auth.model.password
            };
            break;
          case 'implicit':
            flowsParams = {
              ...flowsParams,
              authorizationUrl: auth.model.authorizationUrl
            };
            if (auth.model['x-xc-oauth2-callbackUrl']) {
              flowsParams['x-xc-oauth2-callbackUrl'] = auth.model['x-xc-oauth2-callbackUrl'];
            }
            break;
          case 'clientCredentials':
            flowsParams = {
              ...flowsParams,
              tokenUrl: auth.model.tokenUrl
            };
            break;
        }
        if (auth.model.refreshUrl) {
          flowsParams.refreshUrl = auth.model.refreshUrl;
        }
        if (auth.model['x-xc-oauth2-clientId']) {
          flowsParams['x-xc-oauth2-clientId'] = auth.model['x-xc-oauth2-clientId'];
        }
        if (auth.model['x-xc-oauth2-clientSecret']) {
          flowsParams['x-xc-oauth2-clientSecret'] = auth.model['x-xc-oauth2-clientSecret'];
        }
        if (auth.model['x-xc-oauth2-clientAuthType']) {
          flowsParams['x-xc-oauth2-clientAuthType'] = auth.model['x-xc-oauth2-clientAuthType'];
        }
        const params = {
          type: 'oauth2',
          flows: {
            [auth.model[oAuth2Key]]: flowsParams
          },
          [oAuth2Key]: auth.model[oAuth2Key],
          [newTokenKey]: auth.model[newTokenKey]
        };
        return params;
      }
      return {
        type: 'oauth2',
        [oAuth2Token]: auth.model[oAuth2Token],
        [newTokenKey]: auth.model[newTokenKey]
      };
    }
  }
};

const authKeyChange = (value:string, auth:AuthConfigObj) => {
  auth.keyErr = !value;
};
const usernameChange = (value:string, auth:AuthConfigObj) => {
  auth.model.usernameErr = !value;
};
const passwordChange = (value:string, auth:AuthConfigObj) => {
  auth.model.passwordErr = !value;
};
const tokenChange = (value:string, auth:AuthConfigObj) => {
  auth.model.tokenErr = !value;
};
const apiKeyNameChange = (value:string, apiKey:ApiKeyExtensionFields) => {
  apiKey.nameErr = !value;
};
const apiKeyValueChange = (value:string, apiKey:ApiKeyExtensionFields) => {
  apiKey.valueErr = !value;
};
const tokenUrlChange = (value:string, auth:AuthConfigObj, key:'tokenUrlErr' | 'authorizationUrlErr') => {
  if (!value) {
    auth.model[key].isEmpty = true;
    return;
  }

  auth.model[key].isEmpty = false;
  if (regexpUtils.isUrl(value)) {
    auth.model[key].isError = false;
    return;
  }
  auth.model[key].isError = true;
};
const callbackUrlChange = (value:string, auth:AuthConfigObj, key:'callbackUrlErr' | 'refreshUrlErr') => {
  if (!value) {
    auth.model[key].isError = false;
    return;
  }

  if (regexpUtils.isUrl(value)) {
    auth.model[key].isError = false;
    return;
  }
  auth.model[key].isError = true;
};

const oauth2TokenChange = (value:string, auth:AuthConfigObj) => {
  auth.model.tokenErr = !value;
};

const scopesChange = (value:string[], auth:AuthConfigObj) => {
  auth.model.scopesErr = !value.length;
  if (value.length > 199) {
    value.pop();
  }
  auth.model.scopes = value;
};

onMounted(() => {
  loadAuthConfig();
});

const getAuthConfigInfo = async (auth:AuthConfigObj) => {
  const [error, { data }] = await services.getRefInfo(props.id, auth.ref);
  if (error) {
    return;
  }
  auth.hasModel = true;
  const model = JSON.parse(data.model);
  const _type = model.type === 'http' ? model.scheme : model.type;
  auth.model.type = _type;
  if (model.description) {
    auth.description = model.description;
  }
  switch (_type) {
    case 'basic': {
      // const base64Str = model['x-xc-value'].replace('basic ', '');
      // auth.model.username = decode(base64Str).name;
      // auth.model.password = decode(base64Str).value;
      const basic = model[basicAuthKey] || {};
      auth.model.username = basic.username;
      auth.model.password = basic.password;
      break;
    }
    case 'bearer': {
      auth.model.token = model['x-xc-value'].replace('Bearer ', '');
      break;
    }
    case 'apiKey': {
      auth.model.apiKeyList = [{ name: model.name, value: model['x-xc-value'], in: model.in, nameErr: false, valueErr: false }];
      if (model['x-xc-apiKey']?.length) {
        auth.model.apiKeyList = [...auth.model.apiKeyList, ...model['x-xc-apiKey'].map(item => ({
          name: item.name, value: item['x-xc-value'], in: item.in, nameErr: false, valueErr: false
        }))];
      }
      break;
    }
    case 'oauth2': {
      auth.model[newTokenKey] = model[newTokenKey];
      if (model[newTokenKey]) {
        const key = Object.keys(model.flows)[0] as FlowKey;
        auth.model[oAuth2Key] = key;
        if (model.flows[key]?.authorizationUrl) {
          auth.model.authorizationUrl = model.flows[key].authorizationUrl;
        }
        if (model.flows[key]?.refreshUrl) {
          auth.model.refreshUrl = model.flows[key].refreshUrl;
        }
        if (model.flows[key]?.tokenUrl) {
          auth.model.tokenUrl = model.flows[key].tokenUrl;
        }
        if (model.flows[key]?.['x-xc-oauth2-callbackUrl']) {
          auth.model['x-xc-oauth2-callbackUrl'] = model.flows[key]['x-xc-oauth2-callbackUrl'];
        }
        if (model.flows[key]?.['x-xc-oauth2-clientId']) {
          auth.model['x-xc-oauth2-clientId'] = model.flows[key]['x-xc-oauth2-clientId'];
        }
        if (model.flows[key]?.['x-xc-oauth2-clientAuthType']) {
          auth.model['x-xc-oauth2-clientAuthType'] = model.flows[key]['x-xc-oauth2-clientAuthType'];
        }
        if (model.flows[key]?.['x-xc-oauth2-clientSecret']) {
          auth.model['x-xc-oauth2-clientSecret'] = model.flows[key]['x-xc-oauth2-clientSecret'];
        }
        if (model.flows[key]?.username) {
          auth.model.username = model.flows[key].username;
        }
        if (model.flows[key]?.password) {
          auth.model.password = model.flows[key].password;
        }
        if (model.flows[key]?.scopes && Object.keys(model.flows[key].scopes)?.length) {
          auth.model.scopes = Object.keys(model.flows[key].scopes);
        }
        break;
      }
      auth.model[oAuth2Token] = model[oAuth2Token];
      break;
    }
  }

  for (let i = 0; i < oldAuthConfigList.value.length; i++) {
    if (oldAuthConfigList[i]?.id === auth?.id) {
      oldAuthConfigList[i] = auth;
      break;
    }
  }
};

// 记录正在编辑的数据 同时只有一个编辑
const currEditData = ref<AuthConfigObj>();

// 展开收起 开启关闭编辑
const handleExpand = async (event, auth:AuthConfigObj) => {
  event.stopPropagation();
  if (!auth.hasModel) {
    await getAuthConfigInfo(auth);
    oldAuthConfigList.value = JSON.parse(JSON.stringify(authConfigList.value));
  }

  const hasEditData = authConfigList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    if (hasEditData[0].isAdd) {
      authConfigList.value = authConfigList.value.filter(item => item.id !== hasEditData[0].id);
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
  auth.isExpand = !auth.isExpand;
  if (!auth.isExpand) {
    auth.isEdit = false;
  }
  setEditFalseExceptId(authConfigList.value, auth.id);
};

// 提起公共代码 校验数据未保存
const getChenkUpdateRes = () => {
  if (currEditData.value) {
    const hasUpdate = chenkUpdate(authConfigList.value.filter(item => item.id === currEditData.value?.id)[0]);
    if (hasUpdate) {
      notification.warning('数据未保存，请先保存数据或者取消编辑');
      return true;
    }
  }
  return false;
};

// 判断编辑的数据有无改变
const chenkUpdate = (newData:AuthConfigObj) => {
  const _oldDataList = oldAuthConfigList.value.filter(item => item?.id === newData?.id);
  if (!_oldDataList?.length) {
    return true;
  }

  // eslint-disable-next-line
  const { isEdit: _oldEdit, isExpand: _oldExpand, ..._oldData } = _oldDataList[0];
  // eslint-disable-next-line
  const { isEdit, isExpand, ..._newData } = newData;

  return !utils.deepCompare(_oldData, _newData);
};

const lastIsExpandState = ref(false);
// 开启关闭编辑 同时修改展开收起
const handleEdit = async (event, auth:AuthConfigObj) => {
  event.stopPropagation();
  if (!auth.hasModel) {
    await getAuthConfigInfo(auth);
    oldAuthConfigList.value = JSON.parse(JSON.stringify(authConfigList.value));
  }

  const hasEditData = authConfigList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    if (hasEditData[0].isAdd) {
      authConfigList.value = authConfigList.value.filter(item => item.id !== hasEditData[0].id);
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

  lastIsExpandState.value = auth.isExpand;
  auth.isEdit = true;
  auth.isExpand = true;
  currEditData.value = auth;
  setEditFalseExceptId(authConfigList.value, auth.id);
};

// 取消编辑
const handleCancel = (auth:AuthConfigObj) => {
  // 如果取消的是添加的数据
  if (auth.isAdd) {
    // 如果列表仅有一条数据 且是添加的 禁止取消，保持展开并且编辑状态
    if (authConfigList.value.length === 1) {
      emit('update:visible', false);
      return;
    }
    // 如果列表有多条数据 取消后删除添加的数据 并启用添加按钮
    authConfigList.value = authConfigList.value.filter(item => item.id !== auth.id);
    addBtnDisabled.value = false;
    currEditData.value = undefined;
    return;
  }

  //  如果取消的是历史数据 判断数据有没有修改，然后收起详情并取消编辑状态
  const hasUpdate = chenkUpdate(auth);
  //  如果有修改,取消编辑先恢复数据
  if (hasUpdate) {
    const oldSync = oldAuthConfigList.value.find(item => item.id === auth.id);
    for (let i = 0; i < authConfigList.value.length; i++) {
      if (auth.id === authConfigList.value[i].id) {
        authConfigList.value[i] = oldSync ? JSON.parse(JSON.stringify(oldSync)) : auth;
        break;
      }
    }
  }
  auth.isEdit = false;
  auth.isExpand = lastIsExpandState.value;
  currEditData.value = undefined;
  addBtnDisabled.value = false;
};

// 收起当前数据之外的数据并取消编辑
const setEditFalseExceptId = (arr:AuthConfigObj[], id:string):void => {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i].id !== id) {
      arr[i].isEdit = false;
      arr[i].isExpand = false;
    }
  }
};

const serverUrlListRef = ref<HTMLElement | null>();
// 添加ApiKey
const addApiKey = (auth:AuthConfigObj) => {
  const hasEmpty = getVariablesHasEmpty(auth);
  if (hasEmpty) {
    return;
  }
  auth.model.apiKeyList.push({
    name: '',
    value: '',
    in: 'header',
    nameErr: false,
    valueErr: false
  });
};

// 获取整组ApiKey里有没有空值 有空值触发校验 返回true 否则返回false
const getVariablesHasEmpty = (auth:AuthConfigObj):boolean => {
  let hasEmpty = false;
  if (!auth.model.apiKeyList?.length) {
    return hasEmpty;
  }

  for (let i = 0; i < auth.model.apiKeyList.length; i++) {
    const _item = auth.model.apiKeyList[i];
    if (!_item.name) {
      _item.nameErr = true;
      hasEmpty = true;
    }
    if (!_item.value) {
      _item.valueErr = true;
      hasEmpty = true;
    }
  }
  return hasEmpty;
};

// 删除变量
const delApikey = (auth:AuthConfigObj, index:number) => {
  auth.model.apiKeyList?.splice(index, 1);
};

const modelTypeChange = (auth:AuthConfigObj) => {
  auth.model.tokenErr = false;
  auth.model.oauth2TokenErr = false;
  auth.model.refreshUrlErr = {
    isEmpty: false,
    isError: false
  };
  auth.model.callbackUrlErr = {
    isEmpty: false,
    isError: false
  };
  auth.model.tokenUrlErr = {
    isEmpty: false,
    isError: false
  };
  auth.model.authorizationUrlErr = {
    isEmpty: false,
    isError: false
  };
  auth.model.scopesErr = false;
  auth.model.usernameErr = false;
  auth.model.passwordErr = false;
};

const cancelModal = () => {
  emit('update:visible', false);
};

const authTypeOptions = [
  {
    label: 'BasicAuth',
    value: 'basic'
  },
  {
    label: 'BearerToken',
    value: 'bearer'
  },
  {
    label: 'ApiKey',
    value: 'apiKey'
  },
  {
    label: 'OAuth2.0',
    value: 'oauth2'
  }
];

const apiKeyInOptions = [
  {
    label: 'header',
    value: 'header'
  },
  {
    label: 'query',
    value: 'query'
  }
];

const OAuth2AuthorizationTypeOptions = [
  {
    value: 'authorizationCode',
    label: '授权码模式（AuthorizationCode）'
  },
  {
    value: 'password',
    label: '密码模式（PasswordCredentials）'
  },
  {
    value: 'implicit',
    label: '隐式模式（Implicit）'
  },
  {
    value: 'clientCredentials',
    label: '客户端模式（ClientCredentials）'
  }
];
</script>
<template>
  <Modal
    title="安全方案配置"
    :visible="visible"
    :reverse="true"
    :footer="null"
    @cancel="cancelModal">
    <Hints text="定义当前服务下所有接口可以使用的安全方案(Authorization)。" class="mt-2" />
    <Spin :spinning="loading">
      <div class="flex justify-end my-2">
        <Button
          size="small"
          type="primary"
          class="flex items-center"
          :disabled="authConfigList?.length > 49 || addBtnDisabled"
          @click="addAuthConfig">
          <Icon icon="icon-jia" class="mr-1" />
          添加
        </Button>
      </div>
      <div
        ref="serverUrlListRef"
        style="min-height: 400px;max-height: 72vh;scrollbar-gutter: stable;"
        class="overflow-y-auto flex flex-col space-y-2 pr-2.5 -mr-4.5 mb-5 text-3 text-text-content">
        <div
          v-for="auth in authConfigList"
          :key="auth.id"
          class="border border-border-divider p-2 rounded">
          <div v-if="!auth.isAdd && !auth.isEdit">
            <div class="flex items-start justify-between -mt-1">
              <div class="mr-2 mt-1 break-all whitespace-pre-wrap cursor-pointer" @click="(e) => handleExpand(e, auth)">{{ auth.key }}</div>
              <Arrow
                :open="auth.isExpand"
                class="mt-1.5"
                @click="(e) => handleExpand(e, auth)" />
            </div>
          </div>
          <div v-if="!auth.isAdd && !auth.isEdit" class="text-3 flex justify-end ml-2 mt-2">
            <Tooltip title="编辑" placement="top">
              <Icon
                icon="icon-shuxie"
                class="mr-1"
                :class="auth.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                @click="(e) => handleEdit(e, auth)" />
            </Tooltip>
            <Tooltip title="删除" placement="top">
              <Icon
                icon="icon-qingchu"
                class="text-3.5"
                :class="auth.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                @click="hanldeDelete(auth)" />
            </Tooltip>
          </div>
          <div
            class="text-3 text-text-content transition-height duration-500 overflow-hidden"
            :class="auth.isExpand ? 'open-info' : 'stop-info'">
            <template v-if="auth.isExpand">
              <div class="flex items-center">
                <IconRequired />
                <span>名称</span>
                <Tooltip title="  名称只能包含 A-Z a-z 0-9 - . _ 字符" placement="topLeft">
                  <Icon icon="icon-tishi1" class="ml-1 text-tips cursor-pointer text-3.5" />
                </Tooltip>
              </div>
              <Input
                v-model:value="auth.key"
                type="input"
                size="small"
                :maxlength="400"
                :error="auth.keyErr"
                :allowClear="false"
                :disabled="!auth.isEdit || !auth.isAdd"
                placeholder="请输入方案名称"
                dataType="mixin-en"
                includes="_.-"
                @change="(event)=>authKeyChange(event.target.value,auth)" />
              <div class="mt-4"><IconRequired />认证类型</div>
              <RadioGroup
                v-model:value="auth.model.type"
                class="flex flex-wrap"
                :options="authTypeOptions"
                :disabled="!auth.isEdit"
                @change="modelTypeChange(auth)">
              </RadioGroup>
              <template v-if="auth.model.type === 'basic'">
                <div class="mt-2">
                  <IconRequired />用户名
                </div>
                <Input
                  v-model:value="auth.model.username"
                  type="input"
                  class="rounded"
                  size="small"
                  :maxlength="400"
                  :error="auth.model.usernameErr"
                  :allowClear="false"
                  :disabled="!auth.isEdit"
                  placeholder="请输入用户名"
                  @change="(event)=>usernameChange(event.target.value,auth)" />
                <div class="mt-4">
                  <IconRequired />密码
                </div>
                <Input
                  v-model:value="auth.model.password"
                  :error="auth.model.passwordErr"
                  :maxlength="400"
                  :disabled="!auth.isEdit"
                  type="password"
                  size="small"
                  placeholder="请输入密码"
                  @change="(event)=>passwordChange(event.target.value,auth)" />
              </template>
              <template v-if="auth.model.type === 'bearer'">
                <div class="mt-2">
                  <IconRequired />令牌
                </div>
                <Input
                  v-model:value="auth.model.token"
                  size="small"
                  prefix="Bearer"
                  placeholder="请输入访问令牌"
                  :error="auth.model.tokenErr"
                  :disabled="!auth.isEdit"
                  @change="(event)=>tokenChange(event.target.value,auth)" />
              </template>
              <template v-if="auth.model.type === 'apiKey'">
                <div v-if="auth.model.apiKeyList?.length" class="-mt-2">
                  <div
                    v-for="item,apikeyIndex in auth.model.apiKeyList"
                    :key="apikeyIndex"
                    class="flex flex-col border-b border-dashed pb-4"
                    :class="{'border-b-0 pb-0': apikeyIndex === auth.model.apiKeyList.length-1}">
                    <div class="mt-4 flex justify-between">
                      <span><IconRequired />名称</span>
                      <span v-if="auth.isEdit" class="text-4 -mt-2">
                        <template v-if="auth.model.apiKeyList.length < 49">
                          <Icon
                            icon="icon-tianjia"
                            class="cursor-pointer hover:text-text-link-hover"
                            @click="addApiKey(auth)" />
                        </template>
                        <template v-if="auth.model.apiKeyList.length !== 1">
                          <Icon
                            icon="icon-jianshao"
                            class="ml-2"
                            @click="delApikey(auth,apikeyIndex)" />
                        </template>
                      </span>
                    </div>
                    <Input
                      v-model:value="item.name"
                      size="small"
                      :error="item.nameErr"
                      :allowClear="false"
                      :disabled="!auth.isEdit"
                      placeholder="输入名称"
                      @change="(event)=>apiKeyNameChange(event.target.value,item)" />
                    <div class="mt-4">
                      <IconRequired />值
                    </div>
                    <Input
                      v-model:value="item.value"
                      size="small"
                      :error="item.valueErr"
                      :disabled="!auth.isEdit"
                      :allowClear="false"
                      placeholder="输入值"
                      @change="(event)=>apiKeyValueChange(event.target.value,item)" />
                    <div class="mt-4">
                      <IconRequired />位置
                    </div>
                    <Select
                      v-model:value="item.in"
                      class="flex-1"
                      size="small"
                      :disabled="!auth.isEdit"
                      :allowClear="false"
                      :options="apiKeyInOptions"
                      placeholder="选择位置">
                    </Select>
                  </div>
                </div>
              </template>
              <template v-if="auth.model.type === 'oauth2'">
                <div class="mt-2 pl-1.5">
                  配置令牌
                </div>
                <RadioGroup v-model:value="auth.model[newTokenKey]" :disabled="!auth.isEdit">
                  <Radio :value="false">已有令牌</Radio>
                  <Radio :value="true">生成令牌</Radio>
                </RadioGroup>
                <template v-if="!auth.model[newTokenKey]">
                  <div class="mt-2 pl-1.5">
                    令牌
                  </div>
                  <Input
                    v-model:value="auth.model[oAuth2Token]"
                    size="small"
                    placeholder="请输入授权KEY"
                    :maxlength="400"
                    :disabled="!auth.isEdit"
                    :error="auth.model.tokenErr"
                    @change="(event)=>oauth2TokenChange(event.target.value,auth)" />
                </template>
                <template v-else>
                  <div class="mt-2 pl-1.5">
                    授权类型
                  </div>
                  <Select
                    v-model:value="auth.model[oAuth2Key]"
                    size="small"
                    class="w-full"
                    :disabled="!auth.isEdit"
                    :options="OAuth2AuthorizationTypeOptions" />
                  <template v-if="['authorizationCode','implicit'].includes(auth.model[oAuth2Key])">
                    <div class="mt-4">
                      <IconRequired />授权URL
                    </div>
                    <Input
                      v-model:value="auth.model.authorizationUrl"
                      size="small"
                      placeholder="请输入授权URL"
                      :disabled="!auth.isEdit"
                      :maxlength="400"
                      :error="auth.model.authorizationUrlErr.isEmpty || auth.model.authorizationUrlErr.isError"
                      @change="(event)=>tokenUrlChange(event.target.value,auth,'authorizationUrlErr')" />
                    <div class="h-4 text-rule">
                      <template v-if="auth.model.authorizationUrlErr.isError">
                        请输入正确的url地址
                      </template>
                    </div>
                    <div class="pl-1.5">
                      回调URL
                    </div>
                    <Input
                      v-model:value="auth.model['x-xc-oauth2-callbackUrl']"
                      size="small"
                      placeholder="请输入回调URL"
                      :disabled="!auth.isEdit"
                      :error="auth.model.callbackUrlErr.isError"
                      @change="(event)=>callbackUrlChange(event.target.value,auth,'callbackUrlErr')" />
                    <div class="h-4 text-rule">
                      <template v-if="auth.model.callbackUrlErr.isError">
                        请输入正确的url地址
                      </template>
                    </div>
                  </template>
                  <template v-if="!['implicit'].includes(auth.model[oAuth2Key])">
                    <div :class="{'mt-4':!['authorizationCode','implicit'].includes(auth.model[oAuth2Key])}">
                      <IconRequired />访问令牌URL
                    </div>
                    <Input
                      v-model:value="auth.model.tokenUrl"
                      size="small"
                      placeholder="请输入访问令牌URL"
                      :disabled="!auth.isEdit"
                      :maxlength="400"
                      :error="auth.model.tokenUrlErr.isEmpty || auth.model.tokenUrlErr.isError"
                      @change="(event)=>tokenUrlChange(event.target.value,auth,'tokenUrlErr')" />
                    <div class="h-4 text-rule">
                      <template v-if="auth.model.tokenUrlErr.isError">
                        请输入正确的url地址
                      </template>
                    </div>
                  </template>
                  <div class="pl-1.5">
                    刷新令牌URL
                  </div>
                  <Input
                    v-model:value="auth.model.refreshUrl"
                    size="small"
                    placeholder="请输入刷新令牌URL"
                    :maxlength="400"
                    :disabled="!auth.isEdit"
                    :error="auth.model.refreshUrlErr.isError"
                    @change="(event)=>callbackUrlChange(event.target.value,auth,'refreshUrlErr')" />
                  <div class="h-4 text-rule">
                    <template v-if="auth.model.refreshUrlErr.isError">
                      请输入正确的url地址
                    </template>
                  </div>
                  <div class="pl-1.5">
                    客户ID
                  </div>
                  <Input
                    v-model:value="auth.model['x-xc-oauth2-clientId']"
                    size="small"
                    placeholder="请输入客户ID"
                    :maxlength="400"
                    :disabled="!auth.isEdit" />
                  <div class="mt-4 pl-1.75">
                    客户端秘钥
                  </div>
                  <Input
                    v-model:value="auth.model['x-xc-oauth2-clientSecret']"
                    size="small"
                    placeholder="请输入客户Secret"
                    :maxlength="400"
                    :disabled="!auth.isEdit" />
                  <template v-if="auth.model[oAuth2Key] === 'password'">
                    <div class="mt-4">
                      <IconRequired />用户名
                    </div>
                    <Input
                      v-model:value="auth.model.username"
                      type="input"
                      class="rounded"
                      size="small"
                      :maxlength="400"
                      :error="auth.model.usernameErr"
                      :allowClear="false"
                      :disabled="!auth.isEdit"
                      placeholder="请输入用户名"
                      @change="(event)=>usernameChange(event.target.value,auth)" />
                    <div class="mt-4">
                      <IconRequired />密码
                    </div>
                    <Input
                      v-model:value="auth.model.password"
                      :error="auth.model.passwordErr"
                      :maxlength="1024"
                      :disabled="!auth.isEdit"
                      type="password"
                      size="small"
                      placeholder="请输入密码"
                      @change="(event)=>passwordChange(event.target.value,auth)" />
                  </template>
                  <div class="mt-4">
                    <IconRequired />Scopes
                  </div>
                  <Select
                    :value="auth.model.scopes"
                    mode="tags"
                    class="w-full"
                    :disabled="!auth.isEdit"
                    :error="auth.model.scopesErr"
                    :open="false"
                    :tokenSeparators="[',']"
                    placeholder="输入scope，多个scope以英文逗号分隔"
                    @change="(value)=>scopesChange(value,auth)" />
                  <div class="mt-4 pl-1.5">
                    客户端认证
                  </div>
                  <SelectEnum
                    v-model:value="auth.model['x-xc-oauth2-clientAuthType']"
                    :disabled="!auth.isEdit"
                    class="w-full"
                    enumKey="AuthClientIn"
                    placeholder="选择客户端认证" />
                </template>
              </template>
              <template v-if="auth.isEdit">
                <div class="mt-4 pl-1.5">描述</div>
                <Textarea
                  v-model:value="auth.description"
                  size="small"
                  placeholder="输入描述，支持Markdown语法"
                  :rows="3"
                  :maxlength="400"
                  :disabled="!auth.isEdit" />
              </template>
              <template v-else>
                <template v-if="auth.description">
                  <div class="mt-2">描述</div>
                  <Textarea
                    :value="auth.description"
                    size="small"
                    disabled
                    :autoSize="{ minRows: 2, maxRows: 6 }" />
                </template>
              </template>
              <div v-if="auth.isEdit" class="flex justify-end mt-3.5">
                <Button
                  type="primary"
                  size="small"
                  @click="handleSave(auth)">
                  确定
                </Button>
                <Button
                  size="small"
                  class="ml-3.5"
                  @click="handleCancel(auth)">
                  取消
                </Button>
              </div>
            </template>
          </div>
        </div>
      </div>
    </Spin>
  </Modal>
</template>
<style scoped>
:deep(.ant-input-prefix) {
  @apply text-3;
}

.open-info {
  max-height: 2000px;
}

.stop-info {
  max-height: 0;
}

:deep(.ant-radio-group .ant-radio-wrapper > span.ant-radio+*) {
  padding: 4px;
}

:deep(.v-note-wrapper .v-note-read-model) {
  padding: 8px;
}
</style>
