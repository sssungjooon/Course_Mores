package com.moham.coursemores.service.impl;

import com.moham.coursemores.domain.*;
import com.moham.coursemores.dto.course.*;
import com.moham.coursemores.dto.profile.UserSimpleInfoResDto;
import com.moham.coursemores.repository.*;
import com.moham.coursemores.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseLocationRepository courseLocationRepository;
    private final CourseLocationImageRepository courseLocationImageRepository;
    private final FileUploadService fileUploadService;
    private final RegionRepository regionRepository;
    private final HashtagRepository hashtagRepository;
    private final HashtagOfCourseRepository hashtagOfCourseRepository;
    private final ThemeRepository themeRepository;
    private final ThemeOfCourseRepository themeOfCourseRepository;
    private final InterestRepository interestRepository;

    @Override
    public Page<CoursePreviewResDto> search(Long userId, String word, Long regionId, List<Long> themeIds, int page, String sortby) {
        User user = userRepository.findByIdAndDeleteTimeIsNull(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        // 한 페이지에 보여줄 코스의 수
        final int size = 10;

        // Sort 정렬 기준
        Sort sort = ("latest".equals(sortby) ?
                Sort.by("createTime").descending() :
                Sort.by("likeCount").descending());
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Course> pageCourse = courseRepository.searchAll(word, regionId,themeIds,pageable);
        Page<CoursePreviewResDto> result = pageCourse
                .map(course -> {
                    Region region = course.getCourseLocationList().get(0).getRegion();

//                    Optional<Interest> interest = interestRepository.findByUserIdAndCourseId(user.getId(), course.getId());
                    boolean isInterest = false;
                    for (Interest interest : course.getInterestList()){
                        if(Objects.equals(interest.getUser().getId(), user.getId())){
                            isInterest = interest.isFlag();
                            break;
                        }
                    }

                    return CoursePreviewResDto.builder()
                        .courseId(course.getId())
                        .title(course.getTitle())
                        .content(course.getContent())
                        .people(course.getPeople())
                        .visited(course.isVisited())
                        .likeCount(course.getLikeCount())
                        .commentCount(course.getCommentCount())
                        .mainImage(course.getMainImage())
                        .sido(region.getSido())
                        .gugun(region.getGugun())
                        .locationName(course.getLocationName())
//                        .isInterest(interest.map(Interest::isFlag).orElse(false))
                        .isInterest(isInterest)
                        .build();
                });

        long totalElements = result.getTotalElements();
        System.out.println("size = "+result.getContent().size());
        System.out.println("totalElements = "+totalElements);
        System.out.println("isFirst = "+result.isFirst());
        System.out.println("isLast = "+result.isLast());
        System.out.println("isEmpty = "+result.isEmpty());
        System.out.println("getNumber = "+result.getNumber());

        return result;
    }

    @Override
    @Transactional
    public void increaseViewCount(Long courseId) {
        // 코스 정보 가져오기
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("해당 코스를 찾을 수 없습니다."));
        // 코스 조회수 증가
        course.increaseViewCount();
    }

    @Override
    public CourseInfoResDto getCourseInfo(Long courseId) {
        // 코스 정보 가져오기
        Course course = courseRepository.findByIdAndDeleteTimeIsNull(courseId)
                .orElseThrow(()->new RuntimeException("해당 코스를 찾을 수 없습니다."));
        // 코스 해시태그 이름 가져오기
        List<String> hashtagList = hashtagOfCourseRepository.findByCourseId(courseId)
                .stream()
                .map(hashtagOfCourse -> hashtagOfCourse.getHashtag().getName())
                .collect(Collectors.toList());
        // 코스 테마 id 가져오기
        List<Long> themeIdList = themeOfCourseRepository.findByCourseId(courseId)
                .stream()
                .map(themeOfCourse -> themeOfCourse.getTheme().getId())
                .collect(Collectors.toList());
        // 코스 작성자 정보 가져오기
        User user = userRepository.findByIdAndDeleteTimeIsNull(course.getUser().getId())
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
        // 코스 정보 반환
        return CourseInfoResDto.builder()
                .title(course.getTitle())
                .content(course.getContent())
                .people(course.getPeople())
                .time(course.getTime())
                .visited(course.isVisited())
                .viewCount(course.getViewCount())
                .likeCount(course.getLikeCount())
                .interestCount(course.getInterestCount())
                .mainImage(course.getMainImage())
                .hashtagList(hashtagList)
                .themeIdList(themeIdList)
                .simpleInfoOfWriter(UserSimpleInfoResDto.builder()
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImage())
                        .build())
                .build();
    }

    @Override
    public List<CourseDetailResDto> getCourseDetail(Long courseId) {
        // 해당 코스가 존재하는지 확인
        if(!courseRepository.existsByIdAndDeleteTimeIsNull(courseId))
            throw new RuntimeException("해당 코스를 찾을 수 없습니다.");
        // 코스 정보 반환
        return courseLocationRepository.findByCourseId(courseId)
                .stream()
                .map(courseLocation -> CourseDetailResDto.builder()
                        .name(courseLocation.getName())
                        .title(courseLocation.getTitle())
                        .content(courseLocation.getContent())
                        .latitude(courseLocation.getLatitude())
                        .longitude(courseLocation.getLongitude())
                        .sido(courseLocation.getRegion().getSido())
                        .gugun(courseLocation.getRegion().getGugun())
                        .locationImage(courseLocation.getCourseLocationImageList()
                                .stream()
                                .map(CourseLocationImage::getImage)
                                .collect(Collectors.toList())
                        )
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<MyCourseResDto> getMyCourseList(Long userId) {
        // 유저 정보 가져오기
        User user = userRepository.findByIdAndDeleteTimeIsNull(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
        // 내 코스 목록 list 생성
        List<MyCourseResDto> myCourseResDtoList = new ArrayList<>();
        // 유저의 코스들을 Dto로 가공하여 list에 담기
        user.getCourseList().forEach(course -> {
            // 삭제한 코스인지 확인
            if(course.getDeleteTime()!=null) return;
            // 코스의 첫번째 지역
            Region region = course.getCourseLocationList().get(0).getRegion();
            // 코스를 Dto로 가공하기
            myCourseResDtoList.add(MyCourseResDto.builder()
                    .courseId(course.getId())
                    .title(course.getTitle())
                    .content(course.getContent())
                    .people(course.getPeople())
                    .visited(course.isVisited())
                    .likeCount(course.getLikeCount())
                    .mainImage(course.getMainImage())
                    .sido(region.getSido())
                    .gugun(region.getGugun())
                    .locationName(course.getLocationName())
                    .commentCount(course.getCommentCount())
                    .build());
            });
        // 내 코스 목록 반환
        return myCourseResDtoList;
    }

    @Override
    @Transactional
    public void addCourse(Long userId, CourseCreateReqDto courseCreateReqDto, List<MultipartFile> imageList) {
        // 유저 정보 가져오기
        User user = userRepository.findByIdAndDeleteTimeIsNull(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
        // 코스 생성
        Course course = courseRepository.save(Course.builder()
                .title(courseCreateReqDto.getTitle())
                .content(courseCreateReqDto.getContent())
                .people(courseCreateReqDto.getPeople())
                .time(courseCreateReqDto.getTime())
                .visited(courseCreateReqDto.isVisited())
                .viewCount(0)
                .interestCount(0)
                .likeCount(0)
                .commentCount(0)
                .locationName(courseCreateReqDto.getLocationList().get(0).getName())
                .user(user)
                .build());
        // 코스의 테마 생성
        courseCreateReqDto.getThemeIdList().forEach(themeId -> {
            // 테마 정보 가져오기
            Theme theme = themeRepository.findById(themeId)
                            .orElseThrow(() -> new RuntimeException("해당 테마를 찾을 수 없습니다."));
            // 코스의 테마 저장
            themeOfCourseRepository.save(ThemeOfCourse.builder()
                    .course(course)
                    .theme(theme)
                    .build());
        });
        // 코스의 해시태그 생성
        courseCreateReqDto.getHashtagList().forEach(hashtagName ->{
            Optional<Hashtag> hashtag = hashtagRepository.findByName(hashtagName);
            // 해시태그가 이미 존재한다면
            if(hashtag.isPresent()){
                // 코스의 해시태그 생성
                hashtagOfCourseRepository.save(HashtagOfCourse.builder()
                        .course(course)
                        .hashtag(hashtag.get())
                        .build());
            }
            // 해시태그가 없다면
            else{
                // 해시태그를 생성하고
                Hashtag newHashtag = hashtagRepository.save(Hashtag.builder()
                        .name(hashtagName)
                        .build());
                // 코스의 해시태그 생성
                hashtagOfCourseRepository.save(HashtagOfCourse.builder()
                        .course(course)
                        .hashtag(newHashtag)
                        .build());
            }
        });


        int imageIdx = 0;
        String mainImage = null;
        // 코스의 장소 정보 생성
        for (LocationCreateReqDto location : courseCreateReqDto.getLocationList()) {
            // 코스의 장소의 지역 가져오기
            Region region = regionRepository.findById(location.getRegionId())
                    .orElseThrow(() -> new RuntimeException("해당 지역을 찾을 수 없습니다."));
            // 코스의 장소 저장
            CourseLocation courseLocation = courseLocationRepository.save(CourseLocation.builder()
                    .name(location.getName())
                    .title(location.getTitle())
                    .content(location.getContent())
                    .roadViewImage(location.getRoadViewImage())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .course(course)
                    .region(region)
                    .build());
            // 코스의 장소의 이미지 생성
            for (int end = imageIdx + location.getNumberOfImage(); imageIdx < end; imageIdx++) {
                String imagePath = fileUploadService.uploadImage(imageList.get(imageIdx));
                if (mainImage == null)
                    mainImage = location.getNumberOfImage() == 0 ? location.getRoadViewImage() : imagePath;
                courseLocationImageRepository.save(CourseLocationImage.builder()
                        .image(imagePath)
                        .courseLocation(courseLocation)
                        .build());
            }
        }
        // 코스의 대표 이미지 설정
        course.setMainImage(mainImage);
    }

    @Override
    @Transactional
    public void setCourse(Long userId, Long courseId, CourseUpdateReqDto courseUpdateReqDto, List<MultipartFile> imageList) {
        // 유저 정보 가져오기
        User user = userRepository.findByIdAndDeleteTimeIsNull(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
        // 코스 정보 가져오기
        Course course = courseRepository.findByIdAndUserIdAndDeleteTimeIsNull(courseId, user.getId())
                .orElseThrow(() -> new RuntimeException("해당 코스를 찾을 수 없습니다."));
        // 코스 정보 수정하기
        course.update(courseUpdateReqDto);
        // 기존 해시태그 지우기
        hashtagOfCourseRepository.deleteByCourseId(courseId);
        // 코스 해시태그 생성하기
        courseUpdateReqDto.getHashtagList().forEach(hashtag -> {
            Optional<Hashtag> beforeHashtag = hashtagRepository.findByName(hashtag);
            // 해시태그가 이미 존재한다면
            if(beforeHashtag.isPresent()){
                // 코스의 해시태그 생성
                hashtagOfCourseRepository.save(HashtagOfCourse.builder()
                        .course(course)
                        .hashtag(beforeHashtag.get())
                        .build());
            }
            // 해시태그가 없다면
            else{
                // 해시태그를 생성하고
                Hashtag newHashtag = hashtagRepository.save(Hashtag.builder()
                        .name(hashtag)
                        .build());
                // 코스의 해시태그 생성
                hashtagOfCourseRepository.save(HashtagOfCourse.builder()
                        .course(course)
                        .hashtag(newHashtag)
                        .build());
            }
        });
        // 기존 테마 지우기
        themeOfCourseRepository.deleteByCourseId(courseId);
        // 코스의 테마 생성
        courseUpdateReqDto.getThemeIdList().forEach(themeId -> {
            // 테마 정보 가져오기
            Theme theme = themeRepository.findById(themeId)
                    .orElseThrow(() -> new RuntimeException("해당 테마를 찾을 수 없습니다."));
            // 코스의 테마 저장
            themeOfCourseRepository.save(ThemeOfCourse.builder()
                    .course(course)
                    .theme(theme)
                    .build());
        });


        int imageIdx = 0;
        for(LocationUpdateReqDto updateCourseLocation : courseUpdateReqDto.getLocationList()){
            // 코스 장소 불러오기
            CourseLocation courseLocation = courseLocationRepository.findById(updateCourseLocation.getCourseLocationId())
                    .orElseThrow(() -> new RuntimeException("해당 장소를 찾을 수 없습니다."));
            // 코스 장소 수정하기
            courseLocation.update(updateCourseLocation);
            // 이미지 삭제
            for(long locationImageId : updateCourseLocation.getDeleteImageList()){
                CourseLocationImage courseLocationImage = courseLocationImageRepository.findById(locationImageId)
                        .orElseThrow(() -> new RuntimeException("해당 장소 이미지를 찾을 수 없습니다."));
                courseLocationImageRepository.delete(courseLocationImage);
            }
            // 코스의 장소 이미지 추가 생성
            for (int end = imageIdx + updateCourseLocation.getNumberOfImage(); imageIdx < end; imageIdx++) {
                String imagePath = fileUploadService.uploadImage(imageList.get(imageIdx));
                courseLocationImageRepository.save(CourseLocationImage.builder()
                        .image(imagePath)
                        .courseLocation(courseLocation)
                        .build());
            }
        }
        // 코스의 대표 이미지 재설정
        String mainImage;
        try {
            mainImage = course.getCourseLocationList().get(0).getCourseLocationImageList().get(0).getImage();
        } catch (NullPointerException e){
            mainImage = course.getCourseLocationList().get(0).getRoadViewImage();
        }
        course.setMainImage(mainImage);
    }

    @Override
    @Transactional
    public void deleteCourse(Long userId, Long courseId) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
        // 코스 찾기
        Course course = courseRepository.findByIdAndUserId(courseId,user.getId())
                .orElseThrow(() -> new RuntimeException("해당 코스를 찾을 수 없습니다."));
        // 코스 삭제
        course.delete();
    }
}