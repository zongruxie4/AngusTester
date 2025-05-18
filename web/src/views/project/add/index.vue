<script lang="ts" setup>
import { computed, inject, ref, watch, defineAsyncComponent } from 'vue';
import { DatePicker, Icon, Image, Input, Modal, notification, Select, SelectUser } from '@xcan-angus/vue-ui';
import { Divider, Form, FormItem, RadioButton, RadioGroup, Upload, Popover } from 'ant-design-vue';
import { project } from 'src/api/tester';
import { GM, upload } from '@xcan-angus/tools';

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
const tenantInfo = inject('tenantInfo', ref());

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
  AGILE: ['支持Scrum敏捷开发与敏捷测试流程，旨在通过迭 代和增量的方式开发和测试软件，‌这种方法可以有 效加速产品交付，同时确保产品开发方向始终和用 户的需求和期望保持一致。', '适用于软件项目和具有敏捷经验的团队。'],
  GENERAL: ['支持通用化研发和测试流程，使用WBS将项目递归 分解为较小、更易管理的组件的层次结构，然后基 于甘特图管理任务进度、依赖关系和时间线。', '适用于有软件项目和所有开发测试团队。'],
  TESTING: ['支持通用测试流程，包括指定测试计划，开展功能 测试、接口测试、场景测试等活动；', '适用于所有软件测试团队。']
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
    return Promise.reject('不能超过2000字符');
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
      : await project.replaceProject({ ...otherProject, type: projectType.value, startDate: dateRange[0], deadlineDate: dateRange[1], memberTypeIds: { USER: USER.length ? USER : undefined, DEPT: DEPT.length ? DEPT : undefined, GROUP: GROUP.length ? GROUP : undefined } });
    loading.value = false;
    if (error) {
      return;
    }
    if (formData.value.id) {
      notification.success('修改项目成功');
    } else {
      notification.success('添加项目成功');
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
      USER: [tenantInfo.value.id],
      DEPT: [],
      GROUP: []
    };
    defaultOptionsUser.value = {
      [tenantInfo.value.id]: {
        fullName: tenantInfo.value.fullName,
        id: tenantInfo.value.id,
        name: tenantInfo.value.fullName,
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
  return props.dataSource?.id ? '编辑项目' : '添加项目';
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
            <div>敏捷项目管理</div>
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
            <div>一般项目管理</div>
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
            <div>测试项目管理</div>
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
                <div class="text-3">点击替换图标</div>
              </div>
            </Upload>
          </div>
        </FormItem>
        <FormItem
          label="名称"
          name="name"
          required>
          <Input
            v-model:value="formData.name"
            placeholder="项目名称，最多100个字符"
            :maxlength="100">
          </Input>
        </FormItem>
        <FormItem
          name="ownerId"
          :rules="[{ required: true, message: '请输入负责人'}]">
          <template #label>
            <div class="flex items-center">
              <span>负责人</span>
              <Popover placement="right">
                <template #content>
                  <div class="max-w-100">
                    通过为项目设置负责人，可以明确责任和权利，更好地促进项目的完成和进度控制，如：解决问题、推进进度、协作团队成员等。
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
              </Popover>
            </div>
          </template>
          <SelectUser
            v-model:value="formData.ownerId"
            size="small"
            placeholder="请选择项目负责人"
            :allowClear="false" />
        </FormItem>
        <FormItem
          label="时间"
          name="dateRange"
          :rules="[{ required: true, message: '请输入时间'}]">
          <template #label>
            <div class="flex items-center">
              <span>时间</span>
              <Popover placement="right">
                <template #content>
                  <div class="max-w-100">
                    时间限制有助于确保项目在约定的时间内完成，这对于满足客户期望和合同义务至关重要。
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

        <FormItem>
          <template #label>
            <div class="flex items-center">
              <span>导入示例</span>
              <Popover placement="right">
                <template #content>
                  <div class="max-w-100">
                    导入主要业务示例数据，用于快速熟悉AngusTester业务与功能演示。
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
              </Popover>
            </div>
          </template>
          <RadioGroup
            v-model:value="formData.importExample"
            :options="[{ value: true, label: '是'}, { value: false, label: '否' }]">
          </RadioGroup>
        </FormItem>
        <FormItem label="成员" required>
          <RadioGroup
            v-model:value="memberType"
            buttonStyle="solid"
            size="small">
            <RadioButton value="user">
              用户
            </RadioButton>
            <RadioButton value="dept">
              部门
            </RadioButton>
            <RadioButton value="group">
              组
            </RadioButton>
          </RadioGroup>
          <Popover placement="right">
            <template #content>
              <div class="max-w-110">
                明确项目的参与人员，项目成员会自动分配项目下数据权限，方便团队协作。
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips text-3.5 ml-2" />
          </Popover>
          <div class="mt-3.5">
            <Select
              v-show="memberType === 'user'"
              v-model:value="members.USER"
              :showSearch="true"
              placeholder="选择用户"
              :action="`${GM}/user/search`"
              :defaultOptions="defaultOptionsUser"
              mode="multiple"
              :fieldNames="{ label: 'fullName', value: 'id' }">
            </Select>

            <Select
              v-show="memberType === 'dept'"
              v-model:value="members.DEPT"
              placeholder="选择部门"
              :showSearch="true"
              :action="`${GM}/dept/search`"
              :defaultOptions="defaultOptionsDept"
              mode="multiple"
              :fieldNames="{ label: 'name', value: 'id' }">
            </Select>

            <Select
              v-show="memberType === 'group'"
              v-model:value="members.GROUP"
              placeholder="选择组"
              :showSearch="true"
              :action="`${GM}/group/search`"
              :defaultOptions="defaultOptionsGroup"
              mode="multiple"
              :fieldNames="{ label: 'name', value: 'id' }">
            </Select>
          </div>
        </FormItem>

        <FormItem
          label="描述"
          name="description"
          :rules="[{validator: validateDesc}]">
          <RichEditor
            ref="descRichRef"
            v-model:value="formData.description"
            class="border rounded border-border-input"
            :height="80"
            :options="{placeholder: '描述，最多2000个字符', theme: 'bubble'}" />
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
