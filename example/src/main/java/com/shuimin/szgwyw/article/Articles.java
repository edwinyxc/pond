package com.shuimin.szgwyw.article;

import com.shuimin.pond.codec.mvc.Controller;
import com.shuimin.pond.codec.restful.Resource;

/**
 * Created by ed on 5/26/14.
 */
public class Articles {
    static Article article= new Article();
    static ArticleBrief brief= new ArticleBrief();

    public static Controller ARTICLE_RES = Resource.build(() -> article).name("article");

    public static Controller BRIEF_RES = Resource.build(() -> brief).name("article_brief");

}
