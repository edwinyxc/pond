package com.shuimin.pond.db;

import com.shuimin.common.S;
import com.shuimin.common.sql.Criterion;

import java.util.List;

import static com.shuimin.common.S._for;

public class RecordServiceTest {

    public static void test2() {
        RecordService<TestRecord> recordService =
                RecordService.build(new TestRecord());
        List<TestRecord> list =
                recordService.query();
        S.echo(S.dump(_for(list).map(TestRecord::view).toList()));
    }


    public static void test4() {
        RecordService<TestRecord> recordService =
                RecordService.build(new TestRecord());
        List<TestRecord> list =
                recordService.query("create_time", Criterion.BETWEEN, new String[]{
                        "1402230396671", "1402230396673"
                });
        S.echo(S.dump(_for(list).map(TestRecord::view).toList()));
    }

    public static void test3() {
        RecordService<TestRecord> recordService =
                RecordService.build(new TestRecord());
        List<TestRecord> list =
                recordService.query("1 = 1 AND 1 > 0");
        S.echo(S.dump(_for(list).map(TestRecord::view).toList()));
    }

    public static void main(String[] args){
        test3();
    }

}