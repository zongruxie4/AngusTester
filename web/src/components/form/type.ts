export interface Example{
  instance:string,
  values:string[]
}

export interface CallbackValue{
  description:string|undefined,
  examples:Example[]|undefined,
  name?:string
}

export interface Description{
  en:string,
  zhCn:string
}

export interface Parameter{
name:string,
descriptions:Description
}

export interface FunctionConfig {
  descriptions:Description,
  name:string,
  parameters:Parameter[]|string,
  examples?:Example[]
}
