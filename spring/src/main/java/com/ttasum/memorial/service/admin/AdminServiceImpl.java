package com.ttasum.memorial.service.admin;

import com.ttasum.memorial.domain.entity.Story;
import com.ttasum.memorial.domain.entity.donationStory.DonationStory;
import com.ttasum.memorial.domain.entity.admin.AdminAuthority;
import com.ttasum.memorial.domain.entity.admin.AdminDepartment;
import com.ttasum.memorial.domain.entity.admin.AdminPosition;
import com.ttasum.memorial.domain.entity.admin.User;
import com.ttasum.memorial.domain.entity.blameText.BlameTextComment;
import com.ttasum.memorial.domain.entity.blameText.BlameTextLetter;
import com.ttasum.memorial.domain.repository.blameText.BlameTextCommentRepository;
import com.ttasum.memorial.domain.repository.donationStory.DonationStoryRepository;
import com.ttasum.memorial.domain.repository.admin.AdminAuthorityRepository;
import com.ttasum.memorial.domain.repository.admin.AdminDepartmentRepository;
import com.ttasum.memorial.domain.repository.admin.AdminEmployeeRepository;
import com.ttasum.memorial.domain.repository.admin.AdminPositionRepository;
import com.ttasum.memorial.domain.repository.blameText.BlameTextLetterRepository;
import com.ttasum.memorial.domain.repository.blameText.BlameTextLetterSentenceRepository;
import com.ttasum.memorial.domain.repository.heavenLetter.HeavenLetterRepository;
import com.ttasum.memorial.domain.type.BoardType;
import com.ttasum.memorial.dto.common.ApiResponse;
import com.ttasum.memorial.dto.blameText.BlameTextCommentDto;
import com.ttasum.memorial.dto.donationStory.response.PageResponse;
import com.ttasum.memorial.dto.UserDto;
import com.ttasum.memorial.dto.blameText.BlameTextLetterDto;
import com.ttasum.memorial.dto.blameText.BlameTextLetterSentenceDto;
import com.ttasum.memorial.exception.donationStory.DonationStoryNotFoundException;
import com.ttasum.memorial.exception.blameText.BlametextNotDefinitionFiltering;
import com.ttasum.memorial.service.donationStory.DonationStoryService;
import com.ttasum.memorial.service.blameText.BlameTextLetterMapper;
import com.ttasum.memorial.service.blameText.BlameTextPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService
        , UserDetailsService {
//    {
    private final AdminEmployeeRepository adminEmployeeRepository;
    private final AdminAuthorityRepository adminAuthorityRepository;
    private final AdminPositionRepository adminPositionRepository;
    private final AdminDepartmentRepository adminDepartmentRepository;
    private final HeavenLetterRepository heavenLetterRepository;

    private final DonationStoryRepository donationStoryRepository;


    private final BCryptPasswordEncoder passwordEncoder;
    private final BlameTextLetterRepository blameTextLetterRepository;

    public static Integer IS_BLAME_LABEL = 1;
    public static Integer IS_NOT_BLAME_LABEL = 0;
    public static Integer IS_DELETE = 1;
    public static Integer IS_NOT_DELETE = 0;
    private final BlameTextLetterSentenceRepository blameTextLetterSentenceRepository;
    private final BlameTextCommentRepository blameTextCommentRepository;

    // 관리자 회원가입 시 아이디 중복 확인
    @Override
    public ResponseEntity<ApiResponse> duplicationId(String id) {
        User isDuplication = adminEmployeeRepository.findAdminEmployeeById(id);

        if (isDuplication == null || isDuplication.getId() == null) {
            return ResponseEntity.ok(new ApiResponse(true, 200, "사용 가능한 ID 입니다."));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, 400, "이미 사용 중인 ID 입니다."));
    }


    @Override
    public ArrayList<AdminAuthority> findAllAuthority() {
        return (ArrayList<AdminAuthority>) adminAuthorityRepository.findAll();
    }

    @Override
    public ArrayList<AdminPosition> findAllPosition() {
        return (ArrayList<AdminPosition>) adminPositionRepository.findAll();
    }

    @Override
    public ArrayList<AdminDepartment> findAllDepartment() {
        return (ArrayList<AdminDepartment>) adminDepartmentRepository.findAll();
    }

    @Transactional
    @Override
    public ResponseEntity<?> signupAdmin(UserDto admin) {
        adminEmployeeRepository.save(this.toEntity(admin));
        return ResponseEntity.ok(new ApiResponse(true,201, "등록되었습니다."));
    }

    @Override
    public User toEntity(UserDto userDto) {
        AdminAuthority adminAuthority = adminAuthorityRepository.findadminAuthorityByAuthorityCode(userDto.getRoles());
        AdminPosition adminPosition = adminPositionRepository.findAdminPositionByPositionCode(userDto.getPosition());
        AdminDepartment adminDepartment = adminDepartmentRepository.findAdminDepartmentById(userDto.getDepartmentCode());
        return User.builder()
                .id(userDto.getId())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .name(userDto.getName())
                .age(userDto.getAge())
                .gender(userDto.getGender())
                .email(userDto.getEmail())
                .phoneNumber(userDto.getPhoneNumber())
                .hireDate(LocalDate.now())
                .activeFlag((byte) 'Y')
                .roles(adminAuthority)
                .position(adminPosition)
                .departmentCode(adminDepartment)
                .build();
    }

    /*   <option value="Default" selected>Default</option>
            <option value="isNotDelete">isNotDelete</option>
            <option value="isDelete">isDelete</option>
            <option value="dateASC">dateASC</option>
            <option value="dateDESC">dateDESC</option>
            <option value="isBlameLabelTrue">isBlameLabelTrue</option>
            <option value="isBlameLabelFalse">isBlameLabelFalse</option>
    * */
    @Transactional(readOnly = true)
    @Override
    public PageResponse<BlameTextLetterDto> getBlameTextLetters(String option, String orderBy, Pageable pageable) {
        Page<BlameTextLetter> page = null;
        if(option == null || option.equals("Default")){
            page = blameTextLetterRepository.findBlameTextLetterByLabelOrderByUpdateTimeDesc(IS_BLAME_LABEL, pageable);
        } else if (option.equals("isDelete")) {
            page = blameTextLetterRepository.findBlameTextLettersByDeleteFlag(IS_DELETE, pageable);
        } else if (option.equals("isNotDelete")) {
            page = blameTextLetterRepository.findBlameTextLettersByDeleteFlag(IS_NOT_DELETE, pageable);
        } else if (option.equals("isBlameLabelTrue")) {
            page = blameTextLetterRepository.findBlameTextLettersByLabel(IS_BLAME_LABEL, pageable);
        } else if (option.equals("isBlameLabelFalse")) {
            page = blameTextLetterRepository.findBlameTextLettersByLabel(IS_NOT_BLAME_LABEL, pageable);
        } else {
            throw new BlametextNotDefinitionFiltering("등록되어 있는 Option이 아님");
        }

        return new PageResponse<>(
                Objects.requireNonNull(page).getContent().stream().map(BlameTextLetterMapper::toBlameTextLetterDto).collect(Collectors.toList()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public ArrayList<BlameTextLetterSentenceDto> getBlameTextLettersSentences(int seq) {
        return blameTextLetterSentenceRepository.getBlameTextLetterSentencesByIdLetterSeqOrderByIdSeq(seq)
                .stream().map(BlameTextLetterMapper::toBlameTextLetterSentenceDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Transactional
    @Override
    public ResponseEntity<?> deleteBlameText(int seq, String boardType) {
        Story story = null;
        if (boardType.equals(BoardType.DONATION.getType())){
            story = donationStoryRepository.findByIdAndDelFlag(seq, "N")
                    .orElseThrow(() -> new DonationStoryNotFoundException(seq));
        } else if (boardType.equals(BoardType.HEAVEN.getType())){
            story = heavenLetterRepository.findByIdAndDelFlag(seq, "N")
                    .orElseThrow(() -> new DonationStoryNotFoundException(seq));
        }

        List<BlameTextLetter> list = blameTextLetterRepository
                .findBlameTextLettersByOriginSeqAndDeleteFlag(Objects.requireNonNull(story).getId(), 0);
        for (BlameTextLetter letter : list) {
            letter.setDeleteFlag(1);
        }

    // 사용자 단도 비공개 처리
        story.setDelFlag("Y");
        return ResponseEntity.ok().body(new ApiResponse(true, 200, "비공개 처리 완료"));
    }

    @Override
    public PageRequest setOrderByOptions(int page, int size, String orderBy) {

        // 정렬
        Sort sort = Sort.by("updateTime");
        if (orderBy == null || orderBy.equals("dateASC")) {
            sort = sort.ascending();
        } else {
            sort = sort.descending();
        }

        return PageRequest.of(page, size, sort);
    }

    @Override
    public PageResponse<BlameTextCommentDto> getBlameTextComment(String filter, String orderBy, Pageable pageable) {
        Page<BlameTextComment> page = blameTextCommentRepository.findBlameTextCommentsByLabel(IS_BLAME_LABEL, pageable);

        return new PageResponse<>(
                page.getContent().stream().map(BlameTextLetterMapper::toBlameTextCommentDto).collect(Collectors.toList()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    // Security 구현 메서드
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        User admin = adminEmployeeRepository.findAdminEmployeeByIdAndActiveFlag(id, (byte) 'Y');
        if (admin == null) {
            throw new UsernameNotFoundException("해당 ID를 가진 사용자를 찾을 수 없습니다: " + id);
        }
        return admin;
    }
}
