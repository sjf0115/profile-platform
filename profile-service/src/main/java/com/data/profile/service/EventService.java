package com.data.profile.service;

import com.data.profile.common.domain.RequestContext;
import com.data.profile.common.enums.ModelType;
import com.data.profile.common.enums.SourceType;
import com.data.profile.common.enums.Status;
import com.data.profile.common.utils.IDGenerator;
import com.data.profile.dao.EventMapper;
import com.data.profile.model.Event;
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
 * 功能：事件服务
 * 作者：SmartSi
 * CSDN博客：https://smartsi.blog.csdn.net/
 * 公众号：大数据生态
 * 日期：2024/7/7 15:44
 */

@Slf4j
@Service
public class EventService {
    private static Logger LOG = LoggerFactory.getLogger(EventService.class);

    @Resource
    private EventMapper eventMapper;

    /**
     * 根据查询条件获取事件列表
     * @param event
     * @return
     */
    public List<Event> getList(Event event) {
        List<Event> events = eventMapper.selectByParams(event);
        return events;
    }

    /**
     * 根据事件ID获取事件详细信息
     * @param eventId
     * @return
     */
    public Optional<Event> getDetail(String eventId) {
        Event event = eventMapper.selectByEventId(eventId);
        if (event == null) {
            return Optional.empty();
        }
        return Optional.of(event);
    }

    /**
     * 保存事件 新增/修改
     * @param event
     * @return
     * @throws RuntimeException
     */
    public int save(Event event) throws RuntimeException {
        if (StringUtils.isBlank(event.getEventId())) {
            // 创建事件
            return createEvent(event);
        } else {
            // 修改事件
            return updateEvent(event);
        }
    }

    /**
     * 创建事件
     * @param event
     * @return
     */
    private int createEvent(Event event) {
        // 事件名称是否唯一
        List<Event> events = eventMapper.selectByEventName(event.getEventName());
        if (events.size() > 0) {
            throw new RuntimeException("事件已经存在，不允许重复添加");
        }
        // 事件ID是否唯一
        String eventId = IDGenerator.getInstance().generate(ModelType.EVENT);
        Event target = eventMapper.selectByEventId(eventId);
        if (!Objects.equals(target, null)) {
            throw new RuntimeException("事件ID已经存在，不允许重复添加");
        }

        event.setEventId(eventId);
        event.setStatus(Status.ENABLE.getCode());
        event.setSourceType(SourceType.CUSTOM.getCode());
        event.setOwner(RequestContext.currentUserId());
        event.setCreator(RequestContext.currentUserId());
        event.setModifier(RequestContext.currentUserId());
        int result = eventMapper.insertSelective(event);
        return result;
    }

    /**
     * 修改事件
     * @param event
     * @return
     */
    private int updateEvent(Event event) {
        event.setModifier(RequestContext.currentUserId());
        int result = eventMapper.updateByEventIdSelective(event);
        return result;
    }
}