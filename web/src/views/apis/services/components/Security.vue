<script lang="ts" setup>
import { inject, ref, watch } from 'vue';
import { Hints, Icon, Input, notification, Select } from '@xcan-angus/vue-ui';
import { Button, Dropdown, Menu, MenuItem } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { apis, services } from '@/api/tester';

interface Props {
  data: Record<string, any>[];
  id: string;
  type: 'SERVICE'|'API';
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  data: () => ([]),
  id: '',
  type: 'API',
  disabled: false
});

const { t } = useI18n();

const serviceId = inject('serviceId', ref());
const emits = defineEmits<{(e: 'update:showSecurity', value: boolean):void}>();

// Reactive reference for security requirements data
const security = ref<{key: string; value: string[]; defaultKey?: string; defaultValue?: string[]; edit?: boolean; symbolKey?: number}[][]>([]);
// Reactive reference for default security requirements data
const defaultSecurity = ref<{key: string; value: string[]}[]>([]);

/**
 * Handle selection of security scheme from dropdown
 * @param item - The selected menu item
 * @param idx - The index of the security group
 * @param subIdx - The index of the security item within the group
 */
const handleSelectSecurity = (item: { key: string | number }, idx: number, subIdx: number) => {
  security.value[idx][subIdx].key = String(item.key);
  showOptions.value[`${idx}${subIdx}`] = false;
  security.value[idx][subIdx].value = security.value[idx][subIdx].value || [];
};

/**
 * Add a new security requirement group
 */
const addSecurity = () => {
  security.value.push([{ key: '', value: [], edit: true }]);
  emits('update:showSecurity', true);
  editSecurity();
};

/**
 * Add a new security requirement to an existing group
 * @param idx - The index of the security group to add to
 */
const addSubSecurity = (idx: number) => {
  security.value[idx].push({ key: '', value: [] });
};

// Reactive reference for edit mode state
const edit = ref(false);

/**
 * Enable edit mode for security requirements
 */
const editSecurity = () => {
  edit.value = true;
  isValid.value = false;
};

/**
 * Validate security keys when input loses focus
 */
const blurKeys = () => {
  if (isValid.value) {
    getRepeatedKeys();
  }
};

// Reactive reference for validation state
const isValid = ref(false);
// Reactive reference for repeated security keys
const repeatedKeys = ref<string[][]>([]);

/**
 * Validate security requirements for duplicate keys
 * @returns Boolean indicating if validation passed
 */
const validate = (): boolean => {
  isValid.value = true;
  getRepeatedKeys();
  return !repeatedKeys.value.flat().length;
};

/**
 * Check for repeated security keys within groups
 * @returns Array of repeated keys for each group
 */
const getRepeatedKeys = (): string[][] => {
  const result: string[][] = [];
  security.value.forEach(item => {
    const keys: Record<string, boolean> = {};
    const repeatKeys: string[] = [];
    item.forEach(i => {
      if (!i.key) {
        return;
      }
      if (keys[i.key]) {
        repeatKeys.push(i.key);
      } else {
        keys[i.key] = true;
      }
    });
    result.push(repeatKeys);
  });
  repeatedKeys.value = result;
  return result;
};

/**
 * Save security requirements after validation
 */
const saveSecurity = async () => {
  if (!validate()) {
    notification.warning(t('service.security.nameRule'));
    return;
  }

  const params = security.value.map(item => {
    const param: Record<string, string[]> = {};
    item.forEach(i => {
      if (i.key) {
        param[i.key] = i.value;
      }
    });
    return param;
  });
  await saveApi(params);
  edit.value = false;
};

/**
 * Cancel security editing and reset to initial state
 */
const cancelSecurity = () => {
  edit.value = false;
  initData();
  emits('update:showSecurity', security.value.length > 0);
};

/**
 * Delete a security requirement
 * @param subIdx - The index of the security item to delete
 * @param idx - The index of the security group
 */
const delSecurity = async (subIdx: number, idx: number) => {
  security.value[idx].splice(subIdx, 1);
  if (security.value[idx].length < 1) {
    security.value.splice(idx, 1);
  }
};

