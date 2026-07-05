package com.data.profile.web.converter;

import com.data.profile.web.model.Label;
import com.data.profile.web.vo.AnalysisLabelVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 功能：群组分析转换器
 * 作者：@SmartSi
 * 博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 */
public class GroupAnalysisConverter {

    private GroupAnalysisConverter() {
        // 静态工具类
    }

    /**
     * Label DO -> AnalysisLabelVO（手动 enrich dataset/category 字段）
     */
    public static AnalysisLabelVO toAnalysisLabelVO(Label label) {
        if (label == null) {
            return new AnalysisLabelVO();
        }
        AnalysisLabelVO vo = DO2VOMapper.INSTANCE.convert(label);
        return vo;
    }

    @Mapper
    public interface DO2VOMapper extends BaseConverter<Label, AnalysisLabelVO> {
        DO2VOMapper INSTANCE = Mappers.getMapper(DO2VOMapper.class);

        @Override
        AnalysisLabelVO convert(Label label);
    }
}
