package com.huawei.aitransform.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 简单分页结果（列表 + 总数）
 * <p>可选 {@code totalCredits}：个人多元化学分列表等场景返回该工号全量学分合计；其它接口保持 null。
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private List<T> rows;
    /** 可选：学分合计（如手工录入全量 SUM），与 {@link #total}（条数）无关 */
    private BigDecimal totalCredits;

    public PageResult() {
    }

    public PageResult(long total, List<T> rows) {
        this.total = total;
        this.rows = rows == null ? Collections.<T>emptyList() : rows;
    }

    public static <T> PageResult<T> of(long total, List<T> rows) {
        return new PageResult<>(total, rows);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public BigDecimal getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(BigDecimal totalCredits) {
        this.totalCredits = totalCredits;
    }
}
