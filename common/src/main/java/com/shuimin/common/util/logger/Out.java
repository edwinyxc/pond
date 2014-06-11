package com.shuimin.common.util.logger;

import com.shuimin.common.abs.Namable;


public abstract class Out implements Namable<Out> {
    private String name;
    private int level = Logger.INFO;

    protected abstract void _print(String s);

    public void print(String s) {
        _print(s);
    }

    public void println(String s) {
        print(s + "\n");
    }

    public Out lvl(int lvl) {
        level = lvl;
        return this;
    }

    public int lvl() {
        return level;
    }

    @Override
    public String name() {
        return name;

    }

    @Override
    public Out name(String name) {
        this.name = name;
        return this;
    }
}