/**
 * Save API or service security requirements based on type
 * @param params - The security parameters to save
 * @returns Promise resolving to the API response
 */
const saveApi = (params: Record<string, string[]>[]) => {
  if (props.type === 'API') {
    return apis.updateApi([{ id: props.id, security: params }]);
  } else {
    return services.updateSchemaSecurity(props.id, params);
  }
};

// Reactive reference for dropdown visibility state
const showOptions = ref<{[key: string]: boolean}>({});
// Reactive reference for security scheme options
const securityOpt = ref<{ label: string, value: string, messages: Record<string, string>, schemaValue?: string }[]>([]);

/**
 * Load available security scheme options for the service
 * @param serviceId - The ID of the service to load options for
 */
const loadSecurityNameOptions = async (serviceId: string) => {
  const [error, resp] = await services.getCompData(serviceId, ['securitySchemes'], [], false);
  if (error) {
    return;
  }
  securityOpt.value = (resp.data || []).map(item => {
    return {
      label: item.key,
      value: item.key
    };
  });
};

/**
 * Initialize component data from props
 */
const initData = () => {
  security.value = [];
  edit.value = false;
  isValid.value = false;
  props.data.forEach(item => {
    security.value.push(Object.keys(item).map((key) => {
      return {
        key,
        value: item[key] || []
      };
    }));
  });
  defaultSecurity.value = JSON.parse(JSON.stringify(security.value));
};

// Watch for service ID changes and load security options
watch(() => serviceId.value, newValue => {
  if (newValue) {
    loadSecurityNameOptions(newValue);
  }
}, {
  immediate: true
});

// Watch for data prop changes and reinitialize
watch(() => props.data, () => {
  initData();
  emits('update:showSecurity', security.value.length > 0);
}, {
  immediate: true
});

defineExpose({ addSecurity: addSecurity });
</script>
<template>
  <div class="pl-2 -mt-2">
    <Hints :text="t('service.security.hints')" />
    <Icon
      v-show="!props.disabled && !edit && !!security.length"
      icon="icon-shuxie"
      class="text-3.5 mx-1 cursor-pointer text-text-link"
      @click="editSecurity()" />
    <div
      v-for="(item, idx) in security"
      :key="idx"
      class="mb-4">
      <div v-show="item.length" class="my-1 flex items-center space-x-2">
        <span>security {{ idx + 1 }}</span>
        <Icon
          v-if="edit"
          icon="icon-jia"
          @click="addSubSecurity(idx)" />
      </div>
      <div
        v-for="(subItem, subIdx) in item"
        :key="subIdx"
        class="flex mb-2 space-x-1">
        <div class="flex-1">
          <Dropdown v-model:visible="showOptions[`${idx}${subIdx}`]" :trigger="['click']">
            <Input
              v-model:value="subItem.key"
              :disabled="props.disabled || !edit"
              :error="isValid && repeatedKeys[idx].includes(subItem.key)"
              :placeholder="t('common.placeholders.searchKeyword')"
              size="small"
              class="w-full"
              @blur="blurKeys" />
            <template #overlay>
              <Menu v-if="!!securityOpt.length" @click="handleSelectSecurity($event, idx, subIdx)">
                <MenuItem v-for="opt in securityOpt" :key="opt.value">{{ opt.label }}</MenuItem>
              </Menu>
            </template>
          </Dropdown>
          <Select
            v-model:value="subItem.value"
            mode="tags"
            :disabled="props.disabled || !edit"
            :placeholder="t('service.security.valuePlaceholder')"
            size="small"
            class="mt-1 w-full" />
        </div>
        <div>
          <Icon
            icon="icon-qingchu"
            class="text-3.5 cursor-pointer text-text-link"
            @click="delSecurity(subIdx, idx)" />
        </div>
      </div>
    </div>
    <template v-if="edit && !!security.length">
      <Button
        type="link"
        size="small"
        @click="saveSecurity()">
        {{ t('actions.save') }}
      </Button>
      <Button
        type="link"
        size="small"
        @click="cancelSecurity()">
        {{ t('actions.cancel') }}
      </Button>
    </template>
  </div>
</template>
