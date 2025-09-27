<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Input, IconRequired, IconCopy } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

export interface Props {
  value: {
    id: string;
    name: string;
    description: string;
    scriptId?: string;
    scriptName?: string;
  };

  loading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  loading: false
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'save', value: { name: string; description: string; }): void;
  (e: 'canecel'): void;
}>();

const name = ref<string>();
const nameError = ref(false);
const description = ref<string>();

const nameChange = (event: { target: { value: string; } }) => {
  const value = event.target.value;
  name.value = value;
  nameError.value = false;
};

const descriptionChange = (event: { target: { value: string; } }) => {
  const value = event.target.value;
  description.value = value;
};

const isValid = (): boolean => {
  nameError.value = false;
  let errorNum = 0;
  if (!name.value) {
    errorNum++;
    nameError.value = true;
  }

  return !errorNum;
};

const getData = () => {
  return { description: description.value, name: name.value };
};

const save = () => {
  if (!isValid()) {
    return;
  }

  emit('save', getData());
};

const cancel = () => {
  emit('canecel');
  init();
};

const init = () => {
  nameError.value = false;
  const data = props.value;
  if (!data) {
    return;
  }

  const { name: _name, description: _description } = data;
  name.value = _name;
  description.value = _description;
};

onMounted(() => {
  watch(() => props.value, () => {
    init();
  }, { immediate: true });
});

const scriptId = computed(() => {
  return props.value?.scriptId || '';
});

const scriptName = computed(() => {
  return props.value?.scriptName || '';
});

const id = computed(() => {
  if (!scriptId.value) {
    return '';
  }

  return props.value?.id;
});

defineExpose({ isValid, getData });
</script>

<template>
  <div class="space-y-4 leading-5">
    <div v-if="id" class="space-y-0">
      <div class="flex items-center">
        <span>{{ t('common.id') }}</span>
      </div>
      <div class="flex-1 flex items-center space-x-2">
        <span :title="id" class="truncate">{{ id }}</span>
        <IconCopy class="flex-shrink-0" :copyText="id" />
      </div>
    </div>
    <div class="space-y-0.5">
      <div class="flex items-center">
        <IconRequired />
        <span>{{ t('ftpPlugin.drawerMenu.saveForm.name') }}</span>
      </div>
      <Input
        :maxlength="200"
        :value="name"
        :error="nameError"
        :placeholder="t('common.placeholders.searchKeyword')"
        trim
        @change="nameChange" />
    </div>

    <div v-if="scriptName" class="space-y-0">
      <div class="flex items-center">
        <span>{{ t('common.scriptName') }}</span>
      </div>
      <div class="flex-1 flex items-center space-x-2">
        <span :title="scriptName" class="truncate">{{ scriptName }}</span>
        <IconCopy class="flex-shrink-0" :copyText="scriptName" />
      </div>
    </div>

    <div v-if="scriptId" class="space-y-0">
      <div class="flex items-center">
        <span>{{ t('common.scriptId') }}</span>
      </div>
      <div class="flex-1 flex items-center space-x-2">
        <span :title="scriptId" class="truncate">{{ scriptId }}</span>
        <IconCopy class="flex-shrink-0" :copyText="scriptId" />
      </div>
    </div>
    <div class="space-y-0.5">
      <div class="flex items-center">{{ t('common.description') }}</div>
      <Input
        :maxlength="800"
        :value="description"
        :autoSize="{ minRows: 5, maxRows: 5 }"
        showCount
        type="textarea"
        :placeholder="t('ftpPlugin.drawerMenu.saveForm.descriptionPlaceholder')"
        trim
        @change="descriptionChange" />
    </div>
    <div class="flex items-center space-x-2 !mt-5">
      <Button
        type="primary"
        size="small"
        :loading="props.loading"
        @click="save">
        {{ t('common.confirm') }}
      </Button>
      <Button
        type="default"
        size="small"
        @click="cancel">
        {{ t('actions.cancel') }}
      </Button>
    </div>
  </div>
</template>
