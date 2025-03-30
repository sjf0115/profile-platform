package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.AttributeMapper;
import com.data.profile.model.Attribute;
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
 * 功能：事件属性服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class AttributeService {
    private static Logger LOG = LoggerFactory.getLogger(AttributeService.class);

    @Resource
    private AttributeMapper attrMapper;

    /**
     * 根据查询条件获取事件属性列表
     * @param attr
     * @return
     */
    public List<Attribute> getList(Attribute attr) {
        List<Attribute> attrs = attrMapper.selectByParams(attr);
        return attrs;
    }

    /**
     * 根据事件属性ID获取事件属性详细信息
     * @param attrId
     * @return
     */
    public Optional<Attribute> getDetail(String attrId) {
        Attribute attr = attrMapper.selectByAttrId(attrId);
        if (attr == null) {
            return Optional.empty();
        }
        return Optional.of(attr);
    }

    /**
     * 保存事件属性 新增/修改
     * @param attr
     * @return
     * @throws RuntimeException
     */
    public int save(Attribute attr) throws RuntimeException {
        if (StringUtils.isBlank(attr.getEntityId())) {
            // 新增
            List<Attribute> attrs = attrMapper.selectByAttrName(attr.getAttrName());
            if (attrs.size() > 0) {
                throw new RuntimeException("属性已经存在，不允许重复添加");
            }
            String attrId = IDGenerator.getInstance().generate(ModelType.ATTRIBUTE);
            Attribute target = attrMapper.selectByAttrId(attrId);
            if (!Objects.equals(target, null)) {
                throw new RuntimeException("属性ID已经存在，不允许重复添加");
            }
            attr.setAttrId(attrId);
            attr.setStatus(Status.ENABLE.getCode());
            attr.setCreator(RequestContext.currentUserId());
            attr.setModifier(RequestContext.currentUserId());
            int result = attrMapper.insertSelective(attr);
            return result;
        } else {
            // 修改
            attr.setModifier(RequestContext.currentUserId());
            int result = attrMapper.updateByAttrIdSelective(attr);
            return result;
        }
    }
}