<script setup lang="ts">
import { computed, inject, ref, watch } from 'vue';
import { Modal, NoData, Spin } from '@xcan-angus/vue-ui';
import { Checkbox, Input } from 'ant-design-vue';
import { services } from 'src/api/tester';
import { deconstruct } from '@/utils/swagger';

interface Props {
  visible: boolean;
}
const props = withDefaults(defineProps<Props>(), {
  visible: false
});
const loading = ref(false);
const keywords = ref();
const apiBaseInfo = inject('apiBaseInfo', ref());
const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'confirm', value?: Record<string, any>):void}>();

const toggleOpenModel = () => {
  emits('update:visible', false);
};

const handleImportModel = async () => {
  if (!targetModel.value) {
    toggleOpenModel();
  }
  // const [error, resp] = await services.getCompData(apiBaseInfo.value.serviceId, ['schemas']);
  // if (error) {
  //   return;
  // }

  // const schemas = resp.data.reduce((prev, current) => {
  //   return {
  //     ...prev,
  //     [current.ref]: JSON.parse(current.model)
  //   };
  // }, {});
  // const samplingSummary = deconstruct(modelValue, resolvedRefModels.value);
  const [error, resp] = await services.getRefInfo(apiBaseInfo.value.serviceId, $ref.value);
  if (error) {
    return;
  }
  const result = deconstruct(resp.data);
  emits('confirm', { schema: result || JSON.parse(resp.data.model), ref: $ref.value });
  toggleOpenModel();
};

const options = ref<any[]>([]);
const loadOpt = async () => {
  const [error, resp] = await services.getCompData(apiBaseInfo.value.serviceId, ['requestBodies'], [], {}, false);
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
    title="组件"
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
      placeholder="查询组件名称"
      size="small" />
    <div class="leading-8 text-text-content">
      <div style="background-color: #fafafa;" class="flex items-center px-3 rounded">
        <div class="flex-1/3">组件名称</div>
        <div class="flex-1/3">修改人</div>
        <div class="flex-1/3">修改时间</div>
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
