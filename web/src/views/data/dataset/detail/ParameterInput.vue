<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { IconRequired, Tooltip, Input, Icon, FunctionsButton, IconCopy } from '@xcan-angus/vue-ui';
import { useParameterInput } from './composables/useParameterInput';

const { t } = useI18n();

export interface Option {
  name: string;
  value: string;
}

export interface Props {
  defaultValue: Option[];
}

const props = withDefaults(defineProps<Props>(), {
  defaultValue: () => []
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: Option[]): void;
}>();

// Use the composable for parameter input logic
const {
  // State
  idList,
  dataMap,
  errorMessage,
  nameErrorSet,
  valueErrorSet,

  // Methods
  nameChange,
  nameBlur,
  valueChange,
  deleteHandler,
  isValid,
  getData
} = useParameterInput(props, emit);

// Expose methods for parent components
defineExpose({
  getData,
  isValid
});
</script>
<template>
  <div>
    <div class="flex items-center space-x-2 mb-1">
      <div class="w-100 flex items-center">
        <IconRequired />
        <span>{{ t('common.name') }}</span>
      </div>
      <div class="flex-1 flex items-center">
        <IconRequired />
        <span>{{ t('common.value') }}</span>
      </div>
      <FunctionsButton class="text-3.5" />
    </div>

    <div class="space-y-2.5">
      <div
        v-for="(item, index) in idList"
        :key="item"
        class="flex items-center space-x-2">
        <div class="flex items-center flex-1 space-x-2">
          <div class="w-100 flex items-center">
            <Tooltip
              :title="errorMessage.get(item)"
              internal
              placement="right"
              destroyTooltipOnHide
              :visible="!!errorMessage.get(item)">
              <Input
                v-model:value="dataMap[item].name"
                :maxLength="100"
                :error="nameErrorSet.has(item)"
                excludes="{}"
                includes="\!\$%\^&\*_\-+=\.\/"
                dataType="mixin-en"
                :placeholder="t('common.placeholders.searchKeyword')"
                size="small"
                tirmAll
                class="flex-1 has-suffix"
                @change="nameChange($event, item, index)"
                @blur="nameBlur($event, item)">
                <template #suffix>
                  <div v-if="dataMap[item].name" class="h-full flex items-center overflow-hidden">
                    <div :title="`{${dataMap[item].name}}`" class="flex-1 flex items-center text-3 overflow-hidden">
                      <span>{</span>
                      <span class="truncate">{{ dataMap[item].name }}</span>
                      <span>}</span>
                    </div>
                    <IconCopy :copyText="`{${dataMap[item].name}}`" class="flex-shrink-0 ml-1.75" />
                  </div>
                </template>
              </Input>
            </Tooltip>
          </div>
          <Input
            v-model:value="dataMap[item].value"
            :maxlength="4096"
            :error="valueErrorSet.has(item)"
            class="flex-1"
            trim
            :placeholder="t('common.placeholders.inputMockValue')"
            @change="valueChange($event, item)" />
        </div>
        <Button
          class="w-7 p-0"
          type="default"
          size="small"
          @click="deleteHandler(item, index)">
          <Icon icon="icon-shanchuguanbi" />
        </Button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.has-suffix :deep(.ant-input-suffix) {
  display: inline-block;
  width: 110px;
  margin-left: 4px;
  padding-left: 7px;
  overflow: hidden;
  border-left: 1px solid var(--border-text-box);
}
</style>
