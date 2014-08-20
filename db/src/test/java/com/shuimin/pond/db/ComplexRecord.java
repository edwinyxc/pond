package com.shuimin.pond.db;

import com.shuimin.common.S;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.shuimin.common.S._for;
import static com.shuimin.common.S._try;

/**
 * Created by ed on 8/18/14.
 */
public class ComplexRecord extends AbstractRecord {

    {
        table("t_attachment");
        id("id");
        field("name");
        field("content").mapper((name, rs) ->
                        _try(() -> rs.getBinaryStream(name))
        ).view(t -> "blob");
        //default
        set("content", "(empty)");
        set("name", "(empty)");
    }

    public void export(File f) {
        try {
            if (!f.exists() || f.createNewFile()) {
                S.file.inputStreamToFile(this.get("content"), f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        List<ComplexRecord> a =
                DB.fire(t -> t.map(ComplexRecord.class,
                        "select * from t_attachment where id= 'a932af41341b492b8f0c7b86acc5604d'"));

        _for(a).each(i -> i.export(new File("/tmp", i.get("name"))));

        System.out.println(S.dump(a));
    }
}
