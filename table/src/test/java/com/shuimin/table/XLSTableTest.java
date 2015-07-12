package com.shuimin.table;

import pond.common.S;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XLSTableTest {
    public File dir = new File(S.path.rootClassPath());

    @Test
    public void testReading() throws Exception {
        Table t = new XLSTable(new FileInputStream(new File(dir, "wb.xls")));
        System.out.println(t.toString());
    }

    @Test
    public void testModel() throws Exception {
        XLSTable t = new XLSTable(new FileInputStream(new File(dir, "wb_model.xls")));
        t.analyze();
        System.out.println(S.dump(t.model()));
        Map model = new HashMap<String, String>() {
            {
                put("title", "测试标题");
                put("text", "请在这里输入正文" +
                        "请在这里输入正文请" +
                        "在这里输" +
                        "入正文请" +
                        "在" +
                        "这里输入正文请" +
                        "在这里输入正文请在这" +
                        "里输入正文请在这里" +
                        "输入正文请在这里输" +
                        "输入正文请在这里输" +
                        "输入正文请在这里输" +
                        "输入正文请在这里输" +
                        "输入正文请在这里输"
                );
                put("Salary_item_1","工资项1");
                put("Salary_item_1_value","金额200");
            }
        };
        t.model(model);
        System.out.println(t.toString());
        File out = new File(dir,"out.xls");
        if (!out.exists()){
            out.createNewFile();
        }
        t.save(new FileOutputStream(out));
    }

    @Test
    public void testRowModelParsing() throws Exception {
        XLSTable t = new XLSTable(new FileInputStream(
                new File(dir, "wb_row.xls")));
        System.out.println(t.rows());
        List<Map<String,Object>> list = t.parse(3,t.rows(),t.theadMapper(2));
        System.out.print(list.toString());
    }
}
