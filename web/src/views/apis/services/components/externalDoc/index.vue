<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Hints, Icon, Input } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { apis } from 'src/api/tester';

interface Props {
  data?: {
    url: string;
    description?: string
  };
  id: string;
}
const { t } = useI18n();
const emits = defineEmits<{(e: 'update:data', value: {url: string;description?: string}): void}>();

const url = ref();
const description = ref();
const editable = ref(false);

const props = withDefaults(defineProps<Props>(), {
  data: () => ({ url: '' }),
  id: ''
});

const editDoc = () => {
  editable.value = true;
};

const cancel = () => {
  editable.value = false;
  url.value = props.data.url;
  description.value = props.data.description;
};

const saveDoc = async () => {
  const externalDocs = { ...props.data, url: url.value, description: description.value };
  const [error] = await apis.updateApi([{ id: props.id, externalDocs }]);
  if (error) {
    return;
  }
  emits('update:data', externalDocs);
  editable.value = false;
};

watch(() => props.data, newValue => {
  url.value = newValue.url;
  description.value = newValue.description;
});

</script>
<template>
  <div class="pl-2">
    <Hints :text="t('service.externalDoc.hints')" />
    <div class="flex">
      <div>
        <Input
          v-model:value="url"
          :maxLength="100"
          :placeholder="t('service.externalDoc.urlPlaceholder')"
          :disabled="!editable" />
        <Input
          v-model:value="description"
          :disabled="!editable"
          :maxLength="100"
          type="textarea"
          :placeholder="t('service.externalDoc.descriptionPlaceholder')"
          class="mt-1" />
      </div>
      <Icon
        icon="icon-shuxie"
        class="text-4 ml-1 cursor-pointer text-text-link"
        @click="editDoc" />
    </div>
    <template v-if="editable">
      <Button
        type="link"
        size="small"
        @click="saveDoc">
        {{t('actions.save')}}
      </Button>
      <Button
        type="link"
        size="small"
        @click="cancel">
        {{t('actions.cancel')}}
      </Button>
    </template>
  </div>
</template>
