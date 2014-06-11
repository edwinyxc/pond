package com.shuimin.szgwyw;

import com.shuimin.common.abs.Config;
import com.shuimin.pond.codec.mvc.Controller;
import com.shuimin.pond.codec.restful.Resource;
import com.shuimin.pond.core.mw.Dispatcher;
import com.shuimin.szgwyw.article.Article;
import com.shuimin.szgwyw.article.ArticleBrief;

/**
 * Created by ed on 5/26/14.
 */
public class ARTICLE implements Config<Dispatcher> {

    Controller ARTICLE_RES =
            Resource.build(new Article()).name("article");
    Controller BRIEF_RES =
            Resource.build(new ArticleBrief()).name("article_brief");

    @Override
    public void config(Dispatcher dispatcher) {
        dispatcher.use(ARTICLE_RES);
        dispatcher.use(BRIEF_RES);
    }
}
