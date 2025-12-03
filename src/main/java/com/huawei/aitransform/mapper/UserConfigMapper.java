package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.UserConfigVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户权限配置Mapper接口
 */
@Mapper
public interface UserConfigMapper {

    /**
     * 查询所有有效用户（is_deleted不为删除状态）
     * @return 有效用户列表
     */
    List<UserConfigVO> selectValidUsers();
}

