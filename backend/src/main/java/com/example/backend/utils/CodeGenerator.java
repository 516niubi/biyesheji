package com.example.backend.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.db.ds.simple.SimpleDataSource;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author 
 * @Description 后端代码生成器
 * @Version 1.0
 */
public class CodeGenerator {
    // 填你的数据库名称
    private static final String DATASOURCE = "base-backend";

    // 你自己的项目路径，一定要改成你自己的！！结尾必须是:  \\springboot\\
    private static final String basePath = "E:\\项目\\个人\\version\\1.0\\base-backend\\";
    private static final String vuePath = "E:\\项目\\个人\\version\\1.0\\frontend\\";

    // 数据库需要生成代码的表名
    private static final String tableName = "notice";

    // ----------------------------------------以上必修修改-----------------------------------------

    // 数据库密码 （填你自己的密码，默认 123456）
    private static final String PASSWORD = "123456";

    private static final String url = "jdbc:mysql://localhost:3306/" + DATASOURCE;
    private static final String username = "root";
    private static final String password = PASSWORD;

    public static void main(String[] args) throws Exception {
        // 生成SpringBoot
        generate(tableName);
        // 生成vue
        createVue(tableName);
    }

    /**
     * 生成SpringBoot文件
     */
    private static void generate(String tableName) {
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder.author("") // 设置作者
                            .enableSwagger()
                            .fileOverride() // 覆盖已生成文件
                            .disableOpenDir()
                            .outputDir(basePath + "src/main/java/"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.example.backend") // 设置父包名
                            .moduleName(null) // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, basePath + "src\\main\\resources\\mapper\\")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.entityBuilder().enableLombok();
                    builder.controllerBuilder().enableHyphenStyle()  // 开启驼峰转连字符
                            .enableRestStyle();  // 开启生成@RestController 控制器
                    builder.addInclude(tableName) // 设置需要生成的表名
                            .addTablePrefix("t_", "sys_"); // 设置过滤表前缀
                })
                .execute();

    }

    /**
     * 生成vue文件
     */
    private static void createVue(String tableName) throws Exception {
        String lowerEntityName = StrUtil.toCamelCase(tableName.replace("sys_", "").replace("t_", ""));
        String upperEntityName = StrUtil.upperFirst(lowerEntityName);
        String space = "  ";
        String space2 = "   ";
        String space6 = "      ";
        String space8 = "        ";
        String space10 = "          ";
        StringBuilder tableColumnBuilder = new StringBuilder();
        StringBuilder formItemBuilder = new StringBuilder();
        // 生成表单初始化数据
        StringBuilder initFormBuilder = new StringBuilder();
        List<TableColumn> tableColumns = getTableColumns(tableName);
        for (TableColumn tableColumn : tableColumns) {
            if (tableColumn.getName().equals("id")) {
                continue;
            }

            String camelCaseName = StrUtil.toCamelCase(tableColumn.getName());


            // 生成表格列
            if (tableColumn.getName().endsWith("img")) {
                tableColumnBuilder.append(space6).append("<el-table-column label=\"图片\"><template slot-scope=\"scope\"><el-image style=\"width: 100px; height: 100px\" :src=\"scope.row.").append(camelCaseName).append("\" :preview-src-list=\"[scope.row.img]\"></el-image></template></el-table-column>\n");
            } else if (tableColumn.getName().endsWith("file")) {
                tableColumnBuilder.append(space6).append("<el-table-column label=\"文件\"><template slot-scope=\"scope\"><el-button type=\"primary\" @click=\"download(scope.row.").append(camelCaseName).append(")\">下载</el-button></template></el-table-column>\n");
            } else {
                tableColumnBuilder.append(space6).append("<el-table-column prop=\"").append(camelCaseName).append("\" label=\"").append(tableColumn.getComment()).append("\"></el-table-column>\n");
            }

            // 生成表单初始化数据
            if (tableColumn.getName().contains("time")) {
                continue;
            }
            if (tableColumn.getName().endsWith("delete")) {
                continue;
            }
            initFormBuilder.append(space).append(camelCaseName).append(": \"\",\n");
            // 生成表单项
            StringBuilder formBuilder = formItemBuilder.append(space8).append("<el-form-item label=\"").append(tableColumn.getComment()).append("\">\n");
            if (tableColumn.getName().contains("time")) {
                // 日期时间
                formBuilder.append(space10).append("<el-date-picker v-model=\"form.").append(camelCaseName).append("\" type=\"datetime\" value-format=\"yyyy-MM-dd HH:mm:ss\" placeholder=\"选择日期时间\"></el-date-picker>\n");
            } else if (tableColumn.getName().endsWith("date")) {
                // 日期
                formBuilder.append(space10).append("<el-date-picker v-model=\"form.").append(camelCaseName).append("\" type=\"date\" value-format=\"yyyy-MM-dd\" placeholder=\"选择日期\"></el-date-picker>\n");
            } else if (tableColumn.getName().endsWith("file")) {
                // 文件上传
                formBuilder.append(space10).append("<el-upload action=\"http://localhost:9090/file/upload\" ref=\"file\" :on-success=\"handleFileUploadSuccess\">\n");
                formBuilder.append(space10).append("  <el-button size=\"small\" type=\"primary\">点击上传</el-button>\n");
                formBuilder.append(space10).append("</el-upload>\n");
            } else if (tableColumn.getName().endsWith("img")) {
                // 图片上传
                formBuilder.append(space10).append("<el-upload action=\"http://localhost:9090/file/upload\" ref=\"img\" :on-success=\"handleImgUploadSuccess\">\n");
                formBuilder.append(space10).append("  <el-button size=\"small\" type=\"primary\">点击上传</el-button>\n");
                formBuilder.append(space10).append("</el-upload>\n");
            } else {
                // 普通输入框
                formBuilder.append(space10).append("<el-input v-model=\"form.").append(camelCaseName).append("\"></el-input>\n");
            }
            formBuilder.append(space8).append("</el-form-item>\n");
        }

        Map<String, String> map = new HashMap<>();
        map.put("tableColumn", tableColumnBuilder.toString());
        map.put("form", initFormBuilder.toString());
        map.put("formItem", formItemBuilder.toString());
        map.put("upperEntityName", upperEntityName);
        map.put("lowerEntityName", lowerEntityName);
        // 读取模板文件
        String templateContent = FileUtil.readUtf8String(basePath + "src/main/resources/templates/frontend.template");

        // 格式化模板内容
        String formattedContent = StrUtil.format(templateContent, map);

        // 创建文件夹
        String folderPath = vuePath + "/src/views/system/" + lowerEntityName;
        FileUtil.mkdir(folderPath); // 创建文件夹

        // 生成文件路径
        String filePath = folderPath + "/index.vue";

        // 写入文件
        FileUtil.writeString(formattedContent, filePath, "UTF-8");

        // 打印成功信息
        System.out.println(upperEntityName + "/index.vue 生成成功！");

    }


    /**
     * 获取数据库表字段
     *
     * @param tableName
     * @return
     * @throws SQLException
     */
    private static List<TableColumn> getTableColumns(String tableName) throws SQLException {
        DataSource ds = getDatasource();
        String sql = "SELECT table_name,column_name,data_type, column_comment FROM information_schema.COLUMNS WHERE table_schema = ? and table_name = ?";
        String schema = url.substring(url.lastIndexOf("/") + 1);
        List<Entity> list = Db.use(ds).query(sql, schema, tableName);
        List<TableColumn> columns = CollUtil.newArrayList();
        for (Entity entity : list) {
            TableColumn tableColumn = new TableColumn();
            String columnName = entity.getStr("column_name");
            tableColumn.setName(columnName);
            String comment = entity.getStr("column_comment");
            tableColumn.setComment(comment);
            columns.add(tableColumn);
        }
        return columns;
    }

    private static DataSource getDatasource() {
        return new SimpleDataSource(url, username, password);
    }
}
