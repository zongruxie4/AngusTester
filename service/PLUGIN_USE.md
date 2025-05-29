## Plugin Path

- Set through WM Options parameter

```properties
-Dplugin.dir=/Volumes/workspace/workspace_angus/seek/AngusTester/service/extension/dist
```

- Default plugin path reading

In the application's Home directory: ./plugins

- Jenkins build and plugin path setting

```bash
extension_dist="./extension/dist"
...
mkdir $target/plugins && cp -r $extension_dist/*  $target/plugins/
```

