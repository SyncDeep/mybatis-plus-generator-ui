package com.github.davidfantasy.mybatisplus.generatorui;

import com.github.davidfantasy.mybatisplus.generatorui.mbp.NameConverter;

public class GeberatorUIServer {

    public static void main(String[] args) {
        GeneratorConfig config = GeneratorConfig.builder().jdbcUrl("jdbc:mysql://jeecg-boot-mysql:3306/jeecg-boot?useUnicode=true&serverTimezone=Asia/Seoul")
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
