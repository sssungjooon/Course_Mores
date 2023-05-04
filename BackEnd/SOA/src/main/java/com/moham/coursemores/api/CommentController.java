package com.moham.coursemores.api;

import com.moham.coursemores.dto.comment.CommentCreateReqDTO;
import com.moham.coursemores.dto.comment.CommentResDTO;
import com.moham.coursemores.dto.comment.CommentUpdateReqDTO;
import com.moham.coursemores.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("comment")
@RequiredArgsConstructor
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    @GetMapping("course/{courseId}/{userId}")
    public ResponseEntity<Map<String, Object>> searchCommentAll(
            @PathVariable Long courseId,
            @PathVariable Long userId,
            @RequestParam int page,
            @RequestParam String sortby) {

        logger.info(">> request : courseId : {}, userId : {}, page : {}, sortby : {}", courseId, userId, page, sortby);

        Map<String, Object> resultMap = new HashMap<>();

        List<CommentResDTO> commentList = commentService.getCommentList(courseId, page, sortby);
        resultMap.put("commentList", commentList);
        logger.info("<< response : commentList={}", commentList);


        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    @GetMapping("{userId}")
    public ResponseEntity<Map<String, Object>> serarchMyCommentList(
            @PathVariable Long userId){
        logger.info(">> request : userId = {}", userId);

        Map<String, Object> resultMap = new HashMap<>();

        List<CommentResDTO> myCommentList = commentService.getMyCommentList(userId);
        resultMap.put("myCommentList", myCommentList);
        logger.info("<< response : myCommentList={}", myCommentList);

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }


    @PostMapping("course/{courseId}/{userId}")
    public ResponseEntity<Void> postComment(
            @PathVariable Long courseId,
            @PathVariable Long userId,
            @RequestPart CommentCreateReqDTO commentCreateReqDTO,
            @RequestPart(required = false) List<MultipartFile> imageList) {
        logger.info(">> request : courseId = {}", courseId);
        logger.info(">> request : commentCreateReqDTO = {}", commentCreateReqDTO);
        logger.info(">> request : imageList= {}", imageList);

        commentService.createComment(courseId, userId, commentCreateReqDTO, imageList);
        logger.info("<< response : none");

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("{commentId}/{userId}")
    public ResponseEntity<Void> putComment(
            @PathVariable Long commentId,
            @PathVariable Long userId,
            @RequestPart CommentUpdateReqDTO commentUpdateReqDTO,
            @RequestPart(required = false) List<MultipartFile> imageList){
        logger.info(">> request : commentId = {}", commentId);
        logger.info(">> request : commentUpdateReqDTO = {}", commentUpdateReqDTO);
        logger.info(">> request : imageList= {}", imageList);

        commentService.updateComment(commentId, userId, commentUpdateReqDTO, imageList);
        logger.info("<< response : none");

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("{commentId}/{userId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @PathVariable Long userId){
        logger.info(">> request : commentId = {}", commentId);

        commentService.deleteComment(commentId, userId);
        logger.info("<< response : none");


        return new ResponseEntity<>(HttpStatus.OK);
    }





}