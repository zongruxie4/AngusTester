<script lang="ts" setup>
import { inject, ref, watch } from 'vue';
import { Hints, Icon, Input, notification, Select } from '@xcan-angus/vue-ui';
import { Button, Dropdown, Menu, MenuItem } from 'ant-design-vue';
import { apis, services } from '@/api/altester';

interface Props {
  data: Record<string, any>[];
  id: string;
  type: 'PROJECT'|'API';
  disabled: boolean;
}

const serviceId = inject('serviceId', ref());
const emits = defineEmits<{(e: 'update:showSecurity', value: boolean):void}>();
const props = withDefaults(defineProps<Props>(), {
  data: () => ([]),
  id: '',
  type: 'API',
  disabled: false
});

const security = ref<{key: string; value: string[]; defaultKey?: string; defaultValue?: string[]; edit?: boolean; symbolKey?: number}[][]>([]);
const defaultSecurity = ref<{key: string; value: string[]}[]>([]);

// const handleBlur = (idx) => {
//   if (securityOpt.value.length) {
//     showOptions.value[idx] = true;
//   }
// };

const handleSelectSecurity = (item, idx, subIdx) => {
  security.value[idx][subIdx].key = item.key;
  showOptions.value[`${idx}${subIdx}`] = false;
  security.value[idx][subIdx].value = security.value[idx][subIdx].value || [];
};

// 添加安全
const addSecurity = () => {
  security.value.push([{ key: '', value: [], edit: true }]);
  emits('update:showSecurity', true);
  editSecurity();
};

const addSubSecurity = (idx) => {
  security.value[idx].push({ key: '', value: [] });
};

// 修改安全需求
const edit = ref(false);
const editSecurity = () => {
  edit.value = true;
  isValid.value = false;
};

const blurKeys = () => {
  if (isValid.value) {
    getRepeatedKeys();
  }
};

const isValid = ref(false);
const repeatedKeys = ref<string[][]>([]);
const validate = () => {
  isValid.value = true;
  getRepeatedKeys();
  if (repeatedKeys.value.flat().length) {
    return false;
  }
  return true;
};

const getRepeatedKeys = () => {
  const result: string[][] = [];
  security.value.forEach(item => {
    const keys = {};
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

// 保存安全需求
const saveSecurity = async () => {
  if (!validate()) {
    notification.warning('定义名称重复');
    return;
  }

  const params = security.value.map(item => {
    const param = {};
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

const cancelSecurity = () => {
  edit.value = false;
  initData();
  emits('update:showSecurity', security.value.length > 0);
};

// 删除安全需求
const delSecurity = async (subIdx, idx) => {
  security.value[idx].splice(subIdx, 1);
  if (security.value[idx].length < 1) {
    security.value.splice(idx, 1);
  }
};

const saveApi = (params) => {
  if (props.type === 'API') {
    return apis.updateApi([{ id: props.id, security: params }]);
  } else {
    return services.updateSchemaSecurity(props.id, params);
  }
};

// 已存在的项目安全需求数据
const showOptions = ref<{[key: string]: boolean}>({});
const securityOpt = ref<{ label: string, value: string, messages: Record<string, string>, schemaValue?: string }[]>([]);
const loadSecurityNameOptions = async (serviceId) => {
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

watch(() => serviceId.value, newValue => {
  if (newValue) {
    loadSecurityNameOptions(newValue);
  }
}, {
  immediate: true
});

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
    <Hints text="声明可用于接口的安全方案" />
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
              placeholder="安全方案名称"
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
            placeholder="访问scope，输入回车确认"
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
        保存
      </Button>
      <Button
        type="link"
        size="small"
        @click="cancelSecurity()">
        取消
      </Button>
    </template>
  </div>
</template>
