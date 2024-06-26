package com.qingshan.qsbi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.qingshan.qsbi.mapper.ImageMapper;
import com.qingshan.qsbi.model.entity.Image;
import com.qingshan.qsbi.service.ImageService;
import org.springframework.stereotype.Service;

/**
* @author 罗宇楠
* @description 针对表【image(图片分析表)】的数据库操作Service实现
* @createDate 2023-12-13 22:42:19
*/
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image>
    implements ImageService {

}




