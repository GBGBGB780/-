package com.chinahitech.shop.controller;

import com.chinahitech.shop.bean.User;
import com.chinahitech.shop.bean.notAddedToDatabase.RegisterUser;
import com.chinahitech.shop.defineException.EmailException;
import com.chinahitech.shop.service.EmailService;
import com.chinahitech.shop.service.ManagerService;
import com.chinahitech.shop.utils.JwtUtils;
import com.chinahitech.shop.utils.RedisUtils;
import com.chinahitech.shop.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/manager")
@CrossOrigin
public class ManagerController {
    @Autowired
    private ManagerService managerService;

    @PostMapping("/login")
    // querystring: username=zhangsan&password=123   User user,String username,String password
    // json: {username:zhangsan,password:123}
    // 如果前端传递的数据是json格式，必须使用对象接收，同时需要添加@RequestBody
    public Result login(String userId, String password){
        User user = managerService.login(userId, password);
//        User dbStudent = managerService.getByStunumber(student.getStunumber());
//        if (dbStudent != null && dbStudent.getPassword().equals(student.getPassword())) {
        if (user.getPassword() == null) {
            // 验证失败，返回错误信息
            return Result.error().message("用户名或密码不正确");
        } else if (user.getStatus() < 1) {
            // 权限不足，返回错误信息
            return Result.error().message("用户权限不足");
        } else {
            // 验证通过，生成 token 返回给前端
            String token = JwtUtils.generateToken(user.getUserId());
//        System.out.println(userId + password);
            return Result.ok().data("token", token);
        }
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterUser user) {
//        System.out.println(user);
        String stunumber = user.getUsername();
        String password = user.getPassword();
        String email = user.getEmail();
        String valicode = user.getValicode();

//        System.out.println(stunumber);
//        System.out.println(password);
//        System.out.println(email);
//        System.out.println(valicode);

        String correctValicode = RedisUtils.get(email).toString();

//        System.out.println("this" + correctValicode);

        if (Objects.equals(correctValicode, valicode)){
            managerService.addManager(stunumber, password, email);
            return Result.ok().message("注册成功");
        } else {
            return Result.error().message("注册出错!");
        }
    }

    @PostMapping("/validateEmail")
    public Result validateEmail(String email) throws Exception {
//        String email = emailInfo.getEmail();
        System.out.println(email);
        EmailService sEmail;
        try{
            sEmail = new EmailService(email);
        } catch(EmailException err){
            return Result.error().message(err.expMessage());
        }
        sEmail.sendEmail();
        return Result.ok().message("邮箱发送成功!");
    }

    //用户密码修改
    @PostMapping("/modifyPass")
    public Result modifyPassword(String userId, String password){
        System.out.println(userId);
        System.out.println(password);
        managerService.updatePassword(userId, password);
        return Result.ok();
    }

    //用户电话修改
    @PostMapping("/modifyPhone")
    public Result modifyPhone(String userId, String phone){
        System.out.println(userId);
        System.out.println(phone);
        managerService.updatePhone(userId, phone);
        return Result.ok();
    }

    //用户简介修改
    @PostMapping("/modifyDescription")
    public Result modifyDescription(String userId, String description){
        System.out.println(userId);
        System.out.println(description);
        managerService.updateDescription(userId, description);
        return Result.ok();
    }

    //用户昵称修改
    @PostMapping("/modifyNickname")
    public Result modifyNickname(String userId, String nickname){
        System.out.println(userId);
        System.out.println(nickname);
        managerService.updateNickname(userId, nickname);
        return Result.ok();
    }

    //用户资料显示
    @PostMapping("/profile")
    public Result getProfile(String userId){
        System.out.println(userId);
        User user = managerService.getByUserId(userId);
        System.out.println(user);
        return Result.ok().data("user", user);
    }


    @GetMapping("/info")  // "token:xxx"
    public Result info(String token){
        String username = JwtUtils.getClaimsByToken(token).getSubject();
        String url = "https://nimg.ws.126.net/?url=http%3A%2F%2Fdingyue.ws.126.net%2F2021%2F1120%2F783a7b4ej00r2tvvx002fd200hs00hsg00hs00hs.jpg&thumbnail=660x2147483647&quality=80&type=jpg";
        return Result.ok().data("name",username).data("avatar",url);
    }

    @PostMapping("/logout")  // "token:xxx"
    public Result logout(){
        return Result.ok();
    }
}
