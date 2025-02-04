package com.chinahitech.shop.controller;

import cn.hutool.core.io.FileUtil;
import com.chinahitech.shop.aop.RepeatLimit;
import com.chinahitech.shop.bean.User;
import com.chinahitech.shop.bean.notAddedToDatabase.RegisterUser;
import com.chinahitech.shop.exception.EmailException;
import com.chinahitech.shop.service.EmailService;
import com.chinahitech.shop.service.TopManagerService;
import com.chinahitech.shop.utils.JwtUtils;
import com.chinahitech.shop.utils.RedisUtils;
import com.chinahitech.shop.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/topManager")
@CrossOrigin
public class TopManagerController {
    @Autowired
    private TopManagerService topManagerService;

    @Value("${upload-dir}")
    private String uploadDir;

    private static final String ROOT_PATH = System.getProperty("user.dir") + File.separator + "upload";

    //登录
    @RepeatLimit
    @PostMapping("/login")
    // querystring: userName=zhangsan&password=123   User user,String userName,String password
    // json: {userName:zhangsan,password:123}
    // 如果前端传递的数据是json格式，必须使用对象接收，同时需要添加@RequestBody
    public Result login(String userId, String password){
        User user = topManagerService.login(userId, password);
//        User dbStudent = topManagerService.getByStunumber(student.getStunumber());
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

    //注册
    @RepeatLimit
    @PostMapping("/register")
    public Result register(@RequestBody RegisterUser user) {
//        System.out.println(user);
        String stunumber = user.getUserId();
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
            topManagerService.addManager(stunumber, password, email);
            return Result.ok().message("注册成功");
        } else {
            return Result.error().message("注册出错!");
        }
    }

    //找回密码

    //1.首先获取该账号对应邮箱,发送验证码
    @RepeatLimit
    @PostMapping("/getEmail")
    public Result getEmail(String userNumber) throws Exception {
//        System.out.println(userNumber);
        User user = topManagerService.getByUserId(userNumber);
//        System.out.println(user);
        String email = user.getEmail();
        EmailService sEmail;
        sEmail = new EmailService(email);
        sEmail.sendEmail();
        return Result.ok().data("email", email);
    }

    //2.然后检查用户输入的验证码是否正确
    @RepeatLimit
    @PostMapping("/getValidate")
    public Result getValidate(String email, String validateCode){
        String correctValidateCode = RedisUtils.get(email).toString();

//        System.out.println("this" + correctValidateCode);

        if (Objects.equals(correctValidateCode, validateCode)){
            return Result.ok().message("验证成功");
        } else {
            return Result.error().message("验证出错!");
        }
    }

    //3.若验证通过，需要重新设置密码，使用modifyPass修改密码

    @RepeatLimit
    @PostMapping("/validateEmail")
    public Result validateEmail(String email) throws Exception {
//        String email = emailInfo.getEmail();
//        System.out.println(email);
        EmailService sEmail;
        sEmail = new EmailService(email);
        sEmail.sendEmail();
        return Result.ok().message("邮箱发送成功!");
    }

    //用户密码修改
    @RepeatLimit
    @PostMapping("/modifyPass")
    public Result modifyPassword(String userId, String password){
//        System.out.println(userId);
//        System.out.println(password);
        topManagerService.updatePassword(userId, password);
        return Result.ok();
    }

    //用户电话修改
    @RepeatLimit
    @PostMapping("/modifyPhone")
    public Result modifyPhone(String userId, String phone){
//        System.out.println(userId);
//        System.out.println(phone);
        topManagerService.updatePhone(userId, phone);
        return Result.ok();
    }

    //用户简介修改
    @RepeatLimit
    @PostMapping("/modifyDescription")
    public Result modifyDescription(String userId, String description){
//        System.out.println(userId);
//        System.out.println(description);
        topManagerService.updateDescription(userId, description);
        return Result.ok();
    }

    //用户昵称修改
    @RepeatLimit
    @PostMapping("/modifyNickname")
    public Result modifyNickname(String userId, String nickname){
//        System.out.println(userId);
//        System.out.println(nickname);
        topManagerService.updateNickname(userId, nickname);
        return Result.ok();
    }

