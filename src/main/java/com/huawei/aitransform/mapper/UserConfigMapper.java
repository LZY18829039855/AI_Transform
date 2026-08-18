package com.huawei.aitransform.mapper;

import com.huawei.aitransform.entity.UserConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 根据工号查询有效用户（is_deleted不为删除状态）
     * @param account 工号
     * @return 用户信息，如果不存在或无效则返回null
     */
    UserConfigVO selectValidUserByAccount(String account);

    /**
     * 有效用户数量（可按工号模糊筛选）
     */
    long countValidByAccount(@Param("account") String account);

    /**
     * 有效用户分页（可按工号模糊筛选）
     */
    List<UserConfigVO> selectValidPage(
            @Param("account") String account,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 按主键查询（含已删除）
     */
    UserConfigVO selectById(@Param("id") Integer id);

    /**
     * 按主键查询有效用户
     */
    UserConfigVO selectValidById(@Param("id") Integer id);

    /**
     * 按工号查询（含已删除，用于恢复软删记录）
     */
    UserConfigVO selectByAccount(@Param("account") String account);

    int insert(UserConfigVO record);

    int updateById(UserConfigVO record);

    int softDeleteById(@Param("id") Integer id);
}

