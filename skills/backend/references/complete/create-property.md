---
title: 添加属性
date: 2023-09-25 17:31:34
permalink: /pages/a0dcea/
---


# 注解

## 3.2.1@Property注解

| 参数              | 说明                                                         | 类型    | 默认值                               |
| ----------------- | ------------------------------------------------------------ | ------- | ------------------------------------ |
| name              | 属性名字，也是数据库字段名                                   | String  |                                      |
| displayName       | 用户看到的字段的标签,如果未设置，默认显示字段名              | String  |                                      |
| tooltips          | 用户看到的字段的帮助提示                                     | String  |                                      |
| isReadonly        | 该字段是否为只读。这只会对 UI 产生影响。代码中的任何字段设置值都将起作用 | boolean | false                                |
| isRequired        | 字段的值是否是必需的                                         | boolean | false                                |
| isStore           | 字段是否存储在数据库中，对计算字段不生效                     | boolean | true                                 |
| length            | 字段长度                                                     | int     | 字符串默认长度为240,小数默认长度为22 |
| scale             | 小数位精度                                                   | int     | 2                                    |
| dbIndex           | 该字段是否在数据库中被索引，对于非存储字段不生效             | boolean | false                                |
| display           | 是否显示                                                     | boolean | true                                 |
| ruleScript        | 规则脚本                                                     | String  |                                      |
| computeScript     | 计算字段，通过计算获取值，不保存于数据库。可以由元方法计算，也可以是groovy脚本计算 | String  |                                      |
| computeMethod     | 计算方法                                                     | String  |                                      |
| format            | 字符串格式化                                                 | String  |                                      |
| isDisplayForModel | 是否为模型的显示字段                                         | boolean | false                                |
| unique            | 是否唯一,如果是true，将纳入唯一性校验（与数据库无关）        | boolean | false                                |
| validationType    | 校验类型                                                     | String  |                                      |
| defaultValue      | 字段的默认值。可以是常量值，可是通过元方法提供值，也可以通过groovy脚本提供值 | String  |                                      |
| defaultMethod     | 默认值方法名                                                 | String  |                                      |
| widget            | 显示组件名称                                                 | String  |                                      |
| indexNo           | 排序字段                                                     | int     |                                      |
| percent           | 是否显示为百分比                                             | boolean |                                      |
| dateFormat        | 日期时间格式                                                 | String  |                                      |
| password          | 是否密码                                                     |         |                                      |
| groovy            | groovy脚本                                                   |         |                                      |
| multiple          | 是否多选                                                     | boolean | false                                |
| contentType       | 附件类型                                                     | String  | String                               |
| onChange          | 字段变化时,触发事件                                          |         |                                      |

###             **属性get set**

继承了BaseModel因此拥有基本的CURD能力，也获得HashMap对象，因此拥有Map对象的get set能力，通过CGLIB调用引擎的服务查询数据。

###### **get 方法写法**

get方法 columnName 定义的是下划线rolel_name，但是实际get是驼峰命名的roleName

```
@Property(columnName = "role_name", displayName = "角色名称")
private String roleName;// * 这里需要注意的是get对象为roleName
public String getRoleName(){
   return (String) get("roleName");
}
```



###### **set 方法写法**

set方法 columnName 定义的是下划线role_name ，但是实际set对象是以驼峰命名的roleName

```
public void setRoleName(String roleName){
    set("roleName",roleName);
}
```



######   **配置IDEA get set 代码模板**

getter模板

