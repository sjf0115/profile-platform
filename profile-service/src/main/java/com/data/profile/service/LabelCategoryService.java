package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.DefaultIDGenerator;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.LabelCategoryMapper;
import com.data.profile.dao.LabelMapper;
import com.data.profile.model.Label;
import com.data.profile.model.LabelCategory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Resource
    private LabelCategoryMapper categoryMapper;
    @Resource
    private LabelMapper labelMapper;

    /**
     * 根据查询条件获取类目列表
     * @param category
     * @return
     */
    public List<LabelCategory> getList(LabelCategory category) {
        List<LabelCategory> categories = categoryMapper.selectByParams(category);
        return categories;
    }

    /**
     * 根据标签类目ID获取类目详细信息
     * @param categoryId
     * @return
     */
    public Optional<LabelCategory> getDetail(String categoryId) {
        LabelCategory labelCategory = categoryMapper.selectByCategoryId(categoryId);
        if (labelCategory == null) {
            return Optional.empty();
        }
        return Optional.of(labelCategory);
    }

    /**
     * 添加标签类目
     * @param categoryName
     * @param parentCategoryId
     * @return
     * @throws RuntimeException
     */
    public int add(String categoryName, String parentCategoryId) {
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
        labelCategory.setCategoryId(IDGenerator.getInstance().generate(ModelType.LABEL_CATEGORY));
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

    /**
     * 根据标签类目ID删除标签类目
     *      删除之前需要判断类目下是否标签或者子类目
     *          如果包含子类目则不允许删除
     *          如果不包含子类目，但包含标签将原类目中的标签将移入未分类目录下
     *          如果不包含子类目也不包含标签则直接删除
     * @param categoryId
     * @return
     */
    public int delete(String categoryId) {
        // 类目下有子类目不允许删除
        List<LabelCategory> subCategoryList = categoryMapper.selectByParentId(categoryId);
        if (subCategoryList.size() > 0) {
            throw new RuntimeException("该类目下有子类目不允许删除");
        }
        LabelCategory labelCategory = categoryMapper.selectByCategoryId(categoryId);
        if (Objects.equals(labelCategory, null)) {
            throw new RuntimeException("删除类目不存在");
        }
        // 内置目录不允许删除
        if (Objects.equals(labelCategory.getSourceType(), SourceType.BUILT_IN.getCode())) {
            throw new RuntimeException("默认[未分类]类目不允许删除");
        }
        // 类目下标签需要移入未分类目录下
        Label tmpLabel = new Label();
        tmpLabel.setLabelCategoryId(categoryId);
        List<Label> labels = labelMapper.selectByParams(tmpLabel);
        if (labels.size() > 0) {
            // 未分类目录
            List<LabelCategory> categories = categoryMapper.selectByCategoryName(DEFAULT_LABEL_CATEGORY);
            LabelCategory defaultCategory = categories.get(0);
            // 移入未分类目录下
            for (Label label : labels) {
                label.setLabelCategoryId(defaultCategory.getCategoryId());
                label.setModifier(RequestContext.currentUserId());
                labelMapper.updateByLabelIdSelective(label);
            }
        }
        // 删除标签类目
        int result = categoryMapper.deleteByCategoryId(categoryId);
        return result;
    }

    /**
     * 标签类目名称重命名
     * @param categoryId
     * @param categoryName
     * @return
     * @throws RuntimeException
     */
    public int rename(String categoryId, String categoryName) {
        LabelCategory category = categoryMapper.selectByCategoryId(categoryId);
        if (Objects.equals(category, null)) {
            throw new RuntimeException("标签类目不存在，不允许重命名");
        }
        category.setCategoryName(categoryName);
        category.setModifier(RequestContext.currentUserId());
        int result = categoryMapper.updateByCategoryIdSelective(category);
        return result;
    }
}