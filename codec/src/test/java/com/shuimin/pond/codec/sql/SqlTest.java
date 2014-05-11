package com.shuimin.pond.codec.sql;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SqlTest {


    @Test
    public void testSelectAllFrom() {
        assertEquals("SELECT * FROM users", Sql.select().from("users").toString());
        assertEquals("SELECT * FROM users", Sql.select("*").from("users").toString());
    }

    @Test
    public void testSelectSomeFrom() {
        assertEquals("SELECT sex, name FROM users",
            Sql.select("sex", "name").from("users").toString());
    }

    @Test
    public void testWhere() {
        String Sql1 = Sql.select().from("users").where("gender = 'female'").toString();
        String Sql2 = Sql.select().from("users").where("age between 10 AND 20").toString();
        assertEquals("SELECT * FROM users WHERE gender = 'female'", Sql1);
        assertEquals("SELECT * FROM users WHERE age between 10 AND 20", Sql2);
    }


    @Test
    public void testUpdate() {
        assertEquals("update users set age = ?", Sql.update("users").set("age").toString().toLowerCase());
        assertEquals("update users set name = ?, age = ? where id = ?", Sql.update("users").set("name", "age")
            .where("id = ?").toString().toLowerCase());
        assertEquals("update users set age = ? where id > ? and name = ?", Sql.update("users").set("age")
            .where("id > ?", "name = ?").toString().toLowerCase());
    }

    @Test
    public void testDelete() {
        assertEquals("delete from users", Sql.delete().from("users").toString().toLowerCase() );
        assertEquals("delete from users where id > 2 and name = ?", Sql.delete().from("users")
            .where("id > 2", "name = ?").toString().toLowerCase());
    }

    @Test
    public void testInsert() {
        assertEquals("insert into users (name, age) values (?, ?)",
            Sql.insert().into("users")
                .values("name", "age")
                .toString().toLowerCase()
        );
    }
}