```
#if($field.modifierStatic)
static ##
#end
$field.type ##
#if($field.recordComponent)
    ${field.name}##
#else
#set($name = $StringUtil.capitalizeWithJavaBeanConvention($StringUtil.sanitizeJavaIdentifier($helper.getPropertyName($field, $project))))
#if ($field.boolean && $field.primitive)
  is##
#else
  get##
#end
${name}##
#end
() {
  #if($field.type == "java.util.Date")
    return getDate("$field.name");
  #elseif($field.type == "java.sql.Timestamp")
    return getTimestamp("$field.name");
  #elseif($field.type == "java.time.LocalDateTime")
    return getLocalDateTime("$field.name");
  #elseif($field.type == "java.sql.Time")
    return getTime("$field.name");
  #elseif($field.type == "java.lang.Integer")
    return getInt("$field.name");
  #elseif($field.type == "java.lang.Long")
    return getLong("$field.name");
  #elseif($field.type == "java.lang.Double")
    return getDouble("$field.name");
  #elseif($field.type == "java.lang.Float")
    return getFloat("$field.name");
  #elseif($field.type == "java.lang.Short")
    return getShort("$field.name");
  #elseif($field.type == "java.lang.Byte")
    return getByte("$field.name");
  #elseif($field.type == "java.lang.Boolean")
    return getBoolean("$field.name");
  #elseif($field.type == "java.lang.String")
    return getStr("$field.name");
  #elseif($field.type == "java.lang.Number")
    return getNumber("$field.name");
  #elseif($field.type == "java.math.BigDecimal")
    return getBigDecimal("$field.name");
  #elseif($field.type == "java.math.BigInteger")
    return getBigInteger("$field.name");
  #else
    return ($field.type)this.get("$field.name");
  #end
}
```

setter模板

```js
#set($paramName = $helper.getParamName($field, $project))
#if($field.modifierStatic)
static ##
#end
$classname set$StringUtil.capitalizeWithJavaBeanConvention($StringUtil.sanitizeJavaIdentifier($helper.getPropertyName($field, $project)))($field.type $paramName) {
#if ($field.name == $paramName)
    #if (!$field.modifierStatic)
    this.##
    #else
        $classname.##
    #end
#end
set("$field.name", $paramName);
return this;
}
```

###   数据类型

- BigDecimal
- Boolean 存储true/false 值
- Date日期,默认格式为yyyy-MM-dd
- DateTime时间日期,默认格式为yyyy-MM-dd HH:mm:ss
- Double双精度浮点型。精度可由位数和小数位数对来定义,默认精度为2
- Selection选择类型: 包括常量枚举; 下拉框单选,下拉框多选,下拉框联动, 参考 [3.2.6 Selection下拉选项](#3.2.6.     **Selection下拉选项-单选和多选**)

- File 文件类型,文件类型可由contentType指定

- Integer整形

- Long 长整形

- List 存储List对象,以json格式存储数据,读取的时候反序列化为List对象

- Map 存储Map对象,以json格式存储数据,读取的时候反序列化为Map对象

- Object 存储对象,以json格式存储数据,读取的时候反序列化为对象

- String 字符串

- Text 长文本:

  ```java
  1.对于MySQL数据库 :

  length >20000,dataType=MediumText;
  length >5500000,dataType=LongText

  @Property(columnName = "text", displayName = "text",dataType = DataType.TEXT,length = 60000)
  private String text;

  2.对于Oracle数据库
  length >4000,dataType=clob;
  ```


##   3.2.2@**Validate注解**

Validate 提供的校验方式为在类的属性上加入相应的注解来达到校验的目的。validator提供的用于校验的注解如下：

| 注解             | 说明                                                  |
| ---------------- | ----------------------------------------------------- |
| @NotBlank        | 不能为null                                            |
| @Pattern(regex=) | 被注释的元素必须符合指定的正则表达式                  |
| @Email           | 检查是否是一个有效的email地址                         |
| @Max             | 该字段的值只能小于或等于该值                          |
| @Min             | 该字段的值只能大于或等于该值                          |
| @Size(min=,max=) | 检查所属的字段的长度是否在min和max之间,只能用于字符串 |
| @Null            | 能为null                                              |
| @Unique | 唯一校验。默认只校验当前字段。可以设置 properties，指定多个字段联合唯一 |



如何定义分组校验

TestDataSource 数据源根据数据源类型分组

```java
@Model(name = "TestDataSource", description = "数据源")
public class TestDataSource extends BaseModel {

    @Property(displayName = "数据源类型", widget = "radio-group")
    @Selection(values = {@Option(label = "DB/SQL", value = "DB", groups = Db.class), @Option(label = "API", value = "API", groups = Api.class),
            @Option(label = "Excel", value = "Excel"), @Option(label = "IIOT", value = "IIOT", groups = Iiot.class)})
    private String type;


    interface Db {
    }

    interface Api {
    }

    interface Iiot {

    }
}
```

TestDataSourceApi 扩展模型增加了请求头、请求参数分组为Api.class

```java
@Model(name = "TestDataSource", parent = "TestDataSource")
public class TestDataSourceApi extends BaseModel {
    @Property(columnName = "header", displayName = "请求头")
    @Validate.NotBlank(groups = TestDataSource.Api.class)
    private String header;

    @Validate.NotBlank(groups = TestDataSource.Api.class)
    @Property(columnName = "parameters", displayName = "请求参数")
    private String parameters;
}
```



使用的方式有两种：

1.API方式，前端直接请求create,update接口，引擎会根据类型校验分组。

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"valuesList": [
				{
					"type": "API",
					"header": "aaaaaaaa",
					"parameters": "ssssssssssssss"
				}
			]
		},
		"app": "newSdkApp",
		"service": "create",
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "TestDataSource",
		"tag": "master"
	}
}
```

2.服务定义方式，根据自定义服务使用哪个分组校验。

```java
    @MethodService(description = "测试分组")
    public void testValidate(@Validate.Validated(value = TestDataSource.Api.class) TestDataSourceApi api) {


    }
