package com.example.HolidayManager.service;

import com.example.HolidayManager.dto.NotificationRequestDto;
import com.example.HolidayManager.dto.NotificationResponseDto;
import com.example.HolidayManager.entity.*;
import com.example.HolidayManager.mapper.NotificationMapper;
import com.example.HolidayManager.mapper.RequestMapper;
import com.example.HolidayManager.mapper.UserMapper;
import com.example.HolidayManager.repository.NotificationRepository;
import com.example.HolidayManager.repository.RequestRepository;
import com.example.HolidayManager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;






    @Override
    public List<NotificationResponseDto> findAll() {

        return this.notificationMapper.entitiesToDtos(notificationRepository.findAll());
    }




    @Override
    @Transactional
    public List<NotificationResponseDto> findByUserTo(Long userId)
    {
        System.out.println("din service");

        //System.out.println(requestRepository.findAllByHolidayId(userId).isEmpty());

        if(userId==null) return null;
        List<NotificationEntity> entities= this.notificationRepository.findAllByUserToAndStatus(userRepository.findById(userId).get(), NotificationStatus.UNREAD);
        return this.notificationMapper.entitiesToDtos(entities);
    }

    /*add:
someone has accepted/declined/requested a request



add:
daca request id(repository) == null -> request canceled
if (userFrom ==teamLead) --> someone = your teamlead(numele)
    approved/declined
    ne uitam cu holiday id + user id(userTo) in request ca sa vedem daca request is approved or not  your request/
    asked for extra info ------avem status approved,declined,extra_info
 */

    private String getHolidayType(HolidayEntity holiday) {

        String type;

        if (holiday.getRestHoliday() != null) {

            type = "REST";
        }
        else if (holiday.getSpecialHoliday() != null) {

            type = "SPECIAL";
        }
        else {

            type = "UNPAID";
        }

        return type;
    }

    @Override
    public NotificationResponseDto add(NotificationRequestDto dto) {

        if (dto.getUserTo().equals(dto.getUserFrom())) {
            return null;
        }

        NotificationEntity entity = notificationMapper.dtoToEntity(dto);

        if(entity.getUserTo().getUserType()== UserType.TEAM_LEAD)
        {

            String userName= entity.getUserFrom().getFirstName()+" "+entity.getUserFrom().getLastName();
            System.out.println(entity.getUserFrom().getId());
            System.out.println(entity.getHoliday().getId());

            switch (dto.getHolidayType()) {
                case "REST": {
                    entity.setMessage(userName + " has requested a rest holiday");
                    break;
                }
                case "UNPAID": {
                    entity.setMessage(userName + " has requested an unpaid holiday");
                    break;
                }
                case "SPECIAL": {
                    entity.setMessage(userName + " has requested a special holiday");
                    break;
                }
                default: {
                    break;
                }
            }

        }
        else if(entity.getUserTo().getUserType() == UserType.EMPLOYEE)
        {
            String userName = entity.getUserFrom().getFirstName() + " " + entity.getUserFrom().getLastName();
            String status = dto.getRequestStatus().toString();

            if(status.equals(Status.APPROVED.toString()))
            {
                entity.setMessage(userName+" has approved your "+dto.getHolidayType().toLowerCase()+" holiday request");
            }
            else if(status.equals(Status.DECLINED.toString()))
            {
                entity.setMessage(userName+" has declined your "+dto.getHolidayType().toLowerCase()+" holiday request");
            }
            else if(status.equals(Status.EXTRA_INFO.toString()))
            {
                entity.setMessage(userName+" has asked for extra info to your "+dto.getHolidayType().toLowerCase()+" holiday request");
            }
        }

        NotificationEntity result = notificationRepository.save(entity);
        System.out.println(notificationMapper.entityToDto(result));

        return this.notificationMapper.entityToDto(result);
    }

    public NotificationResponseDto add(NotificationResponseDto dto) {
        if (dto != null) {
            NotificationEntity entity = this.notificationMapper.dtoToEntity(dto);
            return this.notificationMapper.entityToDto(this.notificationRepository.save(entity));
        }
        return null;
    }



    @Override
    @Transactional
    public List<NotificationResponseDto> markAsRead(Long userId) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);

        if (optionalUserEntity.isPresent()) {
            UserEntity foundUserEntity = optionalUserEntity.get();
            List<NotificationEntity> entities = this.notificationRepository.findAllByUserToAndStatus(foundUserEntity, NotificationStatus.UNREAD);

            for(NotificationEntity entity : entities) {
                entity.setStatus(NotificationStatus.READ);
            }
            return this.notificationMapper.entitiesToDtos(this.notificationRepository.saveAll(entities));
        }
        return null;
    }


}
