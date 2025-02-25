package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import com.heima.utils.common.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {

    @Override
    public ResponseResult login(LoginDto dto) {
        //1.正常登录 用户名和密码
        if(StringUtils.isNoneBlank(dto.getPhone()) && StringUtils.isNoneBlank(dto.getPassword())){
            //1.1根据手机号查询用户信息
            ApUser dbUser = getOne(Wrappers.<ApUser>lambdaQuery().eq(ApUser::getPhone, dto.getPhone()));
            if(dbUser == null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"数据不存在");
            }
            //1.2对比密码
            String salt =  dbUser.getSalt();
            String password = dto.getPassword();
            String pswd= DigestUtils.md5DigestAsHex((password+salt).getBytes());
            if(!pswd.equals(dbUser.getPassword())){
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }

            //1.3返回数据 jwt
            Map<String,Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(dbUser.getId().longValue()));
            dbUser.setSalt("");
            dbUser.setPassword("");
            map.put("user", dbUser);
            return ResponseResult.okResult(map);
        }else {
            //游客同样的token id=0
            Map<String,Object> map = new HashMap<>();
            map.put("token",AppJwtUtil.getToken(0L));
             return ResponseResult.okResult(map);
        }
    }
}
