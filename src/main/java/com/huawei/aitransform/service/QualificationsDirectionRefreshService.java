package com.huawei.aitransform.service;

import com.huawei.aitransform.mapper.QualificationsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 任职资格初始方向刷新服务
 */
@Service
public class QualificationsDirectionRefreshService {

    private static final Logger logger = LoggerFactory.getLogger(QualificationsDirectionRefreshService.class);

    @Autowired
    private QualificationsMapper qualificationsMapper;

    /**
     * 刷新 t_qualifications 表中 direction_cn_name 为空的初始任职方向。
     * 当 competence_subcategory_cn 为「AI算法及应用」或「数据科学与AI工程」时，
     * 分别将 direction_cn_name 更新为对应的 ICT 子方向。
     *
     * @return 刷新结果（含各类更新数量）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> refreshInitialQualificationDirection() {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("开始刷新初始任职方向");

            int aiAlgorithmUpdated = qualificationsMapper.refreshAiAlgorithmDirection();
            int dataScienceUpdated = qualificationsMapper.refreshDataScienceDirection();
            int totalUpdated = aiAlgorithmUpdated + dataScienceUpdated;

            result.put("success", true);
            result.put("message", String.format(
                    "刷新完成，共更新 %d 条记录（AI算法及应用：%d 条，数据科学与AI工程：%d 条）",
                    totalUpdated, aiAlgorithmUpdated, dataScienceUpdated));
            result.put("totalUpdated", totalUpdated);
            result.put("aiAlgorithmUpdated", aiAlgorithmUpdated);
            result.put("dataScienceUpdated", dataScienceUpdated);

            logger.info("初始任职方向刷新完成，共更新 {} 条（AI算法及应用：{} 条，数据科学与AI工程：{} 条）",
                    totalUpdated, aiAlgorithmUpdated, dataScienceUpdated);
        } catch (Exception e) {
            logger.error("刷新初始任职方向时发生异常：{}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "刷新失败：" + e.getMessage());
            throw e;
        }

        return result;
    }
}
