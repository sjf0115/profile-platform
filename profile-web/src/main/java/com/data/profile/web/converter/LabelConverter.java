package com.data.profile.web.converter;

import com.data.profile.common.enums.SourceType;
import com.data.profile.web.dto.LabelRequest;
import com.data.profile.web.dto.UserRequest;
import com.data.profile.web.model.Label;
import com.data.profile.web.model.User;
import com.data.profile.web.security.UserContextHolder;
import com.data.profile.web.vo.LabelVO;
import com.data.profile.web.vo.UserVO;
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
    public static LabelVO convert(Label label) {
        if (label == null) {
            return null;
        }
        return DO2VoConverterMapper.INSTANCE.convert(label);
    }

    // List<DO> -> List<VO>
    public static List<LabelVO> convertList(List<Label> labels) {
        if (labels == null || labels.isEmpty()) {
            return Collections.emptyList();
        }
        return DO2VoConverterMapper.INSTANCE.convertList(labels);
    }

    // DTO -> DO
    public static Label convert(LabelRequest labelRequest) {
        if (labelRequest == null) {
            return null;
        }
        Label label = DTO2DOConverterMapper.INSTANCE.convert(labelRequest);
        label.setCreator(UserContextHolder.currentUserId());
        label.setModifier(UserContextHolder.currentUserId());
        label.setSourceType(SourceType.CUSTOM.getCode());
        return label;
    }

    // DO -> VO
    @Mapper
    public interface DO2VoConverterMapper extends BaseConverter<Label, LabelVO> {
        DO2VoConverterMapper INSTANCE = Mappers.getMapper(DO2VoConverterMapper.class);
        @Override
        LabelVO convert(Label label);
    }

    // DTO -> DO
    @Mapper
    public interface DTO2DOConverterMapper extends BaseConverter<LabelRequest, Label> {
        DTO2DOConverterMapper INSTANCE = Mappers.getMapper(DTO2DOConverterMapper.class);
        @Override
        Label convert(LabelRequest request);
    }
}