```



##  3.2.3**ER关系注解**

####             @**OneToMany**

采用@OneToMany对er关系一对多进行描述

```
@OneToMany(mappedBy = "org",cascade = { CascadeType.PERSIST,CascadeType.MERGE, CascadeType.DELETE })
private List<TestUser> userList;
```

One2Many 一对多关系，不会写入表中。



| **字段名**    | **中文名** | **类型**    | **可选值** |
| ------------- | ---------- | ----------- | ---------- |
| targetEntity  |            | Class       |            |
| cascade       | 级联操作   | CascadeType |            |
| fetch         | 延迟加载   | FetchType   |            |
| mappedBy      | 关系维护   |             |            |
| orphanRemoval |            |             |            |



####              @**ManyToOne**

采用@ManyToOne对er关系多对一进行描述

```
1.同一个App内
@ManyToOne(displayName = "组织机构")
@JoinColumn(name = "org_id", referencedProperty = "id")
private TestOrg org;

2.跨app引用
@ManyToOne(displayName = "申请人", targetModel = "rbac.rbac_user",targetProperty = "id",filter = "[[\"name\",\"=\",\"admin\"]]" )
@JoinColumn(name = "start_user_id",referencedProperty = "id" )
private Map<String, Object> startUserId;
```

ManyToOne只用写关联表， 相对应的org_id字段会存表中，org不会存在表中。

| **字段名**     | **中文名**                                                   | **类型**     | 默认值                | 是否必填 |
| -------------- | ------------------------------------------------------------ | ------------ | --------------------- | -------- |
| displayName    | 显示名字                                                     | String       | 属性英文名字          | 是       |
| targetModel    | one方模型,以app.model命名,<br />跨App引用才需要配置          | Class/String | TestOrg对象的模型名字 | 否       |
| cascade        | 级联操作:<br />DEL_SET_NULL:设置关联字段为空<br />DELETE:被级联删除<br />DEL_NO_PERMIT_RELATIVE:<br />存在记录则不允许主表删除 | CascadeType  | DEL_SET_NULL          | 否       |
| targetProperty | one方关联属性名,跨App引用才需要配置                          | String       | id                    | 否       |
| filter         | 过滤条件,关联one方时带的默认过滤条件                         | Filter       |                       | 否       |
| fetch          | 延迟加载,暂时不支持                                          | FetchType    |                       | 否       |

@**JoinColumn**

| 字段名             | 中文名                                                       | 类型   | 默认值     | 是否必填 |
| ------------------ | ------------------------------------------------------------ | ------ | ---------- | -------- |
| name               | 数据库表列名,当前表会添加一列org_id                          | String |            | 是       |
| referencedProperty | 默认会和one方的主键id关联,<br />如果不是通过id关联需要指定one的属性名 | String | id         | 否       |
| length             | 列长度                                                       | int    | 主键的长度 | 否       |



####              @**ManyToMany**

采用@ManyToMany对er关系多对多进行描述

```
@ManyToMany
@JoinTable(name = "role_user", joinColumns = @JoinColumn(name = "role_id", nullable = false), inverseJoinColumns = @JoinColumn(name = "user_id", nullable = false))
private List<TestUser> userList;
```



ManyToMany

role与user是多对多的关系，这种情况关联字段不会存在表中，关联字段会存在创建的关系表中。

| **字段名**   | **中文名** | **类型**    | **可选值** |
| ------------ | ---------- | ----------- | ---------- |
| targetEntity |            | Class       |            |
| cascade      | 级联操作   | CascadeType |            |
| fetch        | 延迟加载   | FetchType   |            |
| mappedBy     | 关系维护   | String      |            |
|              |            |             |            |



##    3.2.4@**Selection注解**

**1.1 单选**

- **单选-常量枚举**

  ```java
  @Property(displayName = "1单选常量", defaultValue = "1",length = 256)
  @Selection(values = { @Option(label = "状态1", value = "1"), @Option(label = "禁用", value = "2"), @Option(label = "已删除", value = "3") })
  private String status;
  ```



- **单选-选项值来自方法调用**

  ```java
  @Property(displayName = "selectMetod")
  @Selection(method = "selectMetod")
  private String selectMetod;

  /**
   * value为回显时候的值
   */
  @MethodService(description = "selectMetod")
  public List<Options> selectMetod(Object value) {
      String json = "[{\"code\":\"110101\",\"value\":\"东城区\"},{\"code\":\"110102\",\"value\":\"西城区\"},{\"code\":\"110105\",\"value\":\"朝阳区\"},{\"code\":\"110106\",\"value\":\"丰台区\"},{\"code\":\"110107\",\"value\":\"石景山区\"},{\"code\":\"110108\",\"value\":\"海淀区\"},{\"code\":\"110109\",\"value\":\"门头沟区\"},{\"code\":\"110111\",\"value\":\"房山区\"},{\"code\":\"110112\",\"value\":\"通州区\"},{\"code\":\"110113\",\"value\":\"顺义区\"},{\"code\":\"110114\",\"value\":\"昌平区\"},{\"code\":\"110115\",\"value\":\"大兴区\"},{\"code\":\"110116\",\"value\":\"怀柔区\"},{\"code\":\"110117\",\"value\":\"平谷区\"},{\"code\":\"110118\",\"value\":\"密云区\"},{\"code\":\"110119\",\"value\":\"延庆区\"}]";
      List<Options> options = new ArrayList<Options>();
      List<Options> provinceList = new ArrayList<Options>();

      JSONArray proviceArray = JSONObject.parseArray(json);
      for (int i = 0; i < proviceArray.size(); i++) {
          JSONObject proviceObj = proviceArray.getJSONObject(i);
          provinceList.add(Options.of(proviceObj.getString("value"), proviceObj.getString("code")));
      }

      //1.第一步:处理回显
      String valueStr=Objects.toString(value, null);
      if (StringUtils.isNotBlank(valueStr)) {
          Optional<Options> optional = provinceList.stream().filter(p -> p.getValue().equals(valueStr)).findFirst();
          if (optional.isPresent()) {
              options.add(optional.get());
          }
          return options;
      }
      //2.第二步:返回下拉框所有值
      options.addAll(provinceList);
      return options;
  }
  ```



- **单选-选项来自任意一个模型**

  properties是显示字段, id是保存的value值

  ```java
  @Property(displayName = "4单选异步获取模型")
  @Selection(model = "TestOrg", properties = "name", orderBy = "name desc",filter = "[[\"login\",\"like\",\"admin%\"]]")
  private String selectModel;
  ```



**1.2 多选**

- **多选常量**

  ```java
  @Property(displayName = "5-多选常量")
  @Selection(values = { @Option(label = "启用", value = "1"), @Option(label = "禁用", value = "2"),@Option(label = "已删除", value = "3") }, multiple = true)
  private String selectStatus;
  ```



- **多选-选项值来自方法调用**

  ```java
  @Property(displayName = "7-多选异步获取方法")
  @Selection(method = "selectMultipleProvince", multiple = true)
  private String[] selectMultipleProvince;

  	/**
  	 * 多选选择省
  	 */
  	@MethodService(description = "selectMultipleProvince")
  	public List<Options> selectMultipleProvince(Object[] value) {

  		String json = "[{\"code\":\"110101\",\"value\":\"东城区\"},{\"code\":\"110102\",\"value\":\"西城区\"},{\"code\":\"110105\",\"value\":\"朝阳区\"},{\"code\":\"110106\",\"value\":\"丰台区\"},{\"code\":\"110107\",\"value\":\"石景山区\"},{\"code\":\"110108\",\"value\":\"海淀区\"},{\"code\":\"110109\",\"value\":\"门头沟区\"},{\"code\":\"110111\",\"value\":\"房山区\"},{\"code\":\"110112\",\"value\":\"通州区\"},{\"code\":\"110113\",\"value\":\"顺义区\"},{\"code\":\"110114\",\"value\":\"昌平区\"},{\"code\":\"110115\",\"value\":\"大兴区\"},{\"code\":\"110116\",\"value\":\"怀柔区\"},{\"code\":\"110117\",\"value\":\"平谷区\"},{\"code\":\"110118\",\"value\":\"密云区\"},{\"code\":\"110119\",\"value\":\"延庆区\"}]";
  		List<Options> options = new ArrayList<Options>();
  		List<Options> provinceList = new ArrayList<Options>();

  		JSONArray proviceArray = JSONObject.parseArray(json);
  		for (int i = 0; i < proviceArray.size(); i++) {
  			JSONObject proviceObj = proviceArray.getJSONObject(i);
  			provinceList.add(Options.of(proviceObj.getString("value"), proviceObj.getString("code")));
  		}

  		// 1.处理回显
  		if (value != null && value.length > 0) {
  			options = provinceList.stream().filter(p -> ArrayUtils.contains(value, p.getValue())).collect(Collectors.toList());
  			return options;
  		}
          //2.返回选项列表
  		options.addAll(provinceList);
  		return options;
  	}

  ```

- **多选-many2many多选**

  ```java
  @ManyToMany
  @JoinTable(name = "role_user", joinColumns = @JoinColumn(name = "user_id", nullable = false), inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false))
  @Selection(multiple = true,properties = "roleName")
  private List<TestRole> roleList;
  ```



- **多选-选择来自任意模型**

  ```java
  @Property(displayName = "8多选异步获取模型")
  @Selection(model = "TestOrg", properties = "name", multiple = true)
  private List<TestOrg> selectMultipleModel;
  ```



**1.3 多级联动**

1. 后端属性定义

```java
linkageFields:联动的字段
method:调用方法名

