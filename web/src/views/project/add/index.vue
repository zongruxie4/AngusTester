<script lang="ts" setup>
import { computed, inject, ref, watch, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { DatePicker, Icon, Image, Input, Modal, notification, Select, SelectUser } from '@xcan-angus/vue-ui';
import { Divider, Form, FormItem, RadioButton, RadioGroup, Upload, Popover } from 'ant-design-vue';
import { project } from 'src/api/tester';
import { GM, upload, appContext } from '@xcan-angus/infra';

const { t } = useI18n();

export type Project = {
  name: string;
  ownerId: string;
  id: string;
  avatar?: string;
  description?: string;
  members?: { [key: string]: any },
  startDate: string;
  deadlineDate: string;
}

interface Props {
  visible: boolean;
  dataSource?: Project,
  closable: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  dataSource: undefined,
  closable: true
});
const userInfo = ref(appContext.getUser());

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'cancel', value: boolean): void;
  (e: 'ok'): void;
}>();

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const formRef = ref();
const memberType = ref('user');

const projectType = ref('AGILE');

const projectTypeTipConfig = {
  AGILE: [t('project.projectAddModal.projectTypeTip.agile.features'), t('project.projectAddModal.projectTypeTip.agile.scenarios')],
  GENERAL: [t('project.projectAddModal.projectTypeTip.general.features'), t('project.projectAddModal.projectTypeTip.general.scenarios')],
  TESTING: [t('project.projectAddModal.projectTypeTip.testing.features'), t('project.projectAddModal.projectTypeTip.testing.scenarios')]
};

const loading = ref(false);
const formData = ref({
  name: '',
  ownerId: undefined,
  description: '',
  avatar: '',
  dateRange: [],
  importExample: true
});

const members = ref({
  USER: [],
  DEPT: [],
  GROUP: []
});

const selectProjectType = (value) => {
  projectType.value = value;
};

const uploadImg = async ({ file }) => {
  const [error, { data = [] }] = await upload(file, {
    bizKey: 'angusTesterProjectAvatar'
  });
  if (error) {
    return;
  }
  if (data.length > 0) {
    formData.value.avatar = data[0].url;
  }
};

const delImg = () => {
  formData.value.avatar = '';
};

const cancel = () => {
  emit('update:visible', false);
  emit('cancel', false);
};

const defaultOptionsUser = ref<{ [key: string]: any }>({});
const defaultOptionsDept = ref<{ [key: string]: any }>({});
const defaultOptionsGroup = ref<{ [key: string]: any }>({});

