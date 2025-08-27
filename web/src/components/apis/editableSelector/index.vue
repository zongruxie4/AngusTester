<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Input } from '@xcan-angus/vue-ui';
import { Dropdown, Menu, MenuItem } from 'ant-design-vue';

const { t } = useI18n();

interface Props {
  options: string[];
  value: string;
  [key: string]: any;
  maxLength: number;
}

const emits = defineEmits<{(e: 'select', value: string)}>();

const props = withDefaults(defineProps<Props>(), {
  options: () => ([]),
  value: undefined,
  maxLength: 4096
});
const inputValue = ref();

const handleSelect = (item) => {
  inputValue.value = item.key;
  setTimeout(() => {
    emits('select', inputValue.value);
  });
};

const inputProps = computed(() => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { value, options, ...others } = props;
  return { ...others };
});

watch(() => props.value, newValue => {
  inputValue.value = newValue;
}, {
  immediate: true
});

</script>
<template>
  <Dropdown overlayClassName="border rounded" :overlayStyle="{maxHeight: '300px', overflow: 'auto'}">
    <Input
      v-model:value="inputValue"
      :placeholder="t('commonComp.apis.editableSelector.placeholder')"
      v-bind="inputProps"
      size="small">
      <template #suffix>
        <Icon icon="icon-xiajiantou" />
      </template>
    </Input>
    <template #overlay>
      <Menu @click="handleSelect">
        <MenuItem v-for="item in props.options" :key="item">{{ item }}</MenuItem>
      </Menu>
    </template>
  </Dropdown>
</template>