@Property(displayName = "9-联动-省", toolTips = "请选择")
@Selection(method = "selectProvince", linkageFields = {"city", "area" })
private String province;

@Property(displayName = "9-联动-市", toolTips = "请选择",linkageFields = { "area" })
@Selection(method = "selectCity")
private String city;

@Property(displayName = "9-联动-区", toolTips = "请选择")
@Selection(method = "selectArea")
private String area;

```

2. 后端定义联动的方法

   ```java
   String json = "[{\"code\":\"110101\",\"value\":\"东城区\"},{\"code\":\"110102\",\"value\":\"西城区\"},{\"code\":\"110105\",\"value\":\"朝阳区\"},{\"code\":\"110106\",\"value\":\"丰台区\"},{\"code\":\"110107\",\"value\":\"石景山区\"},{\"code\":\"110108\",\"value\":\"海淀区\"},{\"code\":\"110109\",\"value\":\"门头沟区\"},{\"code\":\"110111\",\"value\":\"房山区\"},{\"code\":\"110112\",\"value\":\"通州区\"},{\"code\":\"110113\",\"value\":\"顺义区\"},{\"code\":\"110114\",\"value\":\"昌平区\"},{\"code\":\"110115\",\"value\":\"大兴区\"},{\"code\":\"110116\",\"value\":\"怀柔区\"},{\"code\":\"110117\",\"value\":\"平谷区\"},{\"code\":\"110118\",\"value\":\"密云区\"},{\"code\":\"110119\",\"value\":\"延庆区\"}]";

   	@MethodService(description = "selectProvince")
   	public List<Options> selectProvince(Object value) {
   		List<Options> options = new ArrayList<Options>();
   		List<Options> provinceList = new ArrayList<Options>();

   		JSONArray proviceArray = JSONObject.parseArray(json);
   		for (int i = 0; i < proviceArray.size(); i++) {
   			JSONObject proviceObj = proviceArray.getJSONObject(i);
   			provinceList.add(Options.of(proviceObj.getString("value"), proviceObj.getString("code")));
   		}

   		String valueStr = Objects.toString(value, null);
   		//1.处理回显
   		if (StringUtils.isNotBlank(valueStr)) {
   			Optional<Options> optional = provinceList.stream().filter(p -> p.getValue().equals(valueStr)).findFirst();
   			if (optional.isPresent()) {
   				options.add(optional.get());
   			}

   			return options;
   		}
           //2.返回下拉列表
   		options.addAll(provinceList);
   		return options;
   	}


   	/**
   	 * 选择市
   	 */
   	@MethodService(description = "selectCity")
   	public List<Options> selectCity(String provinceId, String value) {
   		List<Options> options = new ArrayList<Options>();
   		List<Options> cityList = new ArrayList<Options>();

   		//1.处理回显
   		if (StringUtils.isNotBlank(value)) {
   			JSONArray proviceArray = JSONObject.parseArray(json);
   			for (int i = 0; i < proviceArray.size(); i++) {
   				JSONObject proviceObj = proviceArray.getJSONObject(i);
   				JSONArray children = proviceObj.getJSONArray("children");
   				for (int j = 0; j < children.size(); j++) {
   					JSONObject cityObj = children.getJSONObject(j);
   					String cityCode = cityObj.getString("code");
   					String cityName = cityObj.getString("value");
   					if (cityCode.equals(value)) {
   						cityList.add(Options.of(cityName, cityCode));
   					}

   				}
   			}

   			options.addAll(cityList);
   			return options;
   		}

   	//2.返回列表
   		JSONArray proviceArray = JSONObject.parseArray(json);
   		for (int i = 0; i < proviceArray.size(); i++) {
   			JSONObject proviceObj = proviceArray.getJSONObject(i);
   			String provinceCode = proviceObj.getString("code");
   			JSONArray children = proviceObj.getJSONArray("children");
   			if (!provinceCode.equals(provinceId)) {
   				continue;
   			}

   			for (int j = 0; j < children.size(); j++) {
   				JSONObject cityObj = children.getJSONObject(j);
   				String cityCode = cityObj.getString("code");
   				String cityName = cityObj.getString("value");
   				cityList.add(Options.of(cityName, cityCode));
   			}
   		}

   		options.addAll(cityList);
   		return options;

   	}


   	/**
   	 * 选择区
   	 */
   	@MethodService(description = "selectArea")
   	public List<Options> selectArea(String provinceId, String cityId, String value) {
   		List<Options> options = new ArrayList<Options>();
   		List<Options> areaList = new ArrayList<Options>();
   		JSONArray proviceArray = JSONObject.parseArray(json);

   		// 1.处理回显
   		if (StringUtils.isNotBlank(value)) {
   			for (int i = 0; i < proviceArray.size(); i++) {
   				JSONObject proviceObj = proviceArray.getJSONObject(i);
   				JSONArray provinceChildren = proviceObj.getJSONArray("children");
   				for (int j = 0; j < provinceChildren.size(); j++) {
   					JSONObject cityObj = provinceChildren.getJSONObject(j);
   					JSONArray cityChildren = cityObj.getJSONArray("children");
   					for (int k = 0; k < cityChildren.size(); k++) {
   						JSONObject areaObj = cityChildren.getJSONObject(k);
   						String areaCode = areaObj.getString("code");
   						String areaName = areaObj.getString("value");

   						// 处理回显
   						if (areaCode.equals(value)) {
   							areaList.add(Options.of(areaName, areaCode));
   						}

   					}

   				}
   			}

   			options.addAll(areaList);
   			return options;
   		}

   		//2.返回列表
   		for (int i = 0; i < proviceArray.size(); i++) {
   			JSONObject proviceObj = proviceArray.getJSONObject(i);
   			String provinceCode = proviceObj.getString("code");
   			JSONArray provinceChildren = proviceObj.getJSONArray("children");
   			if (!provinceCode.equals(provinceId)) {
   				continue;
   			}

   			for (int j = 0; j < provinceChildren.size(); j++) {
   				JSONObject cityObj = provinceChildren.getJSONObject(j);
   				JSONArray cityChildren = cityObj.getJSONArray("children");
   				String cityCode = cityObj.getString("code");
   				if (!cityCode.equals(cityId)) {
   					continue;
   				}

   				for (int k = 0; k < cityChildren.size(); k++) {
   					JSONObject areaObj = cityChildren.getJSONObject(k);
   					String areaCode = areaObj.getString("code");
   					String areaName = areaObj.getString("value");
   					areaList.add(Options.of(areaName, areaCode));
   				}

   			}
   		}

   		options.addAll(areaList);
   		return options;

   	}
   ```

    3. 后端定义视图



```json

