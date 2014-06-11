package com.shuimin.pond;

import com.shuimin.pond.core.exception.PondException;
import com.shuimin.pond.core.exception.UnexpectedException;

import static com.shuimin.common.S.echo;

/**
 * Created by ed on 2014/4/11.
 */
public class PondExceptionTest {
    public static void print(String s) {
        echo(s);
    }

    public static void _throw() {
        throw new UnexpectedException() {
            @Override
            public String brief() {
                return "sddd";
            }
        };
    }

    public static void main(String[] args) {
        try {
            _throw();
        } catch (PondException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static interface A {
        void test(String a);
    }
}
