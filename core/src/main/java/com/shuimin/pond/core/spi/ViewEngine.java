package com.shuimin.pond.core.spi;

import com.shuimin.pond.core.misc.Renderable;

/**
 * Created by ed on 2014/5/8.
 */
public interface ViewEngine{
    Renderable resolve(String relativePath);
}
