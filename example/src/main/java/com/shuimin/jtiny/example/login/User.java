package com.shuimin.jtiny.example.login;

/**
 * Created by ed on 2014/4/10.
 */
public class User {
    public String id;
    public String pass;

    public static User gufei = new User(){
        {this.id = "gufei"; this.pass = "123456";}
    };
    public static User yxc = new User(){
        {this.id = "yxc"; this.pass = "123456";}
    };

}
