package com.data.profile.web.converter;

import com.data.profile.common.enums.SourceType;
import com.data.profile.web.dto.LabelParam;
import com.data.profile.web.dto.LabelRequest;
import com.data.profile.web.model.Label;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.web.vo.LabelVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * 功能：标签转换器
 * 作者：SmartSi
 * 日期：2026/3/14
 */
public class LabelConverter {

    private LabelConverter() {
        // 静态工具类
    }

    // DO -> VO
    public static LabelVO do2vo(Label label) {
        if (label == null) {
            return null;
        }
        return DO2VoConverterMapper.INSTANCE.convert(label);
    }

    // List<DO> -> List<VO>
    public static List<LabelVO> do2voList(List<Label> labels) {
        if (labels == null || labels.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2VoConverterMapper.INSTANCE.convertList(labels);
    }

    // Request -> DO
    public static Label request2do(LabelRequest labelRequest) {
        if (labelRequest == null) {
            return null;
        }
        Label label = Request2DOMapper.INSTANCE.convert(labelRequest);
        label.setCreator(UserContextHolder.currentUserId());
        label.setModifier(UserContextHolder.currentUserId());
        label.setSourceType(SourceType.CUSTOM.getCode());
        return label;
    }

    // Param -> DO
    public static Label param2do(LabelParam param) {
        if (param == null) {
            return null;
        }
        return Param2DOMapper.INSTANCE.convert(param);
    }

    // DO -> VO
    @Mapper
    public interface DO2VoConverterMapper extends BaseConverter<Label, LabelVO> {
        DO2VoConverterMapper INSTANCE = Mappers.getMapper(DO2VoConverterMapper.class);
        @Override
        LabelVO convert(Label label);
    }

    // Request -> DO
    @Mapper
    public interface Request2DOMapper extends BaseConverter<LabelRequest, Label> {
        Request2DOMapper INSTANCE = Mappers.getMapper(Request2DOMapper.class);
        @Override
        Label convert(LabelRequest request);
    }

    // Param -> DO
    @Mapper
    public interface Param2DOMapper extends BaseConverter<LabelParam, Label> {
        Param2DOMapper INSTANCE = Mappers.getMapper(Param2DOMapper.class);
        @Override
        Label convert(LabelParam param);
    }
}
