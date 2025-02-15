package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.LabelStatus;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.LabelMapper;
import com.data.profile.model.Label;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：标签服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class LabelService {
    private static Logger LOG = LoggerFactory.getLogger(LabelService.class);

    @Resource
    private LabelMapper labelMapper;

    /**
     * 根据查询条件获取标签列表
     * @param label
     * @return
     */
    public List<Label> getList(Label label) {
        List<Label> labels = labelMapper.selectByParams(label);
        return labels;
    }

    /**
     * 根据标签ID获取标签详细信息
     * @param labelId
     * @return
     */
    public Optional<Label> getDetail(String labelId) {
        Label label = labelMapper.selectByLabelId(labelId);
        if (label == null) {
            return Optional.empty();
        }
        return Optional.of(label);
    }

    /**
     * 保存标签 新增/修改
     * @param label
     * @return
     * @throws RuntimeException
     */
    public int save(Label label) throws RuntimeException {
        if (StringUtils.isBlank(label.getLabelId())) {
            // 新增
            List<Label> labels = labelMapper.selectByLabelName(label.getLabelName());
            if (labels.size() > 0) {
                throw new RuntimeException("标签已经存在，不允许重复添加");
            }
            // ID 后续优化 保证唯一
            String labelId = IDGenerator.generate(ModelType.LABEL);
            Label target = labelMapper.selectByLabelId(labelId);
            if (!Objects.equals(target, null)) {
                throw new RuntimeException("标签ID已经存在，不允许重复添加");
            }
            label.setLabelId(labelId);
            label.setLabelStatus(LabelStatus.ENABLE.getCode());
            label.setSourceType(SourceType.CUSTOM.getCode());
            label.setCreator(RequestContext.currentUserId());
            label.setModifier(RequestContext.currentUserId());
            int result = labelMapper.insertSelective(label);
            return result;
        } else {
            // 修改
            label.setModifier(RequestContext.currentUserId());
            int result = labelMapper.updateByLabelIdSelective(label);
            return result;
        }
    }
}