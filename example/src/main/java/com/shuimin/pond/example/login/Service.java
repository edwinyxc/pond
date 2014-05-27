package com.shuimin.pond.example.login;

import com.shuimin.common.f.Tuple;
import com.shuimin.pond.codec.view.View;
import com.shuimin.pond.core.mw.Action;

import static com.shuimin.pond.core.Interrupt.render;

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
            return Tuple.t2(user, true);
        }
        return Tuple.t2(user, false);
    });

    public static Action showResult = Action.consume((Tuple<User,Boolean> last)-> {
            if(!last._b)
                loginFail(last._a);
            else loginSuc(last._a);
        }
    );

    public static void loginSuc(User user){
        render(View.Text.one().val("<h5>User Login Success </h5><p>id:" + user.id + "</p>"));
    }

    public static void loginFail(User user){
        render(View.Text.one().val("<h5>User Login Denied</h5><p>id:" + user.id + "</p>"));
    }
}
