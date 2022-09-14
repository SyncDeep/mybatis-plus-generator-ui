## mybatis-plus-generator-ui
Provides an interactive Web UI for generating related functional codes compatible with the mybatis-plus framework, including Entity, Mapper, Mapper.xml, Service, Controller, etc. You can customize templates and various output parameters, or directly through SQL query statements Generate code.
 
## Instructions

1.  Introduce the relevant dependencies of maven, note that the scope only needs to write test.

```xml
 <dependency>
    <groupId>com.github.davidfantasy</groupId>
    <artifactId>mybatis-plus-generator-ui</artifactId>
    <version>1.4.5</version>
    <scope>test</scope>
 </dependency>
```

2. Create a new startup class in the test directory of the project. The code example is as follows:

```java
public class GeberatorUIServer {

    public static void main(String[] args) {
        GeneratorConfig config = GeneratorConfig.builder().jdbcUrl("jdbc:mysql://192.168.1.211:3306/example")
                .userName("root")
                .password("root")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                 //Database schema, POSTGRE_SQL, ORACLE, DB2 type databases need to be specified
                .schemaName("myBusiness")
                 //If you need to modify the default naming rules of various generated files, you can customize a NameConverter instance to override the corresponding name conversion method:                
                .nameConverter(new NameConverter() {
                    /**
                     * Name rules for custom Service class files
                     */
                    @Override
                    public String serviceNameConvert(String tableName) {
                        return this.entityNameConvert(tableName) + "Service";
                    }
                    /**
                     * Custom Controller class file name rules
                     */
                    @Override
                    public String controllerNameConvert(String tableName) {
                        return this.entityNameConvert(tableName) + "Action";
                    }
                })
                .basePackage("com.github.davidfantasy.mybatisplustools.example")
                .port(8068)
                .build();
        MybatisPlusToolsApplication.run(config);
    }

}
```

**GeneratorConfig**also includes some basic configuration parameters and various extensible interfaces, such as custom template parameters. For specific instructions, see the source code comments.

3. Run the startup class to start a Generator Server. Then visit [http://localhost:8068](http://localhost:8068/) (the port is configurable) to enter the management interface.

#### Renew
**1.4.0**GeberatorUIServer can be independently deployed as a separate spring boot project to provide source code generation services for multiple projects by specifying the root directory of the target project on the page.

## The main function
1. **Data table query and browsing**：You can directly browse and query the data table information of the configured data source, and you can select one or more generated template codes:

![数据表查询](https://gitee.com/davidfantasy/mybatis-plus-generator-ui/raw/master/imgs/table-list.png)

2. **Configuration of output files**： template configuration of built-in 6 types of codes such as Entity, Mapper, Service, Controller, etc., templates can be uploaded for replacement, and various parameters can be modified. The configuration parameters have been reclassified according to the affected file types, and added Part of the text description; you can also add other types of custom output files yourself. All configuration items will be saved according to the project package name, and only need to be set once.

![输出文件类型配置](https://gitee.com/davidfantasy/mybatis-plus-generator-ui/raw/master/imgs/output-config.png)

![文件输出选项配置](https://gitee.com/davidfantasy/mybatis-plus-generator-ui/raw/master/imgs/strategy.png)

3. **Code generation options **：Add the content that may change each time the code is generated to the code generation options to facilitate the adjustment of each generation strategy, such as: whether to overwrite the original file, the type of the generated file, etc.:

![代码生成选项](https://gitee.com/davidfantasy/mybatis-plus-generator-ui/raw/master/imgs/generator-options.png)

4. **SQL result set automatic code generation**：By entering query SQL, the corresponding query method, DTO object and ResultMap (result set mapping configuration) can be automatically generated in Mapper (Xml and Java)

![SQL编辑](https://gitee.com/davidfantasy/mybatis-plus-generator-ui/raw/master/imgs/SQL-edit.png)

![SQL代码生成选项](https://gitee.com/davidfantasy/mybatis-plus-generator-ui/raw/master/imgs/SQL-generator-options.png)

5. **Dynamic SQL enhancement**：automatically identify where conditions containing mybatis dynamic parameters and convert them to dynamic SQL conditions of mybatis

![动态SQL增强](https://gitee.com/davidfantasy/mybatis-plus-generator-ui/raw/master/imgs/dynamicsql.png)


## common problem

**Q:How to directly run the downloaded source code?**

**It is not recommended to directly download the source code and run it**，This project is designed to be directly embedded in the corresponding business project and can automatically identify environment variables such as the project path. If you want to run from source code, you need to compile the static resources in src\frontend separately (the source code does not contain compiled pages), and execute in the src\frontend folder:
~~~shell
yarn install
yarn run build
~~~
Then modify the relevant configuration of the database in src\test\java\TestApplication and run it.

**Q:What types of databases are supported?**

Support almost all mainstream databases, please refer to the documentation of the mybatis-plus-generator framework for details. 
You need to import the driver package of the database yourself, and specify the driverClassName in GeneratorConfig.

**Q:How to customize template parameters?**

Customize TemplateVariableInjecter in GeneratorConfig to return parameters that need to be used in the template, for example:
```java
 GeneratorConfig config = GeneratorConfig.builder()
                .templateVaribleInjecter(new TemplateVaribleInjecter() {
                    @Override
                    public Map<String, Object> getCustomTemplateVaribles(TableInfo tableInfo) {
                        Map<String,Object> params = new HashMap<>();
                        return params;
                    }
                })
```

**Q:Where is the saved configuration stored?**

All user-saved configurations are grouped by basePackage and saved to .mybatis-plus-generator-ui in the user.home directory, and the configurations of different projects will not affect each other.

