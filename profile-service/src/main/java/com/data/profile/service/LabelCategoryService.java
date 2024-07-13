package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.LabelCategoryMapper;
import com.data.profile.model.LabelCategory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 功能：标签类目服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class LabelCategoryService {
    private static Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Resource
    private LabelCategoryMapper categoryMapper;

    // 根据标签类目ID获取类目详细信息
    public Optional<LabelCategory> getDetail(String categoryId) {
        LabelCategory labelCategory = categoryMapper.selectByCategoryId(categoryId);
        if (labelCategory == null) {
            return Optional.empty();
        }
        return Optional.of(labelCategory);
    }

    // 添加标签类目
    public int add(String categoryName, String parentCategoryId) throws RuntimeException {
        List<LabelCategory> categories = categoryMapper.selectByCategoryName(categoryName);
        if (categories.size() > 0) {
            throw new RuntimeException("标签类目已经存在，不允许重复添加");
        }
        // 当前层最大序列号
        Integer maxSeq = categoryMapper.selectMaxSeqByParentId(parentCategoryId);
        // 类目层级
        Integer categoryLevel = categoryMapper.selectLevelByCategoryId(parentCategoryId);
        LabelCategory labelCategory = new LabelCategory();
        labelCategory.setStatus(Status.ENABLE.getCode());
        labelCategory.setCategoryId(IDGenerator.generate(ModelType.LABEL_CATEGORY));
        labelCategory.setCategoryName(categoryName);
        labelCategory.setCategoryLevel(Objects.equals(categoryLevel, null) ? 1 : categoryLevel + 1);
        labelCategory.setParentCategoryId(parentCategoryId);
        labelCategory.setCategorySeq(Objects.equals(maxSeq, null) ? 1 : maxSeq + 1);
        labelCategory.setSourceType(SourceType.CUSTOM.getCode());
        labelCategory.setCreator(RequestContext.currentUserId());
        labelCategory.setModifier(RequestContext.currentUserId());
        int result = categoryMapper.insertSelective(labelCategory);
        return result;
    }

    // 根据标签类目ID删除标签类目
    public int delete(String categoryId) {
        int result = categoryMapper.deleteByCategoryId(categoryId);
        return result;
    }

    // 标签类目名称重命名
    public int rename(String categoryId, String categoryName) throws RuntimeException {
        LabelCategory category = categoryMapper.selectByCategoryId(categoryId);
        if (Objects.equals(category, null) || Objects.equals(category.getCategoryId(), null)) {
            throw new RuntimeException("标签类目不存在，不允许重命名");
        }
        LabelCategory labelCategory = new LabelCategory();
        labelCategory.setCategoryId(categoryId);
        labelCategory.setCategoryName(categoryName);
        labelCategory.setModifier(RequestContext.currentUserId());
        int result = categoryMapper.updateByCategoryIdSelective(labelCategory);
        return result;
    }
}