<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, Select } from '@xcan-angus/vue-ui';
import { RadioButton, RadioGroup } from 'ant-design-vue';
import { GM } from '@xcan-angus/infra';

const { t } = useI18n();
interface Props {
    visible: boolean
}
const props = withDefaults(defineProps<Props>(), {
  visible: false
});
const emits = defineEmits<{(e: 'update:visible', value: boolean): void; (e: 'ok', value: {creatorObjectId: string; creatorObjectType: 'USER'|'GROUP'|'DEPT', creatorObjectName?: string; avatar?: string}):void}>();

const creatorObjectType = ref<'USER'|'GROUP'|'DEPT'>('USER');
const creatorObjectId = ref();
const creatorObjectName = ref();
const avatar = ref();

const handleChangeType = () => {
  creatorObjectId.value = undefined;
};

const setCreator = (value, opt) => {
  creatorObjectId.value = value;
  creatorObjectName.value = opt?.fullName || opt?.name;
  avatar.value = opt.avatar || undefined;
};

const close = () => {
  emits('update:visible', false);
};

const ok = () => {
  if (creatorObjectId.value) {
    emits('ok', {
      creatorObjectType: creatorObjectType.value,
      creatorObjectId: creatorObjectId.value,
      creatorObjectName: creatorObjectName.value,
      avatar: avatar.value
    });
  }
  close();
};
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="t('kanban.selectOrganizationPersonnel')"
    @cancel="close"
    @ok="ok">
    <RadioGroup
      v-model:value="creatorObjectType"
      buttonStyle="solid"
      size="small"
      @change="handleChangeType">
      <RadioButton value="USER">
        {{ t('kanban.user') }}
      </RadioButton>
      <RadioButton value="DEPT">
        {{ t('kanban.department') }}
      </RadioButton>
      <RadioButton value="GROUP">
        {{ t('kanban.group') }}
      </RadioButton>
    </RadioGroup>
    <div class="mt-3.5">
      <Select
        v-show="creatorObjectType === 'USER'"
        v-model:value="creatorObjectId"
        class="w-100"
        :showSearch="true"
        placeholder="t('kanban.selectUser')"
        :action="`${GM}/user?fullTextSearch=true`"
        :fieldNames="{ label: 'fullName', value: 'id' }"
        @change="setCreator">
      </Select>
      <Select
        v-show="creatorObjectType === 'DEPT'"
        v-model:value="creatorObjectId"
        class="w-100"
        placeholder="t('kanban.selectDepartment')"
        :showSearch="true"
        :action="`${GM}/dept?fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }"
        @change="setCreator">
      </Select>
      <Select
        v-show="creatorObjectType === 'GROUP'"
        v-model:value="creatorObjectId"
        class="w-100"
        placeholder="t('kanban.selectGroup')"
        :showSearch="true"
        :action="`${GM}/group?fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }"
        @change="setCreator">
      </Select>
    </div>
  </Modal>
</template>
