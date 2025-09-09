<script setup lang="ts">
import { watch, inject, ref, computed } from 'vue';
import { Modal, Spin, NoData } from '@xcan-angus/vue-ui';
import { Checkbox, Input } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
const { t }  = useI18n();
import { deconstruct } from '@xcan-angus/vue-ui/ApiUtils/index';
import { http, TESTER } from '@xcan-angus/infra';

interface Props {
  visible: boolean;
  getRefInfo: (serviceId: string, ref: string) => Promise<any>;
  getCompData: (serviceId: string, type: string[]) => Promise<any>;
}
const props = withDefaults(defineProps<Props>(), {
  visible: false
});
const loading = ref(false);
const keywords = ref();
const apiBaseInfo = inject('apiBaseInfo', ref());
const emit = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'confirm', value?: Record<string, any>):void}>();

const toggleOpenModel = () => {
  emit('update:visible', false);
};

const handleImportModel = async () => {
  if (!targetModel.value) {
    toggleOpenModel();
  }
  const [error, resp] = await http.get(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/ref`, { ref: $ref.value });
  if (error) {
    return;
  }
  const result = deconstruct(resp.data);
  emit('confirm', { schema: result || JSON.parse(resp.data.model), ref: $ref.value });
  toggleOpenModel();
};

const options = ref<any[]>([]);
const loadOpt = async () => {
  const [error, resp] = await http.get(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/type`, {
    types: ['requestBodies'],
    keys: [],
    ignoreModel: true
  });
  if (error) {
    return;
  }
  options.value = (resp.data || []).map(i => {
    return {
      ...i,
      label: i.ref,
      value: i.id
    };
  });
};

const modelValue = ref();
const $ref = ref();
const resolvedRefModels = ref();

const targetModel = ref();
const handleChangeModel = (value) => {
  targetModel.value = value;
  const modelObj = options.value.find(i => i.value === value);
  let model = modelObj.model;
  $ref.value = modelObj.ref;
  model = JSON.parse(model);
  modelValue.value = model;
  resolvedRefModels.value = modelObj.resolvedRefModels;
};

const showOptions = computed(() => {
  if (keywords.value) {
    return options.value.filter(i => i.key.includes(keywords.value));
  } else {
    return options.value;
  }
});

watch(() => props.visible, newValue => {
  if (newValue && !options.value.length) {
    loadOpt();
  }
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="t('xcan_apiBody.component')"
    @cancel="toggleOpenModel"
    @ok="handleImportModel">
    <!-- <Divider /> -->
    <!-- <RadioGroup
      v-model:value="targetModel"
      :options="options"
      @change="handleChangeModel" /> -->
    <Input
      v-model:value="keywords"
      class="w-50 mb-2"
      :placeholder="t('xcan_apiBody.searchComponentName')"
      size="small" />
    <div class="leading-8 text-theme-content">
      <div style="background-color: #fafafa;" class="flex items-center px-3 rounded">
        <div class="flex-1/3">{{ t('xcan_apiBody.componentName') }}</div>
        <div class="flex-1/3">{{ t('xcan_apiBody.modifier') }}</div>
        <div class="flex-1/3">{{ t('xcan_apiBody.modifyTime') }}</div>
      </div>
      <Spin :spinning="loading">
        <template v-if="!loading&&!showOptions?.length">
          <NoData class="flex items-center min-h-26" />
        </template>
        <template v-else>
          <div style="min-height: 104px;max-height: 328px;" class="px-3 pt-2 overflow-y-auto">
            <div
              v-for="item in showOptions"
              :key="item.id"
              class="flex items-center">
              <div class="flex-1/3 flex items-center">
                <Checkbox :checked="targetModel === item.value" @change="handleChangeModel(item.value)">{{ item.key }}</Checkbox>
              </div>
              <div class="flex-1/3">{{ item.lastModifiedByName }}</div>
              <div class="flex-1/3">{{ item.lastModifiedDate }}</div>
            </div>
          </div>
        </template>
      </Spin>
    </div>
  </Modal>
</template>
