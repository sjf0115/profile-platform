package com.data.profile.web.service;

import com.data.profile.web.dao.LabelCategoryMapper;
import com.data.profile.web.dao.LabelMapper;
import com.data.profile.web.model.Label;
import com.data.profile.web.model.LabelCategory;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.data.profile.common.domain.Constant.DEFAULT_LABEL_CATEGORY;

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
    private static final Gson gson = new GsonBuilder().create();
    @Resource
    private LabelCategoryMapper categoryMapper;
    @Resource
    private LabelMapper labelMapper;

    /**
     * 根据查询条件获取类目列表
     * @param category 标签类目
     */
    public List<LabelCategory> getList(LabelCategory category) {
        List<LabelCategory> categories = categoryMapper.selectByParams(category);
        log.info("根据查询条件获取 {} 个标签类目： {}", categories.size(), gson.toJson(categories));
        return categories;
    }

    /**
     * 根据标签类目ID获取类目详细信息
     * @param categoryId 标签类目ID
     */
    public Optional<LabelCategory> getDetail(String categoryId) {
        LabelCategory labelCategory = categoryMapper.selectByCategoryId(categoryId);
        if (labelCategory == null) {
            return Optional.empty();
        }
        log.info("根据标签类目ID {} 获取类目详细信息：{}", categoryId, gson.toJson(labelCategory));
        return Optional.of(labelCategory);
    }

    /**
     * 添加标签类目
     * @param categoryName 类目ID
     * @param parentCategoryId 父类目ID
     */
    public int add(String categoryName, String parentCategoryId) {
        LabelCategory category = LabelCategory.builder().categoryName(categoryName).parentCategoryId(parentCategoryId).build();
        List<LabelCategory> categories = categoryMapper.selectByParams(category);
        if (!categories.isEmpty()) {
            log.error("标签类目 {} 已经存在，不允许重复添加", gson.toJson(categoryName));
            throw new RuntimeException("标签类目已经存在，不允许重复添加");
        }
        // 当前层最大序列号
        Integer maxSeq = categoryMapper.selectMaxSeqByParentId(parentCategoryId);
        // 类目层级
        Integer categoryLevel = categoryMapper.selectLevelByCategoryId(parentCategoryId);
        category.setStatus(Status.ENABLE.getCode());
        category.setCategoryId(IDGenerator.getInstance().generate(ModelType.LABEL_CATEGORY));
        category.setCategoryLevel(Objects.equals(categoryLevel, null) ? 1 : categoryLevel + 1);
        category.setCategorySeq(Objects.equals(maxSeq, null) ? 1 : maxSeq + 1);
        category.setSourceType(SourceType.CUSTOM.getCode());
        category.setCreator(UserContextHolder.currentUserId());
        category.setModifier(UserContextHolder.currentUserId());
        log.info("添加标签类目：{}", gson.toJson(category));
        return categoryMapper.insertSelective(category);
    }

    /**
     * 根据标签类目ID删除标签类目
     *      删除之前需要判断类目下是否标签或者子类目
     *          如果包含子类目则不允许删除
     *          如果不包含子类目，但包含标签将原类目中的标签将移入未分类目录下
     *          如果不包含子类目也不包含标签则直接删除
     * @param categoryId 标签类目ID
     */
    public int delete(String categoryId) {
        // 类目下有子类目不允许删除
        List<LabelCategory> subCategoryList = categoryMapper.selectByParentId(categoryId);
        if (!subCategoryList.isEmpty()) {
            log.error("该类目下有子类目不允许删除：{}", gson.toJson(subCategoryList));
            throw new RuntimeException("该类目下有子类目不允许删除");
        }
        LabelCategory labelCategory = categoryMapper.selectByCategoryId(categoryId);
        if (Objects.equals(labelCategory, null)) {
            log.error("删除类目不存在：{}", gson.toJson(labelCategory));
            throw new RuntimeException("删除类目不存在");
        }
        // 内置类目不允许删除
        if (Objects.equals(labelCategory.getSourceType(), SourceType.BUILT_IN.getCode())) {
            log.error("默认[未分类]类目不允许删除");
            throw new RuntimeException("默认[未分类]类目不允许删除");
        }
        // 类目下标签需要移入未分类目录下
        Label tmpLabel = new Label();
        tmpLabel.setLabelCategoryId(categoryId);
        List<Label> labels = labelMapper.selectByParams(tmpLabel);
        if (!labels.isEmpty()) {
            // 未分类目录
            List<LabelCategory> categories = categoryMapper.selectByCategoryName(DEFAULT_LABEL_CATEGORY);
            LabelCategory defaultCategory = categories.get(0);
            // 移入未分类目录下
            for (Label label : labels) {
                label.setLabelCategoryId(defaultCategory.getCategoryId());
                label.setModifier(UserContextHolder.currentUserId());
                labelMapper.updateByLabelIdSelective(label);
            }
        }
        // 删除标签类目
        log.info("删除标签类目：{}", categoryId);
        return categoryMapper.deleteByCategoryId(categoryId);
    }

    /**
     * 标签类目名称重命名
     * @param categoryId 标签类目ID
     * @param categoryName 标签类目名称
     */
    public int rename(String categoryId, String categoryName) {
        LabelCategory category = categoryMapper.selectByCategoryId(categoryId);
        if (Objects.equals(category, null)) {
            log.error("标签类目 {} 不存在，不允许重命名", categoryId);
            throw new RuntimeException("标签类目不存在，不允许重命名");
        }
        category.setCategoryName(categoryName);
        category.setModifier(UserContextHolder.currentUserId());
        log.info("标签类目 {} 重命名为 {}", categoryId, categoryName);
        return categoryMapper.updateByCategoryIdSelective(category);
    }
}