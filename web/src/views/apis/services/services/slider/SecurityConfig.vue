<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Arrow, Hints, Icon, IconRequired, Input, notification, Select, Spin, VuexHelper } from '@xcan-angus/vue-ui';
import { Button, Radio, RadioGroup, Tooltip } from 'ant-design-vue';
import { services } from '@/api/tester';
import { regexpUtils, utils } from '@xcan-angus/infra';
import { API_EXTENSION_KEY } from '@/utils/apis';
import { useI18n } from 'vue-i18n';
import { ApiKeyExtensionInfo, SecuritySchemeInfo, ServicesCompDetail } from '@/views/apis/services/services/types';
import { ServicesCompType } from '@/enums/enums';

import { AuthFlowKey } from '@/types/openapi-types';

import SelectEnum from '@/components/form/enum/SelectEnum.vue';

interface Props {
  id: string;
  disabled: boolean;
  source:'modal' | 'home' | 'right'
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  disabled: false,
  source: 'right'
});

const { t } = useI18n();

const {
  basicAuthKey,
  valueKey,
  securityApiKeyPrefix,
  newTokenKey,
  oAuth2Key,
  oAuth2Token,
  oAuth2CallbackUrlKey,
  oAuth2ClientIdKey,
  oAuth2ClientSecretKey,
  oAuth2UsernameKey,
  oAuth2PasswordKey,
  oAuth2ClientAuthTypeKey
} = API_EXTENSION_KEY;

const { useState } = VuexHelper;

const emit = defineEmits<{(e: 'deleteSuccess'): void, (e: 'saveSuccess'): void}>();

// Default model for security scheme form
const defaultModel:SecuritySchemeInfo = {
  // OpenAPI feilds
  type: 'http',
  name: '',
  in: 'header',
  authorizationUrl: '',
  tokenUrl: '',
  refreshUrl: '',
  scopes: [],

  // Temporary fields used only in web UI
  value: '',
  username: '',
  password: '',
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
  usernameErr: false,
  passwordErr: false,
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

// Default wrapper for a security component item
const defaultCompDetail:ServicesCompDetail = {
  id: utils.uuid('api'),
  serviceId: '',
  type: {
    value: ServicesCompType.schemas,
    message: ''
  },
  key: '',
  ref: '',
  model: defaultModel,
  description: '',
  lastModifiedBy: '',
  lastModifiedByName: '',
  lastModifiedDate: '',

  // Temporary fields used only in web UI
  isEdit: true,
  isAdd: true,
  isExpand: true,
  delLoading: false,
  saveLoading: false,
  keyErr: false,
  hasModel: false
};

const loading = ref(false);
const securityConfigList = ref<ServicesCompDetail[]>([]);
const oldSecurityConfigList = ref<ServicesCompDetail[]>([]);
// Initialize list of security schemes from service
const loadSecuritySchemes = async () => {
  loading.value = true;
  const [error, { data }] = await services.getCompData(props.id, ['securitySchemes'], undefined, { silence: false });
  loading.value = false;
  if (error) {
    return;
  }
  // If no historical data exists, create one empty item by default
  if (!data?.length) {
    securityConfigList.value = [JSON.parse(JSON.stringify(defaultCompDetail))];
    // Record the item being edited for subsequent edit operations
    currEditData.value = securityConfigList.value[0];
    addBtnDisabled.value = true;
    return;
  }

  securityConfigList.value = data.map(item => ({
    ...item,
    description: item.description || '',
    isEdit: false,
    isAdd: false,
    isExpand: false,
    delLoading: false,
    saveLoading: false,
    keyErr: false,
    hasModel: false,
    model: defaultModel
  }));
  // Take a snapshot of the current list for change detection
  oldSecurityConfigList.value = JSON.parse(JSON.stringify(securityConfigList.value));
  // Enable add button
  addBtnDisabled.value = false;
};

// State for disabling the add button when editing
const addBtnDisabled = ref(false);

// Add new configuration
/**
 * Add a new empty security scheme entry to the list and enter edit mode.
 * Ensures only one editing entry at a time and validates pending edits.
 */
const addNewSecurityConfig = () => {
  const hasEditData = securityConfigList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    const checkRes = hasValidationErrors(hasEditData[0]);
    if (checkRes) {
      return;
    }
    if (hasUnsavedChangesAndWarn()) {
      return;
    }
  }

  if (securityConfigList.value[0]?.isAdd) {
    securityConfigList.value[0] = { ...JSON.parse(JSON.stringify(defaultCompDetail)), id: utils.uuid('api') };
    return;
  }
  // Insert a new item at the beginning of the list
  securityConfigList.value.unshift({ ...JSON.parse(JSON.stringify(defaultCompDetail)), id: utils.uuid('api') });
  // Record the current editing item
  currEditData.value = securityConfigList.value[0];
  // Disable add button after appending
  addBtnDisabled.value = true;
  collapseAndDisableEditExcept(securityConfigList.value, currEditData.value.id);
};