{
	"body": {
		"columns": [
			{
				"label": "省",
				"name": "province",
				"widget": "select",
				"linkage": {
					"params": {},
					"children": [
						"city",
						"area"
					]
				}
			},
			{
				"label": "市",
				"name": "city",
				"linkage": {
					"params": {
						"provinceId": "${province}"
					},
					"children": [
						"area"
					]
				}
			},
			{
				"label": "区",
				"name": "area",
				"linkage": {
					"params": {
						"provinceId": "${province}",
						"cityId": "${city}"
					}
				}
			},
			//如果联动的是ManyToOne的lookup方法,那么查询参数应该放在filter里面
			{
				"label": "组织",
				"name": "orgId",
				"linkage": {
					"type": "filter",
					"params": [
						[
							"id",
							"=",
							"${id}"
						]
					]
				}
			}
		],
		"type": "form"
	},
	"mode": "primary",
	"model": "TestUser",
	"name": "TestUser-表单",
	"type": "form"
}
```



4.视图显示效果

![](images/微信截图_20230221140934.png)



###             Related**关联属性**

关联属性主要用于带出ManyToOne关联模型的字段,关联字段默认不存储, related最多支持查询3层,比如org.parent.name

```
@ManyToOne(displayName = "组织机构")
@JoinColumn(name = "org_id", referencedProperty = "id")
private TestOrg org;

