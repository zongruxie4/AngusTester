<script setup lang="ts">
import { computed, ref } from 'vue';
import { Radio, RadioGroup, TypographyParagraph } from 'ant-design-vue';
import { Colon, Icon, Modal, Select, notification } from '@xcan-angus/vue-ui';
import { TESTER, http, utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  visible: boolean;
  serviceId: string;
  tips: string;
  okAction: string;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  visible: false,
  serviceId: undefined,
  tips: undefined,
  okAction: undefined,
  title: undefined
});


const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'update:id', value: string | undefined): void;
  (e: 'ok'): void;
}>();

const checkedType = ref<'none' | 'checked'>('none');
const confirmLoading = ref(false);
const selectedIds = ref<string[]>([]);
const selectedServers = ref<{
  url: string;
  description: string;
  'x-xc-id': string;
  variables: {
    id: string;
    name: string;
    description: string;
    default: string;
    enum: { id: string; value: string }[];
  }[];
}[]>([]);

const radioChange = (event:{target:{value:'none'|'checked'}}) => {
  const value = event.target.value;
  if (value === 'none') {
    selectedIds.value = [];
    selectedServers.value = [];
  }
};

const selectChange = (value: string[], option: {
  url: string;
  description: string;
  variables: {
    [key: string]: {
      description: string;
      default: string;
      enum: string[];
    }
  };
  'x-xc-id': string;
}[]) => {
  selectedIds.value = value;
  selectedServers.value = option.map(item => {
    const variables: {
      id: string;
      name: string;
      description: string;
      default: string;
      enum: { id: string; value: string }[];
    }[] = [];
    if (item.variables) {
      const names = Object.keys(item.variables);
      for (let i = 0, len = names.length; i < len; i++) {
        const _name = names[i];
        const enumList = item.variables[_name]?.enum?.map(_value => {
          return {
            id: utils.uuid(),
            value: _value
          };
        });
        variables.push({
          ...item.variables[_name],
          enum: enumList,
          name: _name,
          id: utils.uuid()
        });
      }
    }

    return {
      ...item,
      variables
    };
  });
};

const reset = () => {
  checkedType.value = 'none';
  confirmLoading.value = false;
  selectedIds.value = [];
  selectedServers.value = [];
};

const cancel = () => {
  reset();
  emit('update:visible', false);
  emit('update:id', undefined);
};

const getSaveParams = (data) => {
  const variables = data.variables.reduce((prev, cur) => {
    prev[cur.name] = {
      default: cur.default,
      description: cur.description,
      enum: cur.enum?.map(item => item.value) || []
    };

    return prev;
  }, {} as {
      [key:string]:{
        default:string;
        description:string;
        enum:string[];
      }
    });
  const params:{
    description?:string;
    url:string;
    variables:{
      [key:string]:{
        default:string;
        description:string;
        enum:string[];
      }
    };
    'x-xc-id'?:string;
  } = {
    'x-xc-id': data['x-xc-id'],
    description: data.description,
    url: data.url,
    variables
  };

  return params;
};

const getParams = () => {
  return selectedServers.value.map(item => {
    return getSaveParams(item);
  });
};

const ok = async () => {
  const params = getParams();
  confirmLoading.value = true;
  const [error] = await http.put(props.okAction, params, { dataType: true });
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.execSuccess'));
  reset();
  emit('update:visible', false);
  emit('update:id', undefined);
};
const okButtonProps = computed(() => {
  return {
    disabled: false
  };
});
const modalTitle = computed(() => {
  return props.title || t('service.sidebar.execTest.title');
});
</script>