/**
 * Delete a security scheme. If it is a new unsaved item, remove locally.
 * Otherwise, delete via API and update local lists accordingly.
 */
const handleDelete = async (auth:ServicesCompDetail) => {
  if (auth.delLoading) {
    return;
  }
  // If it is a new item, delete locally
  if (auth.isAdd) {
    // If only one item remains, prevent deletion
    if (securityConfigList.value.length === 1) {
      return;
    }
    securityConfigList.value = securityConfigList.value.filter(item => item.id !== auth.id);
    return;
  }

  loading.value = true;
  const [error] = await services.delComponent(props.id, [auth.ref]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.deleteSuccess'));
  emit('deleteSuccess');
  // After successful deletion, remove from local lists
  securityConfigList.value = securityConfigList.value.filter(item => item.id !== auth.id);
  oldSecurityConfigList.value = oldSecurityConfigList.value.filter(item => item.id !== auth.id);

  // If list is empty, add one default editable item
  if (securityConfigList.value.length === 0) {
    securityConfigList.value.unshift({ ...JSON.parse(JSON.stringify(defaultCompDetail)), id: utils.uuid('api') });
  }
};

/**
 * Validate and persist the security scheme to backend. For existing entries
 * without changes, simply exit edit mode. Builds the payload based on type.
 */
const handleSave = async (auth:ServicesCompDetail) => {
  // Validate required fields
  const checkRes = hasValidationErrors(auth);
  if (checkRes) {
    return;
  }

  if (auth.keyErr) {
    return;
  }

  // For existing item, check whether data has changed
  if (!auth.isAdd) {
    if (!isDataChanged(auth)) {
      auth.isEdit = false;
      auth.isExpand = lastIsExpandState.value;
      return;
    }
  }

  let params:any = buildSecuritySchemePayload(auth);
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

  notification.success(t('actions.tips.saveSuccess'));
  emit('saveSuccess');
  await loadSecuritySchemes();
};

/**
 * Validate required fields of a security scheme model by type.
 * Returns true if any field is missing or invalid, otherwise false.
 */
const hasValidationErrors = (_data:ServicesCompDetail):boolean => {
  let hasEmpty = false;
  if (!_data.key) {
    _data.keyErr = true;
    hasEmpty = true;
  }
  // Check whether there are empty fields in variables
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
      const variablesHasEmpty = hasEmptyApiKeyEntries(_data);
      if (variablesHasEmpty) {
        hasEmpty = variablesHasEmpty;
      }
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
            if (!_data.model[oAuth2UsernameKey]) {
              _data.model.usernameErr = true;
              hasEmpty = true;
            }
            if (!_data.model[oAuth2PasswordKey]) {
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

/**
 * Build the OpenAPI security scheme payload based on the current model type.
 * Returns false if validation fails so the caller can stop submission.
 */
const buildSecuritySchemePayload = (auth:ServicesCompDetail) => {
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
        [valueKey]: 'Bearer ' + auth.model.token
      };
    case 'apiKey': {
      const hasEmpty = hasEmptyApiKeyEntries(auth);
      if (hasEmpty) {
        return false;
      }
      const params = {
        type: 'apiKey',
        in: auth.model.apiKeyList[0].in,
        name: auth.model.apiKeyList[0].name,
        [valueKey]: auth.model.apiKeyList[0].value
      };
      if (auth.model.apiKeyList.length > 1) {
        params[securityApiKeyPrefix] = auth.model.apiKeyList.slice(1).map(item => ({ name: item.name, in: item.in, [valueKey]: item.value }));
      }
      return params;
    }
    case 'oauth2' : {
      if (auth.model[newTokenKey]) {
        let flowsParams:{
          scopes: Record<string, any>;
          authorizationUrl?: string;
          [oAuth2UsernameKey]?:string;
          [oAuth2PasswordKey]?:string;
          tokenUrl?: string;
          refreshUrl?: string;
          [oAuth2ClientAuthTypeKey]?: string;
          [oAuth2CallbackUrlKey]?: string;
          [oAuth2ClientIdKey]?: string;
          [oAuth2ClientSecretKey]?: string;
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
            if (auth.model[oAuth2CallbackUrlKey]) {
              flowsParams[oAuth2CallbackUrlKey] = auth.model[oAuth2CallbackUrlKey];
            }
            break;
          case 'password':
            flowsParams = {
              ...flowsParams,
              tokenUrl: auth.model.tokenUrl,
              [oAuth2UsernameKey]: auth.model[oAuth2UsernameKey],
              [oAuth2PasswordKey]: auth.model[oAuth2PasswordKey]
            };
            break;
          case 'implicit':
            flowsParams = {
              ...flowsParams,
              authorizationUrl: auth.model.authorizationUrl
            };
            if (auth.model[oAuth2CallbackUrlKey]) {
              flowsParams[oAuth2CallbackUrlKey] = auth.model[oAuth2CallbackUrlKey];
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
        if (auth.model[oAuth2ClientIdKey]) {
          flowsParams[oAuth2ClientIdKey] = auth.model[oAuth2ClientIdKey];
        }
        if (auth.model[oAuth2ClientSecretKey]) {
          flowsParams[oAuth2ClientSecretKey] = auth.model[oAuth2ClientSecretKey];
        }
        if (auth.model[oAuth2ClientAuthTypeKey]) {
          flowsParams[oAuth2ClientAuthTypeKey] = auth.model[oAuth2ClientAuthTypeKey];
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

/**
 * Validate scheme key uniqueness and non-empty state on change.
 */
const handleAuthKeyChange = (value:string, auth:ServicesCompDetail) => {
  auth.keyErr = !value;
  if (value) {
    const oldKeys = oldSecurityConfigList.value.map(item => item.key);
    if (oldKeys.includes(value)) {
      auth.keyErr = true;
      notification.warning('The security scheme name already exists');
    }
  }
};
const handleUsernameChange = (value:string, auth:ServicesCompDetail) => {
  auth.model.usernameErr = !value;
};
const handlePasswordChange = (value:string, auth:ServicesCompDetail) => {
  auth.model.passwordErr = !value;
};
const handleTokenChange = (value:string, auth:ServicesCompDetail) => {
  auth.model.tokenErr = !value;
};
const handleApiKeyNameChange = (value:string, apiKey:ApiKeyExtensionInfo) => {
  apiKey.nameErr = !value;
};
const handleApiKeyValueChange = (value:string, apiKey:ApiKeyExtensionInfo) => {
  apiKey.valueErr = !value;
};
/**
 * Validate required URL fields (must be non-empty and a valid URL format).
 */
const validateRequiredUrlField = (value:string, auth:ServicesCompDetail, key:'tokenUrlErr' | 'authorizationUrlErr') => {
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
/**
 * Validate optional URL fields (when present, must be a valid URL format).
 */
const validateOptionalUrlField = (value:string, auth:ServicesCompDetail, key:'callbackUrlErr' | 'refreshUrlErr') => {
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

const handleOauth2TokenChange = (value:string, auth:ServicesCompDetail) => {
  auth.model.tokenErr = !value;
};

const handleScopesChange = (value:string[], auth:ServicesCompDetail) => {
  auth.model.scopesErr = !value.length;
  if (value.length > 199) {
    value.pop();
  }
  auth.model.scopes = value;
};

/**
 * Load the full model of a security scheme from backend by reference,
 * then populate the editable model fields according to scheme type.
 */
const loadSecuritySchemeDetail = async (auth:ServicesCompDetail) => {
  const [error, { data }] = await services.getComponentRef(props.id, auth.ref);
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
      const basic = model[basicAuthKey] || {};
      auth.model.username = basic.username;
      auth.model.password = basic.password;
      break;
    }
    case 'bearer': {
      auth.model.token = model[valueKey].replace('Bearer ', '');
      break;
    }
    case 'apiKey': {
      auth.model.apiKeyList = [{ name: model.name, value: model[valueKey], in: model.in, nameErr: false, valueErr: false }];
      if (model[securityApiKeyPrefix]?.length) {
        auth.model.apiKeyList = [...auth.model.apiKeyList, ...model[securityApiKeyPrefix].map(item => ({
          name: item.name, value: item[valueKey], in: item.in, nameErr: false, valueErr: false
        }))];
      }
      break;
    }
    case 'oauth2': {
      auth.model[newTokenKey] = model[newTokenKey] || false;
      if (model[newTokenKey]) {
        const key = Object.keys(model.flows)[0] as AuthFlowKey;
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
        if (model.flows[key]?.[oAuth2CallbackUrlKey]) {
          auth.model[oAuth2CallbackUrlKey] = model.flows[key][oAuth2CallbackUrlKey];
        }
        if (model.flows[key]?.[oAuth2ClientIdKey]) {
          auth.model[oAuth2ClientIdKey] = model.flows[key][oAuth2ClientIdKey];
        }
        if (model.flows[key]?.[oAuth2ClientAuthTypeKey]) {
          auth.model[oAuth2ClientAuthTypeKey] = model.flows[key][oAuth2ClientAuthTypeKey];
        }
        if (model.flows[key]?.[oAuth2ClientSecretKey]) {
          auth.model[oAuth2ClientSecretKey] = model.flows[key][oAuth2ClientSecretKey];
        }
        if (model.flows[key]?.[oAuth2UsernameKey]) {
          auth.model[oAuth2UsernameKey] = model.flows[key][oAuth2UsernameKey];
        }
        if (model.flows[key]?.[oAuth2PasswordKey]) {
          auth.model[oAuth2PasswordKey] = model.flows[key][oAuth2PasswordKey];
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

  for (let i = 0; i < oldSecurityConfigList.value.length; i++) {
    if (oldSecurityConfigList[i]?.id === auth?.id) {
      oldSecurityConfigList[i] = auth;
      break;
    }
  }
};

// Track the currently edited item; only one can be edited simultaneously
const currEditData = ref<ServicesCompDetail>();

/**
 * Toggle expand/collapse of a scheme entry. Ensures pending edits are validated
 * and warns if there are unsaved changes before switching context.
 */
const handleExpand = async (event, auth:ServicesCompDetail) => {
  event.stopPropagation();
  if (!auth.hasModel) {
    await loadSecuritySchemeDetail(auth);
    oldSecurityConfigList.value = JSON.parse(JSON.stringify(securityConfigList.value));
  }

  const hasEditData = securityConfigList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    if (hasEditData[0].isAdd) {
      securityConfigList.value = securityConfigList.value.filter(item => item.id !== hasEditData[0].id);
    } else {
      const checkRes = hasValidationErrors(hasEditData[0]);
      if (checkRes) {
        return;
      }
      if (hasUnsavedChangesAndWarn()) {
        return;
      }
    }
  }
  auth.isExpand = !auth.isExpand;
  if (!auth.isExpand) {
    auth.isEdit = false;
  }
  collapseAndDisableEditExcept(securityConfigList.value, auth.id);
};

/**
 * Check if current editing entry has unsaved changes. If yes, show a warning.
 * Returns true when there are unsaved changes to block navigation.
 */
const hasUnsavedChangesAndWarn = () => {
  if (currEditData.value) {
    const hasUpdate = isDataChanged(securityConfigList.value.filter(item => item.id === currEditData.value?.id)[0]);
    if (hasUpdate) {
      notification.warning(t('service.securityModal.rule'));
      return true;
    }
  }
  return false;
};

/**
 * Compare a scheme entry with its snapshot to determine if data changed.
 */
const isDataChanged = (newData:ServicesCompDetail) => {
  const _oldDataList = oldSecurityConfigList.value.filter(item => item?.id === newData?.id);
  if (!_oldDataList?.length) {
    return true;
  }

  // eslint-disable-next-line
  const { isEdit: _oldEdit, isAdd: _isAdd, hasModel:_hasModel, isExpand: _oldExpand, ..._oldData } = _oldDataList[0];
  // eslint-disable-next-line
  const { isEdit, isAdd, isExpand ,hasModel, ..._newData } = newData;
  return !utils.deepCompare(_oldData, _newData);
};

const lastIsExpandState = ref(false);
/**
 * Enter edit mode for a scheme entry. Ensures model is loaded and handles
 * validation states. Only one entry can be edited at a time.
 */
const handleEdit = async (event, auth:ServicesCompDetail) => {
  event.stopPropagation();
  if (!auth.hasModel) {
    await loadSecuritySchemeDetail(auth);
    oldSecurityConfigList.value = JSON.parse(JSON.stringify(securityConfigList.value));
  }

  const hasEditData = securityConfigList.value.filter(item => item.isEdit);
  if (hasEditData?.length) {
    if (hasEditData[0].isAdd) {
      securityConfigList.value = securityConfigList.value.filter(item => item.id !== hasEditData[0].id);
    } else {
      const checkRes = hasValidationErrors(hasEditData[0]);
      if (checkRes) {
        return;
      }
      if (hasUnsavedChangesAndWarn()) {
        return;
      }
    }
  }

  lastIsExpandState.value = auth.isExpand;
  auth.isEdit = true;
  auth.isExpand = true;
  currEditData.value = auth;
  collapseAndDisableEditExcept(securityConfigList.value, auth.id);
};

/**
 * Cancel editing. For new entries, remove when multiple entries exist.
 * For existing entries, revert changes to the last snapshot if modified.
 */
const handleCancel = (auth:ServicesCompDetail) => {
  // If canceling a newly added item
  if (auth.isAdd) {
    // If list has only one item and it is newly added, prevent cancel
    if (securityConfigList.value.length === 1) {
      return;
    }
    // If list has multiple items, remove the new item and enable add button
    securityConfigList.value = securityConfigList.value.filter(item => item.id !== auth.id);
    addBtnDisabled.value = false;
    currEditData.value = undefined;
    return;
  }

  // If canceling an existing item, check changes then collapse and exit edit
  const hasUpdate = isDataChanged(auth);
  // If modified, restore data from snapshot before exiting
  if (hasUpdate) {
    const oldSync = oldSecurityConfigList.value.find(item => item.id === auth.id);
    if (!oldSync) {
      // Nothing to restore if snapshot is missing
    } else {
      Object.keys(oldSync).every((key) => {
        if (!['hasModel', 'isAdd', 'isEdit', 'isExpand'].includes(key)) {
          auth[key] = oldSync[key];
        }
        return true;
      });
    }
  }
  auth.isEdit = false;
  auth.isExpand = lastIsExpandState.value;
  currEditData.value = undefined;
  addBtnDisabled.value = false;
};

/**
 * Collapse all entries and disable edit mode except the specified one.
 */
const collapseAndDisableEditExcept = (arr:ServicesCompDetail[], id:string):void => {
  for (let i = 0; i < arr.length; i++) {
    if (arr[i].id !== id) {
      arr[i].isEdit = false;
      arr[i].isExpand = false;
    }
  }
};

/**
 * Append a new ApiKey entry after validating existing entries are complete.
 */
const addApiKeyEntry = (auth:ServicesCompDetail) => {
  const hasEmpty = hasEmptyApiKeyEntries(auth);
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

/**
 * Determine whether ApiKey entries contain any empty required fields.
 */
const hasEmptyApiKeyEntries = (auth:ServicesCompDetail):boolean => {
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

/**
 * Remove an ApiKey entry by index.
 */
const removeApiKeyEntry = (auth:ServicesCompDetail, index:number) => {
  auth.model.apiKeyList?.splice(index, 1);
};

/**
 * Reset validation flags when switching the security scheme type.
 */
const resetValidationForModelType = (auth:ServicesCompDetail) => {
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

const { notify } = useState(['notify'], 'apiSecurityStore');

onMounted(() => {
  loadSecuritySchemes();
});

watch(() => notify.value, () => {
  if (props.source === 'modal') {
    return;
  }
  loadSecuritySchemes();
});

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
    label: t('service.apiAuthorization.flowTypes.authorizationCode')
  },
  {
    value: 'password',
    label: t('common.password')
  },
  {
    value: 'implicit',
    label: t('service.apiAuthorization.flowTypes.implicit')
  },
  {
    value: 'clientCredentials',
    label: t('service.apiAuthorization.flowTypes.clientCredentials')
  }
];
</script>
<template>
  <Spin class="h-full flex flex-col" :spinning="loading">
    <Hints :text="t('service.securityModal.hints')" />
    <div class="flex items-end mt-2">
      <Button
        size="small"
        type="primary"
        class="flex items-center"
        :disabled="props.disabled || securityConfigList?.length > 49 || addBtnDisabled"
        @click="addNewSecurityConfig">
        <Icon icon="icon-jia" class="mr-1" />
        {{ t('actions.add') }}
      </Button>
    </div>
    <div
      style="scrollbar-gutter: stable;"
      class="overflow-y-auto flex flex-col space-y-2 pr-1.5 -mr-3.5 text-3 text-text-content mt-2 flex-1">
      <div
        v-for="auth in securityConfigList"
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
          <Tooltip title="Edit" placement="top">
            <template v-if="props.disabled">
              <Icon
                icon="icon-shuxie"
                class="mr-1 cursor-not-allowed text-text-disabled" />
            </template>
            <template v-else>
              <Icon
                icon="icon-shuxie"
                class="mr-1"
                :class="auth.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                @click="(e) => handleEdit(e, auth)" />
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
                :class="auth.isAdd?'text-text-placeholder cursor-not-allowed':'hover:text-text-link-hover cursor-pointer'"
                @click="handleDelete(auth)" />
            </template>
          </Tooltip>
        </div>
        <div
          class="text-3 text-text-content transition-height duration-500 overflow-hidden leading-3"
          :class="auth.isExpand ? 'open-info' : 'stop-info'">
          <template v-if="auth.isExpand">
            <div>
              <div class="flex items-end mt-2">
                <IconRequired />
                <span>{{ t('common.name') }}</span>
                <Tooltip :title="t('service.securityModal.nameTip')" placement="topLeft">
                  <Icon icon="icon-tishi1" class="ml-1 text-tips cursor-pointer text-3.5" />
                </Tooltip>
              </div>
              <Input
                v-model:value="auth.key"
                type="input"
                class="mt-2 mb-5"
                size="small"
                :maxlength="400"
                :error="auth.keyErr"
                :allowClear="false"
                :disabled="!auth.isEdit || !auth.isAdd"
                dataType="mixin-en"
                includes="_.-"
                :placeholder="t('common.placeholders.searchKeyword')"
                @change="(event)=>handleAuthKeyChange(event.target.value,auth)" />
              <div><IconRequired />{{ t('service.securityModal.typeLabel') }}</div>
              <RadioGroup
                v-model:value="auth.model.type"
                class="flex flex-wrap mt-2 mb-5"
                :options="authTypeOptions"
                :disabled="!auth.isEdit"
                @change="resetValidationForModelType(auth)">
              </RadioGroup>
              <template v-if="auth.model.type === 'basic'">
                <div><IconRequired />{{ t('service.securityModal.usernameLabel') }}</div>
                <Input
                  v-model:value="auth.model.username"
                  type="input"
                  class="mt-2 mb-5"
                  size="small"
                  :maxlength="400"
                  :error="auth.model.usernameErr"
                  :allowClear="false"
                  :disabled="!auth.isEdit"
                  :placeholder="t('service.apiAuthorization.basicusernamePlaceholder')"
                  @change="(event)=>handleUsernameChange(event.target.value,auth)" />
                <div><IconRequired />{{ t('common.password') }}</div>
                <Input
                  v-model:value="auth.model.password"
                  :error="auth.model.passwordErr"
                  :maxlength="400"
                  :disabled="!auth.isEdit"
                  class="mt-2 mb-5"
                  type="password"
                  size="small"
                  :placeholder="t('service.apiAuthorization.basic.passwordPlaceholder')"
                  @change="(event)=>handlePasswordChange(event.target.value,auth)" />
              </template>
              <template v-if="auth.model.type === 'bearer'">
                <div><IconRequired />{{ t('service.apiAuthorization.bearer.token') }} </div>
                <template v-if="auth.isEdit">
                  <Input
                    v-model:value="auth.model.token"
                    size="small"
                    class="mt-2 mb-5"
                    prefix="Bearer"
                    :placeholder="t('service.apiAuthorization.bearer.tokenPlaceholder')"
                    :error="auth.model.tokenErr"
                    :disabled="!auth.isEdit"
                    @change="(event)=>handleTokenChange(event.target.value,auth)" />
                </template>
                <template v-else>
                  <div class="p-2 bg-bg-disabled break-all whitespace-break-spaces leading-4 mt-2 mb-5 rounded" style="min-height: 26px;">
                    <span>Bearer {{ auth.model.token }}</span>
                  </div>
                </template>
              </template>
              <template v-if="auth.model.type === 'apiKey'">
                <div v-if="auth.model.apiKeyList?.length" class="-mt-2">
                  <div
                    v-for="(item,apikeyIndex) in auth.model.apiKeyList"
                    :key="apikeyIndex"
                    class="flex flex-col border-b border-dashed pb-4"
                    :class="{'border-b-0 pb-0': apikeyIndex === auth.model.apiKeyList.length-1}">
                    <div class="mt-4 flex justify-between">
                      <span><IconRequired />{{ t('common.name') }}</span>
                      <span v-if="auth.isEdit" class="text-4 -mt-1">
                        <template v-if="auth.model.apiKeyList.length < 49">
                          <Icon
                            icon="icon-tianjia"
                            class="cursor-pointer hover:text-text-link-hover"
                            @click="addApiKeyEntry(auth)" />
                        </template>
                        <template v-if="auth.model.apiKeyList.length !== 1">
                          <Icon
                            icon="icon-jianshao"
                            class="ml-2"
                            @click="removeApiKeyEntry(auth,apikeyIndex)" />
                        </template>
                      </span>
                    </div>
                    <Input
                      v-model:value="item.name"
                      size="small"
                      class="mt-2 mb-5"
                      :error="item.nameErr"
                      :allowClear="false"
                      :disabled="!auth.isEdit"
                      :placeholder="t('service.securityModal.apiKeyNamePlaceholder')"
                      @change="(event)=>handleApiKeyNameChange(event.target.value,item)" />
                    <div><IconRequired />Value</div>
                    <Input
                      v-model:value="item.value"
                      size="small"
                      class="mt-2 mb-5"
                      :error="item.valueErr"
                      :disabled="!auth.isEdit"
                      :allowClear="false"
                      :placeholder="t('service.securityModal.apiKeyValuePlaceholder')"
                      @change="(event)=>handleApiKeyValueChange(event.target.value,item)" />
                    <div> <IconRequired />Position</div>
                    <Select
                      v-model:value="item.in"
                      class="flex-1 mt-2 mb-5"
                      size="small"
                      :disabled="!auth.isEdit"
                      :allowClear="false"
                      :options="apiKeyInOptions"
                      :placeholder="t('service.securityModal.apiKeyInPlaceholder')">
                    </Select>
                  </div>
                </div>
              </template>
              <template v-if="auth.model.type === 'oauth2'">
                <div class="pl-1.75">{{ t('service.securityModal.configToken') }} </div>
                <RadioGroup
                  v-model:value="auth.model[newTokenKey]"
                  class="mt-2 mb-5"
                  :disabled="!auth.isEdit">
                  <Radio :value="false">{{ t('service.apiAuthorization.oauth2.existingToken') }}</Radio>
                  <Radio :value="true">{{ t('service.apiAuthorization.oauth2.generateToken') }}</Radio>
                </RadioGroup>
                <template v-if="!auth.model[newTokenKey]">
                  <div>{{ t('service.apiAuthorization.bearer.token') }}</div>
                  <template v-if="auth.isEdit">
                    <Input
                      v-model:value="auth.model[oAuth2Token]"
                      size="small"
                      :placeholder="t('service.securityModal.authKeyPlaceholder')"
                      class="mt-2 mb-5"
                      :maxlength="400"
                      :error="auth.model.tokenErr"
                      @change="(event)=>handleOauth2TokenChange(event.target.value,auth)" />
                  </template>
                  <template v-else>
                    <div class="p-2 bg-bg-disabled break-all whitespace-break-spaces leading-4 mt-2 mb-5 rounded" style="min-height: 26px;">
                      <span>{{ auth.model[oAuth2Token] }}</span>
                    </div>
                  </template>
                </template>
                <template v-else>
                  <div class="pl-1.75">{{ t('service.securityModal.authFlowLabel') }}</div>
                  <Select
                    v-model:value="auth.model[oAuth2Key]"
                    size="small"
                    class="w-full mt-2 mb-5"
                    :disabled="!auth.isEdit"
                    :options="OAuth2AuthorizationTypeOptions" />
                  <template v-if="['authorizationCode','implicit'].includes(auth.model[oAuth2Key])">
                    <div><IconRequired />{{ t('service.apiAuthorization.oauth2Fields.authorizationUrl') }}</div>
                    <Input
                      v-model:value="auth.model.authorizationUrl"
                      size="small"
                      :placeholder="t('service.securityModal.authorizationUrlPlaceholder')"
                      class="mt-2"
                      :disabled="!auth.isEdit"
                      :maxlength="400"
                      :error="auth.model.authorizationUrlErr.isEmpty || auth.model.authorizationUrlErr.isError"
                      @change="(event)=>validateRequiredUrlField(event.target.value,auth,'authorizationUrlErr')" />
                    <div class="h-5 text-rule">
                      <template v-if="auth.model.authorizationUrlErr.isError">
                        {{ t('service.securityModal.authorizationUrlRule') }}
                      </template>
                    </div>
                    <div class="pl-1.75">
                      {{ t('service.apiAuthorization.oauth2Fields.callbackUrl') }}
                    </div>
                    <Input
                      v-model:value="auth.model[oAuth2CallbackUrlKey]"
                      size="small"
                      class="mt-2"
                      :placeholder="t('service.securityModal.callbackUrlPlaceholder')"
                      :disabled="!auth.isEdit"
                      :error="auth.model.callbackUrlErr.isError"
                      @change="(event)=>validateOptionalUrlField(event.target.value,auth,'callbackUrlErr')" />
                    <div class="h-5 text-rule">
                      <template v-if="auth.model.callbackUrlErr.isError">
                        {{ t('service.securityModal.callbackUrlRule') }}
                      </template>
                    </div>
                  </template>
                  <template v-if="!['implicit'].includes(auth.model[oAuth2Key])">
                    <div><IconRequired />{{ t('service.apiAuthorization.oauth2Fields.tokenUrl') }}</div>
                    <Input
                      v-model:value="auth.model.tokenUrl"
                      size="small"
                      :placeholder="t('service.securityModal.tokenUrlPlaceholder')"
                      class="mt-2"
                      :disabled="!auth.isEdit"
                      :maxlength="400"
                      :error="auth.model.tokenUrlErr.isEmpty || auth.model.tokenUrlErr.isError"
                      @change="(event)=>validateRequiredUrlField(event.target.value,auth,'tokenUrlErr')" />
                    <div class="h-5 text-rule">
                      <template v-if="auth.model.tokenUrlErr.isError">
                        {{ t('service.securityModal.tokenUrlRule') }}
                      </template>
                    </div>
                  </template>
                  <div class="pl-1.75">{{ t('service.apiAuthorization.oauth2Fields.refreshUrl') }}</div>
                  <Input
                    v-model:value="auth.model.refreshUrl"
                    size="small"
                    :placeholder="t('service.securityModal.refreshUrlPlaceholder')"
                    class="mt-2"
                    :maxlength="400"
                    :disabled="!auth.isEdit"
                    :error="auth.model.refreshUrlErr.isError"
                    @change="(event)=>validateOptionalUrlField(event.target.value,auth,'refreshUrlErr')" />
                  <div class="h-5 text-rule">
                    <template v-if="auth.model.refreshUrlErr.isError">
                      {{ t('service.securityModal.refreshUrlRule') }}
                    </template>
                  </div>
                  <div class="pl-1.75">{{ t('service.securityModal.clientIdLabel') }}</div>
                  <Input
                    v-model:value="auth.model[oAuth2ClientIdKey]"
                    size="small"
                    :placeholder="t('service.securityModal.clientIdPlaceholder')"
                    class="mt-2 mb-5"
                    :maxlength="400"
                    :disabled="!auth.isEdit" />
                  <div class="pl-1.75">{{ t('service.securityModal.clientSecretLabel') }}</div>
                  <Input
                    v-model:value="
                      auth.model[oAuth2ClientSecretKey]"
                    size="small"
                    class="mt-2 mb-5"
                    :placeholder="t('service.securityModal.clientSecretPlaceholder')"
                    :maxlength="400"
                    :disabled="!auth.isEdit" />
                  <template v-if="auth.model[oAuth2Key] === 'password'">
                    <div><IconRequired />{{ t('service.securityModal.usernameLabel') }}</div>
                    <Input
                      v-model:value="auth.model[oAuth2UsernameKey]"
                      type="input"
                      class="mt-2 mb-5"
                      size="small"
                      :maxlength="400"
                      :error="auth.model.usernameErr"
                      :allowClear="false"
                      :disabled="!auth.isEdit"
                      :placeholder="t('service.apiAuthorization.basicusernamePlaceholder')"
                      @change="(event)=>handleUsernameChange(event.target.value,auth)" />
                    <div>
                      <IconRequired />
                      {{ t('common.password') }}
                    </div>
                    <Input
                      v-model:value="auth.model[oAuth2PasswordKey]"
                      :error="auth.model.passwordErr"
                      :maxlength="1024"
                      :disabled="!auth.isEdit"
                      class="mt-2 mb-5"
                      type="password"
                      size="small"
                      :placeholder="t('service.apiAuthorization.basic.passwordPlaceholder')"
                      @change="(event)=>handlePasswordChange(event.target.value,auth)" />
                  </template>
                  <div><IconRequired />Scopes</div>
                  <Select
                    :value="auth.model.scopes"
                    mode="tags"
                    class="w-full mt-2 mb-5"
                    :disabled="!auth.isEdit"
                    :error="auth.model.scopesErr"
                    :open="false"
                    :tokenSeparators="[',']"
                    :placeholder="t('service.securityModal.scopesPlaceholder')"
                    @change="(value)=>handleScopesChange(value,auth)" />
                  <div class="pl-1.75">{{ t('service.apiAuthorization.oauth2.clientAuth') }}</div>
                  <SelectEnum
                    v-model:value="auth.model[oAuth2ClientAuthTypeKey]"
                    :disabled="!auth.isEdit"
                    class="w-full mt-2 mb-5"
                    enumKey="AuthClientIn"
                    :placeholder="t('service.securityModal.clientAuthTypePlaceholder')" />
                </template>
              </template>
              <template v-if="auth.isEdit">
                <div class="pl-1.75">{{ t('common.description') }}</div>
                <Input
                  v-model:value="auth.description"
                  size="small"
                  :placeholder="t('service.securityModal.descriptionPlaceholder')"
                  class="mt-2"
                  type="textarea"
                  :rows="3"
                  :maxlength="400"
                  :disabled="!auth.isEdit" />
              </template>
              <template v-else>
                <template v-if="auth.description">
                  <div>{{ t('common.description') }}</div>
                  <Input
                    :value="auth.description"
                    size="small"
                    disabled
                    type="textarea"
                    class="mt-2"
                    :autoSize="{ minRows: 2, maxRows: 6 }" />
                </template>
              </template>
              <div v-if="auth.isEdit" class="flex justify-end mt-3">
                <Button
                  size="small"
                  class="mr-2 px-0"
                  type="link"
                  :disabled="securityConfigList.length === 1 && auth.isAdd"
                  @click="handleCancel(auth)">
                  {{ t('actions.cancel') }}
                </Button>
                <Button
                  type="link"
                  size="small"
                  class="px-0"
                  :disabled="props.disabled"
                  @click="handleSave(auth)">
                  {{ t('actions.confirm') }}
                </Button>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </Spin>
</template>
<style scoped>
:deep(.ant-input-prefix) {
  @apply text-3;
}

.open-info {
  max-height: auto;
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