//related最多支持3层,比如org.parent.name
@Property(displayName = "组织名称",related = "org.name")
private String orgName;
```



###             Compute**计算属性**

计算字段是指 一个字段的值，可以通过一个函数来动态计算出来

到目前为止，我们接触的字段都是存储在数据库中并直接从数据库检索。字段也可以通过计算获得。在这种情况下，字段的值不是直接检索自数据库，而是通过调用模型的方法来实时计算获得。要创建计算字段，需要设置它的computeMethod属性为方法名。

```
@Property(columnName = "method", computeMethod = "method", displayName = "计算方法")
private String method;

/**
 * 计算属性指定的方法
 * @param map 当前行
 * @return 返回计算后的值
 */
public String method(Map<String, Object> map) {
    return "computer";
}
```

计算脚本

```
@Property(columnName = "script", computeScript = " 3+100", displayName = "计算脚本")
private String script;
```



###             **自动字段**

自动字段由引擎进行全局维护，自动字段默认不显示,包括如下

- id是由雪花算法生成，然后转成16进制，不足13位字符串时以0左补齐

- create_user 创建人,ManyToOne类型

- create_date 创建时间

- update_user 最后一次更新人,ManyToOne类型

- update_date 最后一次更新时间

- tenant_id 租户id



**自动字段默认是不显示的,如果需要前端显示的话,需要添加以下设置**

```json
{
	"columns": [
		{
			"displayName": "创建人",
			"name": "create_user",
			"custom": true,
			"display": true
		},
		{
			"displayName": "创建时间",
			"name": "create_date",
			"custom": true,
			"display": true
		},
		{
			"displayName": "更新人",
			"name": "update_user",
			"custom": true,
			"display": true
		},
		{
			"displayName": "更新时间",
			"name": "update_date",
			"custom": true,
			"display": true
		}
	]
}
```



##  **3.2.5@Dict注解**

代码演示如下 ( 代码来源Demo工程)

```java
 	@Property(displayName = "用户性别")
    @Dict(typeCode = "userSex", defaultValue = "0")
    private String test;
