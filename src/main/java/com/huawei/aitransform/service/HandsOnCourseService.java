package com.huawei.aitransform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.aitransform.entity.EmployeeTrainingInfoPO;
import com.huawei.aitransform.entity.HandsOnCourse;
import com.huawei.aitransform.entity.HandsOnCoursesSyncRequestVO;
import com.huawei.aitransform.mapper.EmployeeTrainingInfoMapper;
import com.huawei.aitransform.mapper.HandsOnCourseMapper;
import com.huawei.aitransform.mapper.PracticalCourseInfoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 实战课程个人完课情况同步服务类
 */
@Service
public class HandsOnCourseService {

    private static final Logger logger = LoggerFactory.getLogger(HandsOnCourseService.class);

    /** 触发同步到训战表的状态值（忽略大小写） */
    private static final String TASK_STATUS_FINISHED = "finished";

    /** 有效员工 user_id 格式：首字母 + 0 开头 + 后续数字 */
    private static final Pattern VALID_USER_ID_PATTERN = Pattern.compile("^[a-zA-Z]0\\d+$");

    @Value("${agent.rank-api-url:http://7.225.29.223:8080/api/rank}")
    private String agentRankApiUrl;

    @Value("${agent.task-type:AI Agent}")
    private String agentTaskType;

    @Value("${agent.pass-score-threshold:800}")
    private int agentPassScoreThreshold;

    @Autowired
    private HandsOnCourseMapper handsOnCourseMapper;

    @Autowired
    private PracticalCourseInfoMapper practicalCourseInfoMapper;

    @Autowired
    private EmployeeTrainingInfoMapper employeeTrainingInfoMapper;

    @Autowired
    private PersonalCreditService personalCreditService;

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
            
            // 5. 当 task_status 为 finished 时，同步更新 t_employee_training_info 的实战完课列表
            if (TASK_STATUS_FINISHED.equalsIgnoreCase(request.getTaskStatus().trim())) {
                syncPracticalCourseToTrainingInfo(request.getAccount().trim(), request.getTaskType().trim(), result);
                schedulePersonalCreditRefreshAfterCommit(request.getAccount().trim(), result);
            }

            // 6. 构建返回结果
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

