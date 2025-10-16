### 数组示例
> Note that arrays and array items support single example but not multiple examples.      
- You can add an example of an individual array item:
```
components:
  schemas:
    ArrayOfInt:
      type: array
      items:
        type: integer
        format: int64
        example: 1
```
- or an array-level example containing multiple items:
```
components:
  schemas:
    ArrayOfInt:
      type: array
      items:
        type: integer
        format: int64
      example: [1, 2, 3]
```
- If the array contains objects, you can specify a multi-item example as follows:
```
components:
  schemas:
    ArrayOfUsers:
      type: array
      items:
        type: object
        properties:
          id:
            type: integer
          name:
            type: string
      example:
        - id: 10
          name: Jessica Smith
        - id: 20
          name: Ron Stewart
```

1. 和schema平级的examples是对象格式
2. 在属性中的exampls是数组格式
3. examples的优先级大于example