package com.shuimin.pond.core.mw.router;

import com.shuimin.pond.core.http.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shuimin.pond.core.Server.G.debug;

/**
 * Created by ed interrupt 2014/4/2.
 */
public class RegexPathMatcher implements  PathMather {


    private static Pattern varPattern = Pattern.compile("\\$\\{(\\w+)\\}");

    private Pattern pattern;

    private List<String> pathVarNames = new ArrayList<>();

    public RegexPathMatcher(String path){
        Matcher matcher = varPattern.matcher(path);
        StringBuffer buffer = new StringBuffer();
        while(matcher.find()){
            pathVarNames.add(matcher.group(1));
            //FIXME: url中最后一个 ‘/’ 的问题
            matcher.appendReplacement(buffer, "([^/]+)");
        }
        matcher.appendTail(buffer);
        debug("bind pattern: " + buffer);
        pattern = Pattern.compile(buffer.toString());
    }

    @Override
    public boolean match(Request req) {
        Matcher matcher = pattern.matcher(req.path());
        if(matcher.matches()){
            for(int i = 0; i< matcher.groupCount();i++){
                req.param(pathVarNames.get(i),matcher.group(i+1));
            }
            return true;
        }
        return false;
    }
}
