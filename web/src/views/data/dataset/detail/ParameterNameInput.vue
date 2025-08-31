<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { computed } from 'vue';
import { useParameterNameInput } from './composables/useParameterNameInput';

const { t } = useI18n();

type Props = {
  defaultValue: { name: string }[];
  columnIndex: string;
}

const props = withDefaults(defineProps<Props>(), {
  defaultValue: () => [],
  columnIndex: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: { name: string }[]): void;
}>();

// Use the composable for parameter name input logic
const {
  // State
  idList,
  dataMap,
  errorMessage,
  nameErrorSet,

  // Methods
  nameChange,
  nameBlur,
  deleteHandler,
  isValid,
  getData
} = useParameterNameInput(props, emit);

// Computed property for mapping values based on column index
const valueMap = computed(() => {
  const num = props.columnIndex ? +props.columnIndex : 0;
  const len = idList.value.length - 1;
  return idList.value.reduce((prev, cur, index) => {
    if (index < len) {
      prev[cur] = num + index;
    }
    return prev;
  }, {} as { [key: string]: number });
});

// Expose methods for parent components
defineExpose({
  getData,
  isValid
});
</script>
<template>
  <div>
    <div class="flex items-center space-x-2 mb-1 pr-9">
      <div class="w-1/2 flex items-center">
        <IconRequired />
        <span>{{ t('dataset.detail.parameterNameInput.name') }}</span>
        <Tooltip :title="t('dataset.detail.parameterNameInput.tooltip')">
          <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </div>
      <div class="flex items-center">
        <IconRequired />
        <span>{{ t('dataset.detail.parameterNameInput.readColumn') }}</span>
      </div>
    </div>

    <div class="space-y-2.5">
      <div
        v-for="(item, index) in idList"
        :key="item"
        class="flex items-center space-x-2">
        <div class="flex items-center flex-1 space-x-2">
          <div class="w-1/2 flex items-center">
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
                :placeholder="t('dataset.detail.parameterNameInput.placeholder')"
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
            :value="valueMap[item]"
            readonly
            class="w-1/2"
            trimAll
            dataType="integer" />
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

.w-1\/2 {
  width: calc((100% - 8px)/2);
}
</style>
