package com.data.profile.dao;

import com.data.profile.model.Event;

import java.util.List;

public interface EventMapper {
    // 查询
    Event selectByEventId(String eventId); // 根据ID查询

    List<Event> selectByEventName(String eventName); // 根据名字查询

    List<Event> selectByParams(Event event); //根据参数查询

    List<Event> selectByKeyword(String keyword); // 模糊查询

    // 插入
    int insert(Event event);

    int insertSelective(Event event);

    // 删除
    int deleteByEventId(String eventId);

    // 更新
    int updateByEventId(Event event);

    int updateByEventIdSelective(Event event);
}