    //用户校区,学院和专业信息修改
    @RepeatLimit
    @PostMapping("/modifyMajor")
    public Result modifyMajor(String userId, String campus, String school, String major){
//        System.out.println(userId);
//        System.out.println(campus);
//        System.out.println(school);
//        System.out.println(major);
        topManagerService.updateMajor(userId, campus, school, major);
        return Result.ok();
    }

    //用户资料显示
    @RepeatLimit
    @PostMapping("/profile")
    public Result getProfile(String userId){
//        System.out.println(userId);
        User user = topManagerService.getByUserId(userId);
//        System.out.println(user);
        return Result.ok().data("user", user);
    }

    @RepeatLimit
    @GetMapping("/info")  // "token:xxx"
    public Result info(String token){
        String username = JwtUtils.getClaimsByToken(token).getSubject();
        String url = "https://nimg.ws.126.net/?url=http%3A%2F%2Fdingyue.ws.126.net%2F2021%2F1120%2F783a7b4ej00r2tvvx002fd200hs00hsg00hs00hs.jpg&thumbnail=660x2147483647&quality=80&type=jpg";
        return Result.ok().data("name",username).data("avatar",url);
    }

    //登出
    @RepeatLimit
    @PostMapping("/logout")  // "token:xxx"
    public Result logout(){
        return Result.ok();
    }

    //添加用户
    @RepeatLimit
    @PostMapping("/addUser")
    public Result addUser(@RequestBody RegisterUser user) {
//        System.out.println(user);
        String stunumber = user.getUserId();
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
            topManagerService.addUser(stunumber, password, email);
            return Result.ok().message("注册成功");
        } else {
            return Result.error().message("注册出错!");
        }
    }

    //查看用户信息
    @RepeatLimit
    @RequestMapping("/getAllUsers")
    public Result getAllUsers(String searchInfo){
        List<User> Users = topManagerService.getAllUsers(searchInfo);
//        System.out.println(Users);
        return Result.ok().data("items",Users);
    }

    //修改用户信息
    @RepeatLimit
    @PostMapping("/modifyUserInfo")
    public Result modifyUserInfo(@RequestBody User user){
//        System.out.println(user);
        topManagerService.updateUserInfo(user);
        return Result.ok();
    }

    //删除用户
    @RepeatLimit
    @RequestMapping("/deleteUser")
    public Result deleteUser(@RequestBody User user){
//        System.out.println(user.getUserName());
        topManagerService.deleteUser(user);
        return Result.ok();
    }

    //批量导入
    @RepeatLimit
    @PostMapping("/uploadExcel")
    public ResponseEntity<Map<String, String>> uploadExcel(@RequestParam("file") MultipartFile file) {

        String fileName = generateUniqueFileName(file.getOriginalFilename());

        try {
            Path targetLocation = Paths.get(ROOT_PATH, fileName);
            if(!FileUtil.exist(ROOT_PATH)){
                FileUtil.mkdir(ROOT_PATH);    // 如果当前文件的父级目录不存在，就创建
            }
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);


            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .pathSegment("upload")
                    .pathSegment(fileName)
                    .toUriString();
//            System.out.println(targetLocation);
//            System.out.println(fileUrl);

            topManagerService.uploadExcel(file);
            Map<String, String> response = new HashMap<>();
            response.put("fileUrl", fileUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/downloadExcel")
    public void downloadExcel(String fileName, HttpServletResponse response) throws IOException {
//        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));  // 附件下载
        // 默认格式就是预览，浏览器会根据格式进行判断，如果可以就预览，不可以就下载
//        response.addHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));  // 附件预览
        String filePath = ROOT_PATH + File.separator + fileName;
        if (!FileUtil.exist(filePath)) {
            return;
        }
        byte[] bytes = FileUtil.readBytes(filePath);
        response.setContentType("text/html;charset=utf-8");
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(bytes);    // 数组是一个字节数组，也就是文件的字节流数组
        outputStream.flush();
        outputStream.close();
    }

    private String generateUniqueFileName(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return timestamp + "_" + originalFilename;
    }

    private Map<String, String> createErrorResponse(String errorMessage) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);
        return errorResponse;
    }
}
