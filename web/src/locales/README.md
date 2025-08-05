# 国际化资源配置优化

## 优化概述

本次优化将国际化资源配置从JavaScript对象格式改为JSON格式，并重新分类和优化Message key定义。

## 主要改进

### 1. 格式转换
- **从**: JavaScript对象格式
- **到**: JSON格式

### 2. 分类优化
- **公共消息** (`common.json`): 时间、状态、操作等通用消息
- **业务消息**: 按功能模块分类
  - API管理 (`api.json`)
  - 任务管理 (`task.json`)
  - 执行管理 (`execution.json`)
  - 场景管理 (`scenario.json`)
  - 设置管理 (`settings.json`)

### 3. Key命名优化
- **从**: `t1`, `t2`, `p1`, `p2` 等不明确的key
- **到**: 语义化的key，如 `welcome.title`, `actions.add`

## 新文件结构

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
    └── [相同的JSON文件]
```

## 使用示例

### 旧格式
```javascript
$t('apis.text.t1')  // 欢迎进入Angus接口！
```

### 新格式
```javascript
$t('api.welcome.title')  // 欢迎进入Angus接口！
```

## 优势

1. **可读性**: JSON格式更易读，key命名更语义化
2. **维护性**: 分类清晰，便于维护和扩展
3. **一致性**: 统一的命名规范和文件结构
4. **工具支持**: JSON格式有更好的工具支持
5. **性能**: JSON解析性能更好

## 迁移步骤

1. 创建新的JSON文件
2. 更新代码中的国际化引用
3. 测试所有页面的国际化功能
4. 删除旧的JS文件 