const setDefaultData = () => {
  const _dataSource = props.dataSource;
  if (!_dataSource) {
    return;
  }

  const { id, name, ownerId, description = '', avatar = '', startDate, deadlineDate, importExample = false } = _dataSource;
  formData.value = {
    id,
    name,
    ownerId,
    description,
    avatar,
    dateRange: [startDate, deadlineDate],
    importExample
  };

  if (_dataSource.members?.USER) {
    members.value.USER = _dataSource.members?.USER.map(i => {
      defaultOptionsUser.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }

  if (_dataSource.members?.DEPT) {
    members.value.DEPT = _dataSource.members?.DEPT.map(i => {
      defaultOptionsDept.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }

  if (_dataSource.members?.GROUP) {
    members.value.GROUP = _dataSource.members?.GROUP.map(i => {
      defaultOptionsGroup.value[i.id] = { ...i, fullName: i.name };
      return i.id;
    });
  }
};

const descRichRef = ref();
const validateDesc = () => {
  if (descRichRef.value && descRichRef.value.getLength() > 2000) {
    return Promise.reject(t('project.projectAddModal.validation.maxCharacters'));
  }
  // if (formData.value?.description?.length && formData.value.description.length > 2000) {
  //   return Promise.reject('不能超过2000字符');
  // }
  return Promise.resolve();
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const { USER, DEPT, GROUP } = members.value;
    const { dateRange, ...otherProject } = formData.value;
    const [error] = !formData.value.id
      ? await project.addProject({ ...otherProject, type: projectType.value, startDate: dateRange[0], deadlineDate: dateRange[1], memberTypeIds: { USER: USER.length ? USER : undefined, DEPT: DEPT.length ? DEPT : undefined, GROUP: GROUP.length ? GROUP : undefined } })
      : await project.putProject({ ...otherProject, type: projectType.value, startDate: dateRange[0], deadlineDate: dateRange[1], memberTypeIds: { USER: USER.length ? USER : undefined, DEPT: DEPT.length ? DEPT : undefined, GROUP: GROUP.length ? GROUP : undefined } });
    loading.value = false;
    if (error) {
      return;
    }
    if (formData.value.id) {
      notification.success(t('project.projectAddModal.messages.updateSuccess'));
    } else {
      notification.success(t('project.projectAddModal.messages.addSuccess'));
    }

    emit('update:visible', false);
    emit('ok');
  });
};

watch(() => props.visible, newValue => {
  if (!newValue) {
    return;
  }
  if (props.dataSource) {
    setDefaultData();
  } else {
    formData.value = {
      name: '',
      ownerId: undefined,
      description: '',
      avatar: '',
      dateRange: [],
      importExample: true
    };
    members.value = {
      USER: [userInfo.value.id],
      DEPT: [],
      GROUP: []
    };
    defaultOptionsUser.value = {
      [userInfo.value.id]: {
        fullName: userInfo.value.fullName,
        id: userInfo.value.id,
        name: userInfo.value.fullName,
        disabled: true
      }
    };
    defaultOptionsDept.value = {};
    defaultOptionsGroup.value = {};
  }
}, {
  immediate: true
});

const modalTitle = computed(() => {
  return props.dataSource?.id ? t('project.projectAddModal.modal.editProject') : t('project.projectAddModal.modal.addProject');
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="modalTitle"
    :closable="props.closable"
    :cancelButtonProps="{
      class: {'hidden': !props.closable}
    }"
    :okButtonProps="{
      loading: loading
    }"
    :width="1000"
    @ok="ok"
    @cancel="cancel">
    <div class="flex">
      <div class="flex-1 px-7 border-r">
        <div class="flex space-x-3">
          <div
            class="flex-1 flex flex-col items-center space-y-3 bg-gray-light px-2 rounded py-4 border "
            :class="{'border-blue-1 relative': projectType === 'AGILE', 'border-transparent': projectType !== 'AGILE'}"
            @click="selectProjectType('AGILE')">
            <Icon
              v-show="projectType === 'AGILE'"
              icon="icon-xuanzhongduigou"
              class="text-theme-special text-5 right-0 bottom-0 absolute " />
            <Icon icon="icon-minjiexiangmuguanli" class="text-10" />
            <div>{{ t('project.projectAddModal.projectTypeName.agile') }}</div>
          </div>

          <div
            class="flex-1 flex flex-col items-center space-y-3 bg-gray-light px-2 rounded py-4 border "
            :class="{' border-blue-1 relative': projectType === 'GENERAL', 'border-transparent': projectType !== 'GENERAL'}"
            @click="selectProjectType('GENERAL')">
            <Icon
              v-show="projectType === 'GENERAL'"
              icon="icon-xuanzhongduigou"
              class="text-theme-special text-5 right-0 bottom-0 absolute " />
            <Icon icon="icon-jiandanxiangmuguanli" class="text-10" />
            <div>{{ t('project.projectAddModal.projectTypeName.general') }}</div>
          </div>

          <div
            class="flex-1 flex flex-col items-center space-y-3 bg-gray-light px-2 rounded py-4 border"
            :class="{' border-blue-1 relative': projectType === 'TESTING', 'border-transparent': projectType !== 'TESTING'}"
            @click="selectProjectType('TESTING')">
            <Icon
              v-show="projectType === 'TESTING'"
              icon="icon-xuanzhongduigou"
              class="text-theme-special text-5 right-0 bottom-0 absolute " />
            <Icon icon="icon-ceshixiangmuguanli" class="text-10" />
            <div>{{ t('project.projectAddModal.projectTypeName.testing') }}</div>
          </div>
        </div>

        <div class="py-3 mt-5 border rounded min-h-40 flex flex-col justify-center items-center">
          <img
            v-show="projectType==='AGILE'"
            src="./agile.png"
            class="inline-block"
            style="width: 70%;" />
          <img
            v-show="projectType==='GENERAL'"
            src="./general.png"
            class="w-4/5 inline-block h-50" />
          <img
            v-show="projectType==='TESTING'"
            src="./testing.png"
            class="inline-block"
            style="width: 72%;" />
        </div>
        <div class="space-y-2 mt-7">
          <div v-for="item in projectTypeTipConfig[projectType]" class="flex space-x-1">
            <Icon icon="icon-duihao-copy" class="text-3.5 mt-1" />
            <p class="text-3.5 text-text-sub-content">{{ item }}</p>
          </div>
        </div>
      </div>
      <Divider type="vertical" class="mr-7 ml-0" />
      <Form
        ref="formRef"
        class="flex-1"
        :model="formData"
        layout="vertical">
        <FormItem>
          <div v-if="formData.avatar" class="flex space-x-2 justify-center">
            <Image
              :src="formData.avatar"
              class="w-25 h-25"
              alt="avatar" />
            <Icon
              icon="icon-qingchu"
              class="mt-2 cursor-pointer text-4"
              @click="delImg" />
          </div>
          <div v-else>
            <Upload
              name="avatar"
              accept=".png,.jpeg,.jpg"
              listType="picture-card"
              :showUploadList="false"
              :customRequest="uploadImg">
              <div class="ant-upload-text bg-blue-2 h-full w-full flex flex-col items-center spacey-2 justify-around">
                <img src="../project/images/default.png" class="w-15" />
                <div class="text-3">{{ t('project.projectAddModal.form.clickToReplaceIcon') }}</div>
              </div>
            </Upload>
          </div>
        </FormItem>
        <FormItem
                      :label="t('project.projectAddModal.form.name')"
          name="name"
          required>
          <Input
            v-model:value="formData.name"
            :placeholder="t('project.projectAddModal.form.projectNamePlaceholder')"
            :maxlength="100">
          </Input>
        </FormItem>

        <FormItem
                      :label="t('project.projectAddModal.form.timePlan')"
          name="dateRange"
                      :rules="[{ required: true, message: t('project.projectAddModal.validation.timeRequired')}]">
          <template #label>
            <div class="flex items-center">
                              <span>{{ t('project.projectAddModal.form.time') }}</span>
              <Popover placement="right">
                <template #content>
                  <div class="max-w-100">
                    {{ t('project.projectAddModal.form.timeDescription') }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
              </Popover>
            </div>
          </template>
          <DatePicker
            v-model:value="formData.dateRange"
            :allowClear="false"
            class="w-full"
            type="date-range">
          </DatePicker>
        </FormItem>

        <FormItem
          name="ownerId"
                      :rules="[{ required: true, message: t('project.projectAddModal.validation.ownerRequired')}]">
          <template #label>
            <div class="flex items-center">
                              <span>{{ t('project.projectAddModal.form.owner') }}</span>
              <Popover placement="right">
                <template #content>
                  <div class="max-w-100">
                    {{ t('project.projectAddModal.form.ownerDescription') }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
              </Popover>
            </div>
          </template>
          <SelectUser
            v-model:value="formData.ownerId"
            size="small"
                          :placeholder="t('project.projectAddModal.form.ownerPlaceholder')"
            :allowClear="false" />
        </FormItem>

        <FormItem>
          <template #label>
            <div class="flex items-center">
              <span>{{ t('project.projectAddModal.form.importExample') }}</span>
              <Popover placement="right">
                <template #content>
                  <div class="max-w-100">
                    {{ t('project.projectAddModal.form.importExampleDescription') }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
              </Popover>
            </div>
          </template>
          <RadioGroup
            v-model:value="formData.importExample"
            :options="[{ value: true, label: t('project.projectAddModal.form.yes')}, { value: false, label: t('project.projectAddModal.form.no') }]">
          </RadioGroup>
        </FormItem>
        <FormItem :label="t('project.projectAddModal.form.members')" required>
          <RadioGroup
            v-model:value="memberType"
            buttonStyle="solid"
            size="small">
            <RadioButton value="user">
              {{ t('project.projectAddModal.form.user') }}
            </RadioButton>
            <RadioButton value="dept">
              {{ t('project.projectAddModal.form.department') }}
            </RadioButton>
            <RadioButton value="group">
              {{ t('project.projectAddModal.form.group') }}
            </RadioButton>
          </RadioGroup>
          <Popover placement="right">
            <template #content>
              <div class="max-w-110">
                {{ t('project.projectAddModal.form.membersDescription') }}
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips text-3.5 ml-2" />
          </Popover>
          <div class="mt-3.5">
            <Select
              v-show="memberType === 'user'"
              v-model:value="members.USER"
              :showSearch="true"
              :placeholder="t('project.projectAddModal.form.selectUser')"
              :action="`${GM}/user?fullTextSearch=true`"
              :defaultOptions="defaultOptionsUser"
              mode="multiple"
              :fieldNames="{ label: 'fullName', value: 'id' }">
            </Select>

            <Select
              v-show="memberType === 'dept'"
              v-model:value="members.DEPT"
              :placeholder="t('project.projectAddModal.form.selectDepartment')"
              :showSearch="true"
              :action="`${GM}/dept?fullTextSearch=true`"
              :defaultOptions="defaultOptionsDept"
              mode="multiple"
              :fieldNames="{ label: 'name', value: 'id' }">
            </Select>

            <Select
              v-show="memberType === 'group'"
              v-model:value="members.GROUP"
              :placeholder="t('project.projectAddModal.form.selectGroup')"
              :showSearch="true"
              :action="`${GM}/group?fullTextSearch=true`"
              :defaultOptions="defaultOptionsGroup"
              mode="multiple"
              :fieldNames="{ label: 'name', value: 'id' }">
            </Select>
          </div>
        </FormItem>

        <FormItem
          :label="t('project.projectAddModal.form.description')"
          name="description"
          :rules="[{validator: validateDesc}]">
          <RichEditor
            ref="descRichRef"
            v-model:value="formData.description"
            class="border rounded border-border-input"
            :height="80"
            :options="{placeholder: t('project.projectAddModal.form.descriptionPlaceholder'), theme: 'bubble'}" />
          <!-- <Textarea
            v-model:value="formData.description"
            type="textarea"
            :autosize="{
              minRows: 5
            }"
            showCount
            placeholder="描述，最多2000个字符"
            :maxlength="2000" /> -->
        </FormItem>
      </Form>
    </div>
  </Modal>
</template>