    /**
     * 在「实战完课同步」事务提交后，增量刷新该员工的个人学分汇总（t_personal_credit）。
     * <p>
     * 关键点：刷新学分失败不应回滚 hands_on_courses 的写入，因此放到 afterCommit 执行。
     */
    private void schedulePersonalCreditRefreshAfterCommit(String account, Map<String, Object> result) {
        if (account == null || account.trim().isEmpty()) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 理论上在 @Transactional 内应为 true；兜底：直接尝试刷新但吞掉异常
            try {
                personalCreditService.syncPersonalCreditsForEmployees(Collections.singleton(account.trim()));
                result.put("personalCreditRefreshed", true);
            } catch (Exception e) {
                logger.warn("实战完课已写入，但个人学分刷新失败（非阻断）：account={}", account, e);
                result.put("personalCreditRefreshed", false);
                result.put("personalCreditRefreshError", e.getMessage());
            }
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    personalCreditService.syncPersonalCreditsForEmployees(Collections.singleton(account.trim()));
                    result.put("personalCreditRefreshed", true);
                } catch (Exception e) {
                    logger.warn("实战完课已提交，但个人学分刷新失败（非阻断）：account={}", account, e);
                    result.put("personalCreditRefreshed", false);
                    result.put("personalCreditRefreshError", e.getMessage());
                }
            }
        });
    }

    /**
     * Agent 实战课程数据同步：调用外部排行榜接口，筛选通过人员，增量写入完课表并刷新学分。
     * <p>
     * 流程：
     * 1. GET 请求外部排行榜接口
     * 2. 过滤有效员工（user_id 匹配「首字母 + 0 开头」格式）
     * 3. 筛选 score >= 阈值的通过人员
     * 4. 与 hands_on_courses 已有记录对比，仅对新增通过人员调用 syncHandsOnCourse
     *
     * @return 同步结果统计
     */
    public Map<String, Object> syncAgentPracticalCourses() {
        logger.info("开始 Agent 实战课程数据同步，排行榜接口：{}，阈值：{}", agentRankApiUrl, agentPassScoreThreshold);

        // 1. 调用外部接口
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(agentRankApiUrl, String.class);
        String body = response.getBody();
        if (body == null || body.trim().isEmpty()) {
            throw new RuntimeException("排行榜接口返回为空");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (Exception e) {
            throw new RuntimeException("排行榜接口返回 JSON 解析失败：" + e.getMessage(), e);
        }

        JsonNode dataNode = root.get("data");
        if (dataNode == null || !dataNode.isArray()) {
            throw new RuntimeException("排行榜接口返回 data 字段缺失或非数组");
        }

        // 2. 过滤有效员工 + 筛选通过人员
        List<Map<String, Object>> passedList = new ArrayList<>();
        int totalFromApi = dataNode.size();
        int filteredValid = 0;

        for (JsonNode item : dataNode) {
            String userId = item.has("user_id") ? item.get("user_id").asText("") : "";
            if (!VALID_USER_ID_PATTERN.matcher(userId).matches()) {
                continue;
            }
            filteredValid++;

            int score = item.has("score") ? item.get("score").asInt(0) : 0;
            if (score < agentPassScoreThreshold) {
                continue;
            }

            String empNum = userId.substring(1);
            Map<String, Object> info = new HashMap<>();
            info.put("empNum", empNum);
            info.put("score", score);
            info.put("username", item.has("username") ? item.get("username").asText("") : "");
            passedList.add(info);
        }

        // 3. 查询已有记录，增量对比
        List<String> existingAccounts = handsOnCourseMapper.selectAccountsByTaskType(agentTaskType);
        Set<String> existingSet = new HashSet<>(existingAccounts != null ? existingAccounts : Collections.emptyList());

        List<String> newlyAdded = new ArrayList<>();
        List<String> alreadyExists = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        for (Map<String, Object> info : passedList) {
            String empNum = (String) info.get("empNum");
            if (existingSet.contains(empNum)) {
                alreadyExists.add(empNum);
                continue;
            }

            // 4. 复用已有 syncHandsOnCourse 逻辑
            HandsOnCoursesSyncRequestVO req = new HandsOnCoursesSyncRequestVO();
            req.setAccount(empNum);
            req.setTaskType(agentTaskType);
            req.setTaskStatus(TASK_STATUS_FINISHED);
            req.setTaskInfo("agent_score:" + info.get("score"));

            try {
                Map<String, Object> syncResult = syncHandsOnCourse(req);
                Boolean success = (Boolean) syncResult.get("success");
                if (success != null && success) {
                    newlyAdded.add(empNum);
                    logger.info("Agent 实战同步新增：empNum={}，score={}", empNum, info.get("score"));
                } else {
                    failed.add(empNum);
                    logger.warn("Agent 实战同步失败：empNum={}，原因={}", empNum, syncResult.get("message"));
                }
            } catch (Exception e) {
                failed.add(empNum);
                logger.error("Agent 实战同步异常：empNum={}", empNum, e);
            }
        }

        logger.info("Agent 实战课程数据同步完成：API 总数={}，有效员工={}，通过={}，新增={}，已存在={}，失败={}",
                totalFromApi, filteredValid, passedList.size(), newlyAdded.size(), alreadyExists.size(), failed.size());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("totalFromApi", totalFromApi);
        result.put("filteredValid", filteredValid);
        result.put("totalPassed", passedList.size());
        result.put("newlyAdded", newlyAdded.size());
        result.put("alreadyExists", alreadyExists.size());
        result.put("failed", failed.size());
        result.put("newlyAddedAccounts", newlyAdded);
        if (!failed.isEmpty()) {
            result.put("failedAccounts", failed);
        }
        return result;
    }

    /**
     * 当 task_status 为 finished 时，将本次完课同步到 t_employee_training_info 的 practical_courses。
     * 根据 task_type 查课程 ID，若该 ID 不在员工完课列表中则追加并更新；已存在则不更新。
     * 未查到课程 ID 或该工号无训战记录时不抛异常，仅不同步训战表。
     *
     * @param account   工号（已 trim）
     * @param taskType  课程类型（已 trim）
     * @param result    当前返回结果，若发生更新可设置 trainingInfoUpdated
     */
    private void syncPracticalCourseToTrainingInfo(String account, String taskType, Map<String, Object> result) {
        try {
            // 1. 根据 task_type 查询课程主键 ID
            Integer courseId = practicalCourseInfoMapper.selectIdByTaskType(taskType);
            if (courseId == null) {
                logger.debug("未找到对应实战课程，task_type：{}，不更新训战完课列表", taskType);
                return;
            }
            String courseIdStr = String.valueOf(courseId);

            // 2. 根据工号查询训战表
            EmployeeTrainingInfoPO trainingInfo = employeeTrainingInfoMapper.selectByEmployeeNumber(account);
            if (trainingInfo == null) {
                logger.debug("训战表中无该工号记录，account：{}，不新增训战记录", account);
                return;
            }

            // 3. 解析 practical_courses（逗号分隔，空串/null 视为空列表）
            String practicalCourses = trainingInfo.getPracticalCourses();
            List<String> idList = new ArrayList<>();
            if (practicalCourses != null && !practicalCourses.trim().isEmpty()) {
                idList = Arrays.stream(practicalCourses.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }

            // 4. 若课程 ID 已在列表中，不更新
            if (idList.contains(courseIdStr)) {
                logger.debug("课程 ID 已在完课列表中，account：{}，courseId：{}", account, courseId);
                return;
            }

            // 5. 追加课程 ID 并写回（逗号分隔，无首尾逗号）
            idList.add(courseIdStr);
            String newPracticalCourses = String.join(",", idList);
            int updated = employeeTrainingInfoMapper.updatePracticalCoursesByEmployeeNumber(account, newPracticalCourses);
            if (updated > 0) {
                result.put("trainingInfoUpdated", true);
                logger.info("已同步更新训战表实战完课列表，工号：{}，课程类型：{}，课程ID：{}", account, taskType, courseId);
            }
        } catch (Exception e) {
            logger.warn("同步训战表实战完课列表异常，工号：{}，课程类型：{}，不影响主流程", account, taskType, e);
            // 不抛异常，保持原有返回成功
        }
    }
}
