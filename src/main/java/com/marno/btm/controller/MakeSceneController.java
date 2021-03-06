package com.marno.btm.controller;

import com.marno.btm.entity.Scene;
import com.marno.btm.service.MakeSceneService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 叶勇
 * @version 1.0
 * @date 2020/2/10 22:33
 */

@Controller
public class MakeSceneController {
    /**
     * 新建签到场景
     * 需要参数：签到方式、蓝牙信标ID、发起人ID
     */

    @Resource
    private MakeSceneService makeSceneService;

    @RequestMapping("/makescene")
    @ResponseBody
    public Map makescene(String originatorID,
                         String userPic,
                         String userName,
                         String theme,
                         String hoster,
                         String startdate,
                         String starttime,
                         Integer timelong,
                         String address,
                         boolean ifRegister,
                         String mymessage,
                         int mode,
                         String deviceId
                            ) throws ParseException {

        System.out.println("originatorID," +originatorID+
                "\nuserPic," +userPic+
                           "\ntheme," +theme+
                           "\nmode," +mode+
                           "\ndeviceId,"+deviceId +
                           "\nmymessage," +mymessage+
                           "\nifRegister," +ifRegister+
                           "\nstdate,"+startdate +
                           "\nsttime," +starttime+
                           "\ntimelong      "+timelong);

        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startTime=formatter.parse(startdate+" "+starttime);
        Date endTime = formatter.parse(startdate+" "+starttime);
        endTime.setTime(endTime.getTime()+timelong*60*1000);
        System.out.println("sttime:"+startTime+"endtime:"+endTime);

        Scene scene = new Scene();
        String newsceneID = makeSceneID();
        scene.setSceneID(newsceneID);
        scene.setOriginatorID(originatorID);
        scene.setUserPic(userPic);
        scene.setUserName(userName);
        scene.setTheme(theme);
        scene.setHoster(hoster);
        scene.setCreatTime(new Date());
        scene.setStartTime(startTime);
        scene.setEndTime(endTime);
        scene.setAddress(address);
        if(ifRegister){
            scene.setIfRegister(1);
        }else{
            scene.setIfRegister(2);
        }
        scene.setMessage(mymessage);
        scene.setMode(mode);
        scene.setDeviceID(deviceId);
        System.out.println(scene);
        Map map = new HashMap();
        int motto =  makeSceneService.MakeScene(scene);
        map.put("motto",motto);
        if(motto == 1) {
            map.put("newsceneID", newsceneID);
        }
        return map;
    }
    /**
     * 以当前时间生成场景ID
     * @return
     */
    private String makeSceneID(){
        Calendar c = Calendar.getInstance();
        String year=Integer.toString(c.get(Calendar.YEAR));
        String month=Integer.toString(c.get(Calendar.MONTH)+1);
        String day=Integer.toString(c.get(Calendar.DAY_OF_MONTH));
        String hh=Integer.toString(c.get(Calendar.HOUR_OF_DAY));
        String mi=Integer.toString(c.get(Calendar.MINUTE));
        String ss = Integer.toString(c.get(Calendar.SECOND));
        if(c.get(Calendar.MONTH)+1<10){month = "0"+month;}
        if(c.get(Calendar.DAY_OF_MONTH)<10){day = "0"+day;}
        if(c.get(Calendar.HOUR_OF_DAY)<10){hh = "0"+hh;}
        if(c.get(Calendar.MINUTE)<10){mi = "0"+mi;}
        if(c.get(Calendar.SECOND)<10){ss = "0"+ss;}
        System.out.println("新的场景ID："+year+month+day+hh+mi+ss);
        return year+month+day+hh+mi+ss;
    }

}
