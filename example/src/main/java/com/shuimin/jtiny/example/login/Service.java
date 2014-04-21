package com.shuimin.jtiny.example.login;

import com.shuimin.base.f.Tuple;
import com.shuimin.jtiny.codec.view.View;
import com.shuimin.jtiny.core.mw.Action;

import static com.shuimin.jtiny.core.Interrupt.render;

/**
 * Created by ed on 2014/4/10.
 */
public class Service{
    public static Action parseUser = Action.resolve((req,resp)->
        new User(){
            {this.id = req.param("id") ; this.pass = req.param("pass");}
        }
    );

    public static Action checkPass = Action.process((User user)->{
        if (user.pass .equals("123456")){
            return Tuple._2(user,true);
        }
        return Tuple._2(user,false);
    });

    public static Action showResult = Action.consume((Tuple<User,Boolean> last)-> {
            if(!last._b)
                loginFail(last._a);
            else loginSuc(last._a);
        }
    );

    public static void loginSuc(User user){
        render(View.Text.one().text("<h5>User Login Success </h5><p>id:"+user.id+"</p>"));
    }

    public static void loginFail(User user){
        render(View.Text.one().text("<h5>User Login Denied</h5><p>id:"+user.id+"</p>"));
    }
}