<template>
  <Modal
    :visible="props.visible"
    :width="750"
    :confirmLoading="confirmLoading"
    :okButtonProps="okButtonProps"
    :title="modalTitle"
    @cancel="cancel"
    @ok="ok">
    <div class="leading-5">
      <div class="text-theme-sub-content mb-2.5">{{ props.tips }}</div>

      <div class="flex items-start mb-2.5">
        <div class="flex items-center flex-shrink-0 mr-2.5 leading-5.5">
          <span>{{ t('service.sidebar.execTest.serviceConfigLabel') }}</span>
          <Colon />
        </div>
        <RadioGroup
          v-model:value="checkedType"
          name="radioGroup"
          @change="radioChange">
          <Radio value="none">{{ t('service.sidebar.execTest.serviceConfigDefault') }}</Radio>
          <Radio value="checked">{{ t('service.sidebar.execTest.serviceConfigModify') }}</Radio>
        </RadioGroup>
      </div>

      <template v-if="checkedType==='checked'">
        <Select
          v-model:value="selectedIds"
          :action="`${TESTER}/services/${props.serviceId}/test/schema/server`"
          :fieldNames="{ label: 'url', value: 'x-xc-id' }"
          mode="multiple"
          style="width: 585px;margin-left: 75px;"
          class="mb-3"
          :placeholder="t('service.sidebar.execTest.selectServicePlaceholder')"
          @change="selectChange">
          <template #option="record">
            <div class="flex items-center overflow-hidden">
              <div class="truncate" :title="record.url">{{ record.url }}</div>
              <div
                v-if="!record.variables||!Object.keys(record.variables).length"
                class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 -translate-y-0.25"
                style="color: rgba(245, 34, 45, 100%);line-height: 15px;">
                <span class="inline-block transform-gpu scale-90">{{ t('service.sidebar.execTest.variableEmpty') }}</span>
              </div>
            </div>
          </template>
        </Select>

        <div
          v-if="!!selectedServers.length"
          style="max-height: 380px;"
          class="space-y-2.5 pl-18.75 overflow-auto">
          <div
            v-for="item in selectedServers"
            :key="item['x-xc-id']"
            class="p-2.5 border border-solid border-theme-text-box rounded">
            <div :title="item.url" class="flex items-center text-theme-title font-semibold truncate mb-2">
              <Icon icon="icon-host" class="text-3.5 mr-1.5" />
              <span>{{ item.url }}</span>
            </div>

            <div v-if="false" class="flex items-start leading-4.5 mb-2">
              <div class="flex-shrink-0 text-theme-sub-content mr-2">
                <span>URL</span>
                <Colon />
              </div>
              <TypographyParagraph
                class="break-all"
                :ellipsis="{ rows: 2, expandable: true, symbol: '更多' }"
                :content="item.url" />
            </div>

            <div class="flex items-start leading-4.5 mb-2.5">
              <div v-if="false" class="flex-shrink-0 text-theme-sub-content mr-2">
                <span>{{ t('service.sidebar.execTest.descLabel') }}</span>
                <Colon />
              </div>
              <TypographyParagraph
                v-if="item.description"
                class="break-all"
                :ellipsis="{ rows: 2, expandable: true, symbol: '更多' }"
                :content="item.description" />
              <div v-else>{{ t('service.sidebar.execTest.descEmpty') }}</div>
            </div>

            <div v-if="!!item.variables?.length">
              <div v-if="false" class="text-theme-sub-content mb-0.5">{{ t('service.sidebar.execTest.variableLabel') }}</div>
              <div class="space-y-5">
                <div
                  v-for="_variable in item.variables"
                  :key="_variable.id"
                  class="space-y-2 relative variable-item">
                  <div class="flex items-start leading-4.5">
                    <div v-if="false" class="w-10 flex-shrink-0 text-theme-sub-content">
                      <span>{{ t('service.sidebar.execTest.variableNameLabel') }}</span>
                      <Colon />
                    </div>
                    <div :title="_variable.name" class="text-theme-title font-semibold flex-1 truncate">
                      <em class="w-1.25 h-1.25 inline-block rounded bg-slate-600 mr-1.25"></em>
                      <span>{{ _variable.name }}</span>
                    </div>
                  </div>

                  <div class="flex items-start leading-4.5">
                    <div v-if="false" class="w-10 flex-shrink-0 text-theme-sub-content">
                      <span>{{ t('service.sidebar.execTest.variableValueLabel') }}</span>
                      <Colon />
                    </div>
                    <div class="flex-1 space-y-1 pl-2.5">
                      <div
                        v-for="_enum in _variable.enum"
                        :key="_enum.id"
                        class="flex items-center justify-between">
                        <div :title="_enum.value" class="truncate flex-1">{{ _enum.value }}</div>
                        <div class="flex items-center leading-5">
                          <div v-if="_enum.value === _variable.default" class="mr-1 text-text-sub-content text-3">{{ t('service.sidebar.execTest.variableValueDefault') }}</div>
                          <Radio
                            size="small"
                            :checked="_enum.value === _variable.default"
                            style="transform: translateY(-4px);"
                            @change="_variable.default=_enum.value" />
                        </div>
                        <!-- <div :class="{ invisible: _enum.value !== _variable.default }" class="flex-shrink-0">默认</div> -->
                      </div>
                    </div>
                  </div>

                  <div v-if="false" class="flex items-start leading-4.5">
                    <div class="w-10 flex-shrink-0 text-theme-sub-content">
                      <span>{{ t('service.sidebar.execTest.variableDescLabel') }}</span>
                      <Colon />
                    </div>
                    <TypographyParagraph
                      class="break-all"
                      :ellipsis="{ rows: 2, expandable: true, symbol: t('actions.more') }"
                      :content="_variable.description" />
                  </div>
                </div>
              </div>
            </div>
            <div v-else>{{ t('service.sidebar.execTest.variableEmpty') }}</div>
          </div>
        </div>
      </template>
    </div>
  </Modal>
</template>

<style scoped>
.variable-item::after {
  content: "";
  display: block;
  position: absolute;
  bottom: -11px;
  left:0;
  width: 100%;
  height: 0;
  border-top: 1px dashed var(--border-text-box);
}

.variable-item:last-child::after {
  display: none;
}
</style>
