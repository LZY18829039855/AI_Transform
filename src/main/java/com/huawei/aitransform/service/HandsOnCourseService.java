package com.huawei.aitransform.service;

import com.huawei.aitransform.entity.HandsOnCourse;
import com.huawei.aitransform.entity.HandsOnCoursesSyncRequestVO;
import com.huawei.aitransform.mapper.HandsOnCourseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 实战课程个人完课情况同步服务类
 */
@Service
public class HandsOnCourseService {

    private static final Logger logger = LoggerFactory.getLogger(HandsOnCourseService.class);

    @Autowired
    private HandsOnCourseMapper handsOnCourseMapper;

    /**
     * 同步实战课程个人完课情况
     * 
     * @param request 请求参数
     * @return 同步结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncHandsOnCourse(HandsOnCoursesSyncRequestVO request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            logger.info("开始同步实战课程个人完课情况，工号：{}，课程类型：{}", request.getAccount(), request.getTaskType());
            
            // 1. 参数校验
            if (request.getAccount() == null || request.getAccount().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "工号不能为空");
                return result;
            }
            if (request.getTaskType() == null || request.getTaskType().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "课程类型不能为空");
                return result;
            }
            if (request.getTaskStatus() == null || request.getTaskStatus().trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "课程状态不能为空");
                return result;
            }
            
            // 2. 查询是否存在记录
            HandsOnCourse existing = handsOnCourseMapper.selectByAccountAndTaskType(
                request.getAccount().trim(), 
                request.getTaskType().trim()
            );
            
            String action;
            if (existing != null) {
                // 3. 存在则更新
                HandsOnCourse updateEntity = new HandsOnCourse();
                updateEntity.setAccount(request.getAccount().trim());
                updateEntity.setTaskType(request.getTaskType().trim());
                updateEntity.setTaskStatus(request.getTaskStatus().trim());
                // task_info 为选填，未传或为空时更新为空（不保留原值）
                updateEntity.setTaskInfo(request.getTaskInfo() != null ? request.getTaskInfo().trim() : null);
                
                int updateCount = handsOnCourseMapper.updateByAccountAndTaskType(updateEntity);
                if (updateCount > 0) {
                    action = "update";
                    logger.info("更新成功，工号：{}，课程类型：{}", request.getAccount(), request.getTaskType());
                } else {
                    result.put("success", false);
                    result.put("message", "更新失败");
                    return result;
                }
            } else {
                // 4. 不存在则插入
                HandsOnCourse insertEntity = new HandsOnCourse();
                insertEntity.setAccount(request.getAccount().trim());
                insertEntity.setTaskType(request.getTaskType().trim());
                insertEntity.setTaskStatus(request.getTaskStatus().trim());
                // task_info 为选填，未传或为空时插入为空
                insertEntity.setTaskInfo(request.getTaskInfo() != null ? request.getTaskInfo().trim() : null);
                
                int insertCount = handsOnCourseMapper.insert(insertEntity);
                if (insertCount > 0) {
                    action = "insert";
                    logger.info("插入成功，工号：{}，课程类型：{}", request.getAccount(), request.getTaskType());
                } else {
                    result.put("success", false);
                    result.put("message", "插入失败");
                    return result;
                }
            }
            
            // 5. 构建返回结果
            result.put("success", true);
            result.put("message", "同步成功");
            result.put("action", action);
            result.put("account", request.getAccount());
            result.put("taskType", request.getTaskType());
            
            return result;
            
        } catch (Exception e) {
            logger.error("同步实战课程个人完课情况失败，工号：{}，课程类型：{}", request.getAccount(), request.getTaskType(), e);
            result.put("success", false);
            result.put("message", "系统异常：" + e.getMessage());
            throw e;
        }
    }
}
