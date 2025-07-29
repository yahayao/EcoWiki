package com.ecowiki.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecowiki.dto.tag.TagDto;
import com.ecowiki.dto.tag.TagStatisticsDto;
import com.ecowiki.entity.tag.Tag;
import com.ecowiki.repository.TagRepository;

/**
 * 标签服务类
 * 
 * 提供标签相关的业务逻辑处理，包括标签的增删改查、搜索、统计等功能。
 * 负责标签实体与DTO之间的转换，以及标签与文章关联的管理。
 * 
 * @author EcoWiki开发团队
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Transactional
public class TagService {
    
    @Autowired
    private TagRepository tagRepository;
    
    /**
     * 创建新标签
     * @param tagName 标签名称
     * @param description 标签描述（可选）
     * @return 创建的标签DTO
     * @throws RuntimeException 当标签名称已存在时抛出异常
     */
    public TagDto createTag(String tagName, String description) {
        if (tagRepository.existsByTagName(tagName)) {
            throw new RuntimeException("标签名称已存在: " + tagName);
        }
        
        Tag tag = new Tag(tagName, description);
        Tag savedTag = tagRepository.save(tag);
        return convertToDto(savedTag);
    }
    
    /**
     * 根据ID获取标签
     * @param tagId 标签ID
     * @return 标签DTO
     * @throws RuntimeException 当标签不存在时抛出异常
     */
    public TagDto getTagById(Long tagId) {
        Optional<Tag> optionalTag = tagRepository.findById(tagId);
        if (optionalTag.isEmpty()) {
            throw new RuntimeException("标签不存在");
        }
        return convertToDto(optionalTag.get());
    }
    
    /**
     * 根据名称获取标签
     * @param tagName 标签名称
     * @return 标签DTO
     * @throws RuntimeException 当标签不存在时抛出异常
     */
    public TagDto getTagByName(String tagName) {
        Optional<Tag> optionalTag = tagRepository.findByTagName(tagName);
        if (optionalTag.isEmpty()) {
            throw new RuntimeException("标签不存在: " + tagName);
        }
        return convertToDto(optionalTag.get());
    }
    
    /**
     * 更新标签信息
     * @param tagId 标签ID
     * @param tagName 新的标签名称
     * @param description 新的标签描述
     * @return 更新后的标签DTO
     * @throws RuntimeException 当标签不存在或名称已被其他标签使用时抛出异常
     */
    public TagDto updateTag(Long tagId, String tagName, String description) {
        Optional<Tag> optionalTag = tagRepository.findById(tagId);
        if (optionalTag.isEmpty()) {
            throw new RuntimeException("标签不存在");
        }
        
        Tag tag = optionalTag.get();
        
        // 检查新名称是否已被其他标签使用
        if (!tag.getTagName().equals(tagName) && tagRepository.existsByTagName(tagName)) {
            throw new RuntimeException("标签名称已存在: " + tagName);
        }
        
        tag.setTagName(tagName);
        tag.setDescription(description);
        
        Tag savedTag = tagRepository.save(tag);
        return convertToDto(savedTag);
    }
    
    /**
     * 删除标签
     * @param tagId 标签ID
     * @throws RuntimeException 当标签不存在时抛出异常
     */
    public void deleteTag(Long tagId) {
        if (!tagRepository.existsById(tagId)) {
            throw new RuntimeException("标签不存在");
        }
        tagRepository.deleteById(tagId);
    }
    
    /**
     * 获取所有标签
     * @return 标签DTO列表
     */
    public List<TagDto> getAllTags() {
        List<Tag> tags = tagRepository.findAllByOrderByTagNameAsc();
        return tags.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * 分页搜索标签
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @return 标签分页数据
     */
    public Page<TagDto> searchTags(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Tag> tags = tagRepository.findByTagNameContainingIgnoreCase(keyword, pageable);
        return tags.map(this::convertToDto);
    }
    
    /**
     * 获取热门标签
     * @param limit 返回数量限制
     * @return 热门标签DTO列表
     */
    public List<TagDto> getPopularTags(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Tag> tags = tagRepository.findPopularTags(pageable);
        return tags.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * 获取最近创建的标签
     * @param limit 返回数量限制
     * @return 最近创建的标签DTO列表
     */
    public List<TagDto> getRecentTags(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Tag> tags = tagRepository.findByOrderByCreatedTimeDesc(pageable);
        return tags.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * 获取标签统计信息
     * @return 标签统计DTO
     */
    public TagStatisticsDto getTagStatistics() {
        long totalTags = tagRepository.count();
        List<Tag> unusedTags = tagRepository.findUnusedTags();
        long unusedTagCount = unusedTags.size();
        List<Object[]> usageStats = tagRepository.getTagUsageStatistics();
        
        return new TagStatisticsDto(totalTags, unusedTagCount, usageStats);
    }
    
    /**
     * 根据逗号分隔的标签字符串获取或创建标签集合
     * @param tagsString 逗号分隔的标签字符串
     * @return 标签实体集合
     */
    public Set<Tag> parseAndCreateTags(String tagsString) {
        Set<Tag> tags = new HashSet<>();
        
        if (tagsString == null || tagsString.trim().isEmpty()) {
            return tags;
        }
        
        String[] tagNames = tagsString.split(",");
        for (String tagName : tagNames) {
            String trimmedName = tagName.trim();
            if (!trimmedName.isEmpty()) {
                Tag tag = getOrCreateTag(trimmedName);
                tags.add(tag);
            }
        }
        
        return tags;
    }
    
    /**
     * 获取或创建标签（如果不存在则创建）
     * @param tagName 标签名称
     * @return 标签实体
     */
    public Tag getOrCreateTag(String tagName) {
        Optional<Tag> existingTag = tagRepository.findByTagName(tagName);
        if (existingTag.isPresent()) {
            return existingTag.get();
        }
        
        Tag newTag = new Tag(tagName);
        return tagRepository.save(newTag);
    }
    
    /**
     * 获取推荐标签
     * @param articleId 文章ID
     * @param limit 返回数量限制
     * @return 推荐标签DTO列表
     */
    public List<TagDto> getRecommendedTags(Long articleId, int limit) {
        List<Tag> tags = tagRepository.findRecommendedTagsForArticle(articleId, limit);
        return tags.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    /**
     * 删除未使用的标签
     * @return 删除的标签数量
     */
    public int deleteUnusedTags() {
        List<Tag> unusedTags = tagRepository.findUnusedTags();
        tagRepository.deleteAll(unusedTags);
        return unusedTags.size();
    }
    
    /**
     * 将标签实体转换为DTO
     * @param tag 标签实体
     * @return 标签DTO
     */
    private TagDto convertToDto(Tag tag) {
        TagDto dto = new TagDto();
        dto.setTagId(tag.getTagId());
        dto.setTagName(tag.getTagName());
        dto.setDescription(tag.getDescription());
        dto.setCreatedTime(tag.getCreatedTime());
        dto.setArticleCount(tag.getArticleCount());
        return dto;
    }
    
    /**
     * 批量获取标签实体
     * @param tagNames 标签名称列表
     * @return 标签实体列表
     */
    public List<Tag> getTagsByNames(List<String> tagNames) {
        return tagRepository.findByTagNameIn(tagNames);
    }
}
