# 国际化资源配置优化指南

## 概述

本次优化将国际化资源配置从JavaScript对象格式改为JSON格式，并重新分类和优化Message key定义。

## 主要改进

### 1. 格式转换
- **从**: JavaScript对象格式 (`export default { t1: 'value', t2: 'value' }`)
- **到**: JSON格式 (`{ "key": "value" }`)

### 2. 分类优化
- **公共消息** (`common.json`): 时间、状态、操作、表单、表格等通用消息
- **业务消息**: 按功能模块分类
  - API管理 (`api.json`)
  - 任务管理 (`task.json`)
  - 执行管理 (`execution.json`)
  - 场景管理 (`scenario.json`)
  - 设置管理 (`settings.json`)

### 3. Key命名优化
- **从**: `t1`, `t2`, `p1`, `p2` 等不明确的key
- **到**: 语义化的key，如 `welcome.title`, `actions.add`, `status.success`

## 新的文件结构

```
web/src/locales/
├── en/
│   ├── common.json      # 公共消息
│   ├── api.json         # API管理
│   ├── task.json        # 任务管理
│   ├── execution.json   # 执行管理
│   ├── scenario.json    # 场景管理
│   └── settings.json    # 设置管理
└── zh_CN/
    ├── common.json      # 公共消息
    ├── api.json         # API管理
    ├── task.json        # 任务管理
    ├── execution.json   # 执行管理
    ├── scenario.json    # 场景管理
    └── settings.json    # 设置管理
```

## Key命名规范

### 1. 公共消息 (common.json)
```json
{
  "time": {
    "year": "年",
    "month": "月"
  },
  "status": {
    "success": "成功",
    "failure": "失败"
  },
  "actions": {
    "add": "添加",
    "edit": "编辑"
  }
}
```

### 2. 业务消息
```json
{
  "welcome": {
    "title": "欢迎进入Angus接口！",
    "description": "在这里和的团队可以完成以下工作："
  },
  "navigation": {
    "myApis": "我的接口",
    "projects": "项目"
  }
}
```

## 迁移步骤

### 1. 创建新的JSON文件
- 按照上述结构创建新的JSON文件
- 将现有的JS文件内容转换为JSON格式

### 2. 更新引用
- 更新所有引用国际化资源的代码
- 从 `$t('apis.text.t1')` 改为 `$t('api.welcome.title')`

### 3. 删除旧文件
- 删除旧的JS文件
- 更新相关的导入语句

## 使用示例

### 旧格式
```javascript
// apis.js
export default {
  text: {
    t1: '欢迎进入Angus接口！',
    t2: '在这里和的团队可以完成以下工作：'
  }
}

// 使用
$t('apis.text.t1')
```

### 新格式
```json
// api.json
{
  "welcome": {
    "title": "欢迎进入Angus接口！",
    "description": "在这里和的团队可以完成以下工作："
  }
}

// 使用
$t('api.welcome.title')
```

## 优势

1. **可读性**: JSON格式更易读，key命名更语义化
2. **维护性**: 分类清晰，便于维护和扩展
3. **一致性**: 统一的命名规范和文件结构
4. **工具支持**: JSON格式有更好的工具支持（语法高亮、验证等）
5. **性能**: JSON解析性能更好

## 注意事项

1. 确保所有引用都已更新
2. 测试所有页面的国际化功能
3. 保持中英文文件结构一致
4. 备份原有文件以防回滚需要 