```

- Dict 注解

| 英文         | 名称     | 可选值   |
| ------------ | -------- | -------- |
| typeCode     | 字典类型 | 英文别名 |
| defaultValue | 字典键值 | 0,1,2,3  |
| multiple     | 多选     | 可空     |
| orderBy      |          | 可空     |
| model        |          | 可空     |
| label        |          | 可空     |
| value        |          | 可空     |
| type         |          | 可空     |

- 界面维护字典数据

- 种子维护字典数据

系统内置了字典数据模型base_dict_value，字典类型模型base_dict_type

```json
{
  "data": {
    "DictTypeSex": {
      "model": "base_dict_type",
      "properties": {
        "dictName": "用户性别",
        "dictType": "userSex",
        "status": 0
      }
    },
    "DictValueSexMan": {
      "model": "base_dict_value",
      "properties": {
        "dictLabel": "男",
        "dictValue": "0",
        "dictType": "userSex",
        "isDefault": true,
        "status": 0
      }
    }
  }
}

```

## 3.2.6 特殊属性的查询

查询`@ManyToOne`、`@Selection`和`@Dict`注解标注的属性值时，会根据上下文参数`useDisplayForModel`返回两种不同格式的值。返回值格式一是数据库存储的值；格式二是Options或Options集合类型的值，方便界面显示。当useDisplayForModel为false或不存在时返回格式一，为true时返回格式二。

useDisplayForModel的值使用 `getMeta().addArgument(MetaConstant.USE_DISPLAY_FOR_MODEL, true)` 进行设置。

useDisplayForModel的值仅在第一次执行查询时有效，查询完毕会默认删除useDisplayForModel。如果传递的是`Arrays.asList("*")`，则useDisplayForModel不会生效，但查询完毕后依然会删除useDisplayForModel。

查询方法如下：

| 方法                                     | 解释                       |
| ---------------------------------------- | -------------------------- |
| select(Filter filter)                    | 查询Arrays.asList("*")     |
| select(Filter filter, String... columns) | 根据useDisplayForModel判定 |
| selectById(String id)                    | 查询Arrays.asList("*")     |
| selectById(String id, String... columns) | 根据useDisplayForModel判定 |
| search                                   | 根据useDisplayForModel判定 |
| read                                     | 根据useDisplayForModel判定 |

在前端调用查询方法时，会设置useDisplayForModel为true，其他情况不会设置该值。
