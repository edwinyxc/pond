package com.shuimin.common.sql;

import org.junit.Test;

public class CriterionTest {

    @Test
    public void test() throws Exception {

        SqlSelect sql = Sql.select().from("t_test").where(
                "id", Criterion.EQ, "sss");
        System.out.println(sql.debug());
    }
}