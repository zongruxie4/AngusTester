#!/usr/bin/env node

/**
 * 国际化资源配置迁移脚本
 * 将JavaScript格式的国际化文件转换为JSON格式
 */

const fs = require('fs');
const path = require('path');

// 定义新的文件结构
const newStructure = {
  'common.json': {
    time: ['year', 'month', 'week', 'day', 'hour', 'minute', 'second'],
    status: ['success', 'failure', 'pending', 'processing', 'completed', 'cancelled'],
    actions: ['add', 'edit', 'delete', 'save', 'cancel', 'confirm']
  },
  'api.json': {
    welcome: ['title', 'description'],
    navigation: ['myApis', 'projects', 'services', 'apis', 'scenarios'],
    actions: ['cancelAllFavorites', 'cancelAllFollows'],
    search: ['taskName', 'scenario'],
    messages: ['cancelAllFavoritesSuccess', 'cancelAllFollowsSuccess']
  },
  'task.json': {
    overview: ['myTasks', 'totalTasks', 'oneTimePassRate'],
    status: ['pending', 'processing', 'completed', 'cancelled'],
    form: ['task', 'type', 'test', 'scenario', 'api', 'time', 'tags', 'description']
  },
  'execution.json': {
    basic: ['execute', 'name', 'type', 'scenario', 'configure', 'status'],
    actions: ['createExecution', 'terminateExecution', 'retry', 'viewReport'],
    search: ['executionName', 'inputName', 'selectScript', 'selectProject']
  },
  'scenario.json': {
    welcome: ['title', 'description'],
    navigation: ['directory', 'scenario', 'execution'],
    scriptInfo: ['scenarioName', 'directory', 'creator', 'createdTime']
  },
  'settings.json': {
    agent: ['title', 'noProxy', 'clientProxy', 'serverProxy'],
    notification: ['status', 'config', 'columns', 'placeholder']
  }
};

/**
 * 转换旧的JS文件到新的JSON格式
 */
function migrateFile(oldFilePath, newFilePath, structure) {
  try {
    // 读取旧的JS文件
    const oldContent = fs.readFileSync(oldFilePath, 'utf8');
    
    // 简单的JS对象解析（这里需要根据实际文件内容调整）
    const oldData = eval('(' + oldContent.replace('export default', '') + ')');
    
    // 创建新的JSON结构
    const newData = {};
    
    // 根据结构定义转换数据
    Object.keys(structure).forEach(category => {
      newData[category] = {};
      structure[category].forEach(key => {
        if (oldData[key]) {
          newData[category][key] = oldData[key];
        }
      });
    });
    
    // 写入新的JSON文件
    fs.writeFileSync(newFilePath, JSON.stringify(newData, null, 2), 'utf8');
    
    console.log(`✅ 成功迁移: ${oldFilePath} -> ${newFilePath}`);
  } catch (error) {
    console.error(`❌ 迁移失败: ${oldFilePath}`, error.message);
  }
}

/**
 * 主迁移函数
 */
function migrate() {
  const localesDir = path.join(__dirname);
  const languages = ['en', 'zh_CN'];
  
  languages.forEach(lang => {
    const langDir = path.join(localesDir, lang);
    
    if (!fs.existsSync(langDir)) {
      console.log(`⚠️  语言目录不存在: ${langDir}`);
      return;
    }
    
    console.log(`\n🔄 开始迁移 ${lang} 语言文件...`);
    
    Object.keys(newStructure).forEach(newFileName => {
      const oldFileName = newFileName.replace('.json', '.js');
      const oldFilePath = path.join(langDir, oldFileName);
      const newFilePath = path.join(langDir, newFileName);
      
      if (fs.existsSync(oldFilePath)) {
        migrateFile(oldFilePath, newFilePath, newStructure[newFileName]);
      } else {
        console.log(`⚠️  旧文件不存在: ${oldFilePath}`);
      }
    });
  });
  
  console.log('\n🎉 迁移完成！');
  console.log('\n📝 下一步操作:');
  console.log('1. 检查生成的JSON文件内容');
  console.log('2. 更新代码中的国际化引用');
  console.log('3. 测试所有页面的国际化功能');
  console.log('4. 删除旧的JS文件');
}

// 运行迁移
if (require.main === module) {
  migrate();
}

module.exports = { migrate, newStructure }; 