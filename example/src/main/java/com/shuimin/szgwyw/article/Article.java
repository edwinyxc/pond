package com.shuimin.szgwyw.article;

import com.shuimin.pond.codec.db.AbstractRecord;
import com.shuimin.pond.codec.db.Record;
import com.shuimin.pond.core.Request;

import java.util.Date;

import static com.shuimin.common.S.time;

/**
 * Created by ed on 2014/5/9.
 * BO
 */
public class Article extends AbstractRecord {


    /**
     * vid          | varchar(64)  | NO   | PRI | NULL    |       |
     | title        | varchar(255) | YES  | UNI | NULL    |       |
     | author       | varchar(20)  | YES  |     | NULL    |       |
     | release_date | varchar(20)  | YES  |     | NULL    |       |
     | change_date  | varchar(20)  | YES  |     | NULL    |       |
     | content      | longtext     | YES  |     | NULL    |       |
     | category     | varchar(255) | YES  |     | NULL    |       |
     +--------------+--------------+------+-----+---------+-------+
     * @return
     */ {
        this.PKLabel("vid");

        this.setDefault("title", () -> "default title");
        this.setDefault("author", () -> "system");
        this.setDefault("release_date", () -> String.valueOf(time()));
        this.setDefault("content", () -> "empty");

        this.onSet("release_date", (Object o) -> {
            if (o instanceof Date) {
                return String.valueOf(((Date) o).getTime());
            }
            if (o instanceof String) {
                return (String) o;
            }
            return String.valueOf(o);
        });
        this.onGet("release_date", s -> s == null ? null
                : new Date(Long.parseLong((String) s)));

        this.onSet("change_date", (Object o) -> {
            if (o instanceof Date) {
                return String.valueOf(((Date) o).getTime());
            }
            if (o instanceof String) {
                return (String) o;
            }
            return String.valueOf(o);
        });
        this.onGet("change_date", s -> s == null ? null
                : new Date(Long.parseLong((String) s)));

        this.table("t_article");
    }

    @Override
    protected String[] _fields() {
        return new String[]{"vid",
                "title",
                "author",
                "release_date",
                "change_date",
                "content",
                "category"};
    }

    @Override
    public Record merge(Request req) {
        return super.merge(req);
    }

}