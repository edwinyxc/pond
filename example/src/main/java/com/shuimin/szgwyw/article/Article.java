package com.shuimin.szgwyw.article;

import com.shuimin.pond.codec.mvc.model.AbstractModel;

import java.util.Date;

import static com.shuimin.common.S.time;

/**
 * Created by ed on 2014/5/9.
 * BO
 */
public class Article extends AbstractModel {


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
        field("vid");
        field("title", () -> "default title");
        field("author", () -> "system");
        field("release_date", () -> String.valueOf(time()));
        field("change_date", () -> String.valueOf(time()));
        field("content", () -> "empty");
        field("category");

        primaryKeyName("vid");

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

}