<script lang="ts" setup>
import { computed, inject, ref, watch } from 'vue';
import { IconRequired, Input, Modal, notification } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';
import { dataSource } from 'src/api/tester';
import SelectEnum from '@/components/SelectEnum/index.vue'
interface Props {
  visible: boolean;
  editData?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  editData: undefined
});

const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo.value?.id;
});
const loading = ref(false);

const formState = ref({
  name: '',
  database: undefined,
  driverClassName: '',
  jdbcUrl: '',
  password: '',
  username: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean), (e: 'refresh')}>();

const handleClose = () => {
  emits('update:visible', false);
};

const formRef = ref();

const handleOk = async () => {
  if (!formRef.value) {
    return;
  }
  formRef.value.validate().then(() => {
    if (props.editData) {
      edit();
    } else {
      add();
    }
  });
};

const edit = async () => {
  if (loading.value) {
    return;
  }

  const params:Record<string, any> = {
    id: props.editData?.id,
    name: formState.value.name,
    database: formState.value.database,
    driverClassName: formState.value.driverClassName,
    jdbcUrl: formState.value.jdbcUrl,
    password: formState.value.password,
    projectId: projectId.value,
    username: formState.value.username
  };

  loading.value = true;
  const [error] = await dataSource.putDataSource(params);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('修改成功');
  emits('update:visible', false);
  emits('refresh');
};

const add = async () => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error] = await dataSource.addDataSource({ ...formState.value, projectId: projectId.value });
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('添加成功');
  emits('update:visible', false);
  emits('refresh');
};

watch(() => props.visible, (newValue) => {
  if (!newValue) {
    return;
  }
  if (props.editData) {
    const _data = props.editData;
    formState.value = {
      name: _data.name,
      database: _data.database,
      driverClassName: _data.driverClassName,
      jdbcUrl: _data.jdbcUrl,
      username: _data.username,
      password: _data.password
    };
    return;
  }

  formState.value = {
    name: '',
    database: undefined,
    driverClassName: '',
    jdbcUrl: '',
    password: '',
    username: ''
  };
}, {
  immediate: true
});

// 切换类型设置默认值
const databaseChange = (value:string) => {
  switch (value) {
    case 'H2':
      formState.value.driverClassName = 'org.h2.Driver';
      formState.value.jdbcUrl = 'jdbc:h2:~/sample_db';
      break;
    case 'HSQLDB':
      formState.value.driverClassName = 'org.hsqldb.jdbc.JDBCDriver';
      formState.value.jdbcUrl = 'jdbc:hsqldb:mem:sample_db';
      break;
    case 'SQLITE':
      formState.value.driverClassName = 'org.sqlite.JDBC';
      formState.value.jdbcUrl = 'jdbc:sqlite:sample_db';
      break;
    case 'POSTGRES':
      formState.value.driverClassName = 'org.postgresql.Driver';
      formState.value.jdbcUrl = 'jdbc:postgresql://127.0.0.1:5432/sample_db';
      break;
    case 'MARIADB':
      formState.value.driverClassName = 'org.mariadb.jdbc.Driver';
      formState.value.jdbcUrl = 'jdbc:mariadb://127.0.0.1:3306/sample_db';
      break;
    case 'MYSQL':
      formState.value.driverClassName = 'com.mysql.cj.jdbc.Driver';
      formState.value.jdbcUrl = 'jdbc:mysql://127.0.0.1:3306/sample_db';
      break;
    case 'ORACLE':
      formState.value.driverClassName = 'oracle.jdbc.driver.OracleDriver';
      formState.value.jdbcUrl = 'jdbc:oracle:thin://127.0.0.1:1521:sample_db';
      break;
    case 'SQLSERVER':
      formState.value.driverClassName = 'com.microsoft.sqlserver.jdbc.SQLServerDriver';
      formState.value.jdbcUrl = 'jdbc:sqlserver://127.0.0.1:1433;databaseName=sample_db';
      break;
    case 'DB2':
      formState.value.driverClassName = 'com.ibm.db2.jcc.DB2Driver';
      formState.value.jdbcUrl = 'jdbc:db2://127.0.0.1:50000/sample_db';
      break;
  }
  formRef.value.clearValidate();
};

</script>
<template>
  <Modal
    :title="props.editData?'修改数据源':'添加数据源'"
    :confirmLoading="loading"
    :visible="visible"
    @cancel="handleClose"
    @ok="handleOk">
    <div class="text-3 text-content flex">
      <div class="space-y-5 pt-1.5 mr-5">
        <div class="h-7"><IconRequired />类型</div>
        <div class="h-7"><IconRequired />名称</div>
        <div class="h-7 pl-1.75">驱动类名</div>
        <div class="h-7"><IconRequired />JBDC Url</div>
        <template v-if="formState.database !== 'SQLITE'">
          <div class="h-7 pl-1.75">用户名</div>
          <div class="h-7 pl-1.75">密码</div>
        </template>
      </div>
      <Form
        ref="formRef"
        class="flex-1"
        :model="formState">
        <FormItem name="database" :rules="{required:true,message:'请选择数据库类型'}">
          <SelectEnum
            v-model:value="formState.database"
            :disabled="!!props.editData"
            enumKey="DatabaseType"
            placeholder="数据库类型"
            @change="databaseChange" />
        </FormItem>
        <FormItem name="name" :rules="{required:true,message:'请输入名称'}">
          <Input
            v-model:value="formState.name"
            :maxlength="100"
            placeholder="名称" />
        </FormItem>
        <FormItem name="driverClassName">
          <Input v-model:value="formState.driverClassName" placeholder="驱动类名" />
        </FormItem>
        <FormItem name="jdbcUrl" :rules="{required:true,message:'请输入JDBC Url'}">
          <Input v-model:value="formState.jdbcUrl" placeholder="JBDC Url" />
        </FormItem>
        <template v-if="formState.database !== 'SQLITE'">
          <FormItem name="username">
            <Input
              v-model:value="formState.username"
              placeholder="用户名"
              :disabled="formState.database === 'SQLITE' && !!props.editData"
              :maxlength="50" />
          </FormItem>
          <FormItem name="password">
            <Input
              v-model:value="formState.password"
              placeholder="密码"
              :maxlength="50"
              type="password" />
          </FormItem>
        </template>
      </Form>
    </div>
  </Modal>
</template>
