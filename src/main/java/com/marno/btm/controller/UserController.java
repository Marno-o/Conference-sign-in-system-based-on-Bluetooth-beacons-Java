package com.marno.btm.controller;


import com.marno.btm.entity.Users;
import com.marno.btm.service.GetUserService;
import com.marno.btm.tools.AesCbcUtil;
import com.marno.btm.tools.HttpRequest;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yz
 * @version 1.0
 */
@Controller
public class UserController {

    @Resource
    private GetUserService getUserService;

    /**
     * 用户登录小程序
     * @param encryptedData 明文,加密数据
     * @param iv            加密算法的初始向量
     * @param code          用户允许登录后，回调内容会带上 code（有效期五分钟），开发者需要将 code 发送到开发者服务器后台，使用code 换取 session_key api，将 code 换成 openid 和 session_key
     * @return
     */
    @RequestMapping("/usersign")
    @ResponseBody
    public Map usersign(String encryptedData, String iv, String code){
        Map map = new HashMap();

        System.out.println("        ====>   encryptedData:"+encryptedData);
        System.out.println("        ====>   iv:"+iv);
        System.out.println("        ====>   code:"+code);


        //登录凭证不能为空
        if (code == null || code.length() == 0) {
            map.put("status", 0);
            map.put("msg", "code 不能为空");
            return map;
        }

        /**
         * //小程序唯一标识   (在微信小程序管理后台获取)
         *         String wxspAppid = "wxd9097feeb66c25f4";
         *         //小程序的 app secret (在微信小程序管理后台获取)
         *         String wxspSecret = "d0da446bb1ba3fb2ac5dfee134f8e96c";
         */

        //小程序唯一标识   (在微信小程序管理后台获取)
        String wxspAppid = "wxd9097feeb66c25f4";
        //小程序的 app secret (在微信小程序管理后台获取)
        String wxspSecret = "d0da446bb1ba3fb2ac5dfee134f8e96c";
        //授权（必填）
        String grant_type = "authorization_code";


        //////////////// 1、向微信服务器 使用登录凭证 code 获取 session_key 和 openid ////////////////
        //请求参数
        String params = "appid=" + wxspAppid + "&secret=" + wxspSecret + "&js_code=" + code + "&grant_type=" + grant_type;
        //发送请求
        String sr = HttpRequest.sendGet("https://api.weixin.qq.com/sns/jscode2session", params);
        //解析相应内容（转换成json对象）
        JSONObject json = JSONObject.fromObject(sr);
        System.out.println("        ====>   以解密，内容："+json);
        //获取会话密钥（session_key）
        String session_key = json.get("session_key").toString();
        //用户的唯一标识（openid）
        String openid = (String) json.get("openid");

        if(getUserService.ifsigned(openid)){
            map.put("status", 1);
            map.put("msg", "已注册");
            Users user = getUserService.SQL2User(openid);
            map.put("userInfo", user);
            return map;
        }else {
            //////////////// 2、对encryptedData加密数据进行AES解密 ////////////////
            try {
                String result = AesCbcUtil.decrypt(encryptedData, session_key, iv, "UTF-8");
                if (null != result && result.length() > 0) {
                    map.put("status", 1);
                    map.put("msg", "解密成功");

                    JSONObject userInfoJSON = JSONObject.fromObject(result);
                    Map userInfo = new HashMap();
                    userInfo.put("userId", userInfoJSON.get("openId"));
                    userInfo.put("nickName", userInfoJSON.get("nickName"));
                    userInfo.put("gender", userInfoJSON.get("gender"));
                    userInfo.put("city", userInfoJSON.get("city"));
                    userInfo.put("province", userInfoJSON.get("province"));
                    userInfo.put("country", userInfoJSON.get("country"));
                    userInfo.put("avatarUrl", userInfoJSON.get("avatarUrl"));
                    userInfo.put("unionId", userInfoJSON.get("unionId"));
                    userInfo.put("userName", userInfoJSON.get("nickName"));
                    /**
                     * 更新用户，存入数据库
                     */
                    getUserService.User2SQL(userInfo);
                    map.put("userInfo", userInfo);
                    return map;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        map.put("status", 0);
        map.put("msg", "解密失败");
        return map;
    }

    @RequestMapping("/changename")
    @ResponseBody
    public Map changeName(String newName, String openId){
        System.out.println("        ====>   即将修改名称："+newName+"       id:"+openId);
        getUserService.changeName(newName,openId);
        Map map = new HashMap();
        map.put("status",0);
        map.put("userInfo", getUserService.SQL2User(openId));
        map.put("msg", "修改成功");
        System.out.println("        ====>   Map："+map);
        return map;
    }
}