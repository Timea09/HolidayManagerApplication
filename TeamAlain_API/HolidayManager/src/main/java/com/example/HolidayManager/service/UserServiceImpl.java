package com.example.HolidayManager.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.example.HolidayManager.dto.*;
import com.example.HolidayManager.entity.*;
import com.example.HolidayManager.mapper.UserMapper;
import com.example.HolidayManager.repository.RequestRepository;
import com.example.HolidayManager.repository.TeamRepository;
import com.example.HolidayManager.repository.UserRepository;
import com.example.HolidayManager.util.exceptions.UserNotFoundException;
import com.example.HolidayManager.util.exceptions.UserPasswordNoMatchException;
import com.itextpdf.text.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.ListItem;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.*;
import javax.transaction.Transactional;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    private final RequestRepository requestRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder encoder;

    private final AmazonSimpleEmailService amazonSimpleEmailService;

//    @EventListener(ApplicationReadyEvent.class)
//    private void createAdmin(){
//        if(userRepository.findByUserType(UserType.ADMIN)==null){
//            UserEntity admin = new UserEntity();
//            admin.setFirstName("admin");
//            admin.setLastName("");
//            admin.setEmail("admin@mhp.com");
//            admin.setPassword(encoder.encode("admin"));
//            admin.setUserType(UserType.ADMIN);
//            admin.setDepartment("");
//            userRepository.save(admin);
//        }
//    }

    @Override
    public List<UserDto> findAll() {
        List<UserEntity> users= userRepository.findAll();
        users.removeIf(u -> u.getUserType().equals(UserType.ADMIN));
        return userMapper.entitiesToDtos(users);
    }

    //method used by UserMapper to map id to entity
    @Override
    public UserEntity getById(Long id) {
        return userRepository.getById(id);
    }

    //method used by UserMapper to map id to entity
    @Override
    public UserEntity getById(UserIdDto userIdDto) {
        return userRepository.getById(userIdDto.getId());
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        UserEntity entity = userMapper.dtoToEntity(userDto);
        if(userRepository.findByEmail(entity.getEmail())==null) {
            entity.setUserType(UserType.EMPLOYEE);//every user is by default employee, except the admin which is created
            //from the database
            entity.setPassword(encoder.encode(entity.getPassword()));//transform the password into a hashed password
            if(userDto.getTeamId()!=null) {
                teamRepository.findById(userDto.getTeamId()).ifPresent(entity::setTeam);
            }
            UserEntity createdEntity = userRepository.save(entity);
            System.out.println(createdEntity);
            return userMapper.entityToDto(createdEntity);


        }
        return null;
    }

    @Override
    @Transactional
    public UserDto deleteUser(Long userID) {

        Optional<UserEntity> optionalUserEntity = userRepository.findById(userID);


        if (optionalUserEntity.isPresent()) {
            if(optionalUserEntity.get().getUserType()!= UserType.TEAM_LEAD)
            {
                requestRepository.deleteByUserId(userID);
                userRepository.deleteById(userID);
                System.out.println(optionalUserEntity.get());
                return userMapper.entityToDto(optionalUserEntity.get());
            }
            else
            {
                return null;
            }

        }

        return null;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {

        UserEntity entity = userMapper.dtoToEntity(userDto);

        if (userRepository.findById(entity.getId()).isPresent()) {

            if (userDto.getTeamId() != null)
                teamRepository.findById(userDto.getTeamId()).ifPresent(entity::setTeam);

            UserEntity updatedEntity = userRepository.save(entity);

            return userMapper.entityToDto(updatedEntity);
        }

        return null;
    }

    @Override
    @Transactional
    public DaysLeftDto getDaysLeft(Long userId) {

        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);

        DaysLeftDto daysLeftDto = new DaysLeftDto();

        optionalUserEntity.ifPresent(
                e -> {

                    daysLeftDto.setDaysLeft(e.getDaysLeft());

                    AtomicInteger unpaidHolidaysSum = new AtomicInteger(0);

                    requestRepository.findApprovedOrPendingByUserId(e.getId()).forEach(

                              r -> {

                                HolidayEntity holiday = r.getHoliday();

                                if (holiday.getRestHoliday() != null || holiday.getSpecialHoliday() != null) {

                                    Duration duration = Duration.between(holiday.getStartDate(), holiday.getEndDate());

                                    daysLeftDto.setDaysLeft((int) (daysLeftDto.getDaysLeft() - duration.toDays() - 1));
                                }

                                if (holiday.getUnpaidHoliday() != null) {

                                    Duration duration = Duration.between(holiday.getStartDate(), holiday.getEndDate());

                                    unpaidHolidaysSum.set(unpaidHolidaysSum.intValue() + (int)duration.toDays() + 1);

                                    //daysLeftDto.setDaysLeft((int) (daysLeftDto.getDaysLeft() - (duration.toDays() / 10)));

                                }
                            }
                    );
                    daysLeftDto.setDaysLeft((int) (daysLeftDto.getDaysLeft() - unpaidHolidaysSum.intValue() / 10));
                }
        );

        return daysLeftDto;
    }


    public List<UserResponseDto> getAllUsersWithNoTeam() {

        for(UserEntity u:userRepository.findByTeamIdIsNullAndUserTypeNot(UserType.ADMIN))
            System.out.println(u.toString());
        return userMapper.responseEntitiesToDtos(userRepository.findByTeamIdIsNullAndUserTypeNot(UserType.ADMIN));
    }

    @Override
    public void changeUserPassword(Long userId, UserChangePasswordDto userChangePasswordDto) throws UserPasswordNoMatchException, UserNotFoundException {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if(optionalUserEntity.isPresent()){
            String oldPassword = userChangePasswordDto.getOldPassword();
            String newPassword = userChangePasswordDto.getNewPassword();
            UserEntity userEntity = optionalUserEntity.get();
            if(encoder.matches(oldPassword,userEntity.getPassword())){
                userEntity.setPassword(encoder.encode(newPassword));
                userRepository.save(userEntity);
            }
            else{
                throw new UserPasswordNoMatchException();
            }
        }
        else{
            throw new UserNotFoundException();
        }
    }

    @Transactional
    @Override
    public ReportDto generateTeamLeadPdfReport(Long userId) throws FileNotFoundException {
        UserEntity teamLeadUser = userRepository.getById(userId);
        TeamEntity team = teamLeadUser.getTeam();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        try{
            PdfWriter.getInstance(document,byteArrayOutputStream);
            document.open();
            Paragraph preface = new Paragraph();
            addEmptyLine(preface,2);
            document.add(preface);

            Paragraph emptyLiner = new Paragraph();
            addEmptyLine(emptyLiner,4);

            Paragraph oneEmptyLiner = new Paragraph();
            addEmptyLine(oneEmptyLiner,1);

            Font fontTitle = new Font();
            fontTitle.setStyle(Font.BOLD);
            fontTitle.setSize(18);

            Font fontSubTitle = new Font();
            fontSubTitle.setStyle(Font.BOLD);
            fontSubTitle.setSize(15);

            Font headerTableFont = new Font();
            headerTableFont.setStyle(Font.BOLD);


            Paragraph mainTitle = new Paragraph();
            mainTitle.setFont(fontTitle);
            mainTitle.add("Team Holiday Status Report");
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(mainTitle);

            document.add(emptyLiner);

            Paragraph subTitleTeamComposition = new Paragraph();
            subTitleTeamComposition.setFont(fontSubTitle);
            subTitleTeamComposition.add("Team Composition");
            subTitleTeamComposition.setAlignment(Element.ALIGN_LEFT);
            document.add(subTitleTeamComposition);

            document.add(oneEmptyLiner);

            PdfPTable table = new PdfPTable(4);


            PdfPCell firstNameHeader = new PdfPCell(new Phrase("First Name",headerTableFont));
            firstNameHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
            firstNameHeader.setVerticalAlignment(Element.ALIGN_CENTER);
            firstNameHeader.setBackgroundColor(new BaseColor(62,85,106));
            table.addCell(firstNameHeader);

            PdfPCell lastNameHeader = new PdfPCell(new Phrase("Last Name",headerTableFont));
            lastNameHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
            lastNameHeader.setVerticalAlignment(Element.ALIGN_CENTER);
            lastNameHeader.setBackgroundColor(new BaseColor(62,85,106));
            table.addCell(lastNameHeader);

            PdfPCell roleHeader = new PdfPCell(new Phrase("Role",headerTableFont));
            roleHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
            roleHeader.setBackgroundColor(new BaseColor(62,85,106));
            table.addCell(roleHeader);

            PdfPCell daysLeftHeader = new PdfPCell(new Phrase("No. Days Left",headerTableFont));
            daysLeftHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
            daysLeftHeader.setVerticalAlignment(Element.ALIGN_BASELINE);
            daysLeftHeader.setBackgroundColor(new BaseColor(62,85,106));
            table.addCell(daysLeftHeader);

            for(UserEntity teamMember:team.getTeamMembers()){
                PdfPCell infoFirstName = new PdfPCell(new Phrase(teamMember.getFirstName()));
                infoFirstName.setBackgroundColor(new BaseColor(231,230,230));
                table.addCell(infoFirstName);

                PdfPCell infoLastName = new PdfPCell(new Phrase(teamMember.getLastName()));
                infoLastName.setBackgroundColor(new BaseColor(231,230,230));
                table.addCell(infoLastName);

                PdfPCell infoUserType = new PdfPCell(new Phrase(teamMember.getUserType().toString()));
                infoUserType.setBackgroundColor(new BaseColor(231,230,230));
                table.addCell(infoUserType);

                PdfPCell infoDaysLeft = new PdfPCell(new Phrase(getDaysLeft(teamMember.getId()).getDaysLeft().toString()));
                infoDaysLeft.setBackgroundColor(new BaseColor(231,230,230));
                table.addCell(infoDaysLeft);

            }

            document.add(table);
            document.add(oneEmptyLiner);

            Paragraph teamMembersRequestParagraph = new Paragraph();
            teamMembersRequestParagraph.setFont(fontSubTitle);
            teamMembersRequestParagraph.add("Team Members Requests");
            document.add(teamMembersRequestParagraph);
            document.add(oneEmptyLiner);
            com.itextpdf.text.List ordered = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
            ordered.setListSymbol("•");
            for ( UserEntity teamMember : team.getTeamMembers()) {

                if(!this.requestRepository.findByUserIdAndStatusNot(teamMember.getId(), Status.DECLINED).isEmpty()) {

                    Paragraph requestsTableParagraph = new Paragraph(" " + teamMember.getFirstName() + " "+teamMember.getLastName());
                    List<RequestEntity> membersRequestList = this.requestRepository.findByUserIdAndStatusNot(teamMember.getId(), Status.DECLINED);

                    PdfPTable tableRequests = new PdfPTable(4);

                    PdfPCell startDateHeader = new PdfPCell(new Phrase("Start Date", headerTableFont));
                    startDateHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
                    startDateHeader.setVerticalAlignment(Element.ALIGN_CENTER);
                    startDateHeader.setBackgroundColor(new BaseColor(62, 85, 106));
                    tableRequests.addCell(startDateHeader);

                    PdfPCell endDateHeader = new PdfPCell(new Phrase("End Date", headerTableFont));
                    endDateHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
                    endDateHeader.setVerticalAlignment(Element.ALIGN_CENTER);
                    endDateHeader.setBackgroundColor(new BaseColor(62, 85, 106));
                    tableRequests.addCell(endDateHeader);

                    PdfPCell holidayTypeHeader = new PdfPCell(new Phrase("Holiday Type", headerTableFont));
                    holidayTypeHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
                    holidayTypeHeader.setVerticalAlignment(Element.ALIGN_BASELINE);
                    holidayTypeHeader.setBackgroundColor(new BaseColor(62, 85, 106));
                    tableRequests.addCell(holidayTypeHeader);

                    PdfPCell statusHeader = new PdfPCell(new Phrase("Status", headerTableFont));
                    statusHeader.setHorizontalAlignment(Element.ALIGN_LEFT);
                    statusHeader.setBackgroundColor(new BaseColor(62, 85, 106));
                    tableRequests.addCell(statusHeader);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("dd.MM.yyyy");

                    for (RequestEntity request : membersRequestList) {
                        PdfPCell infoStartDate = new PdfPCell(new Phrase(request.getHoliday().getStartDate().format(formatter)));
                        infoStartDate.setBackgroundColor(new BaseColor(231,230,230));
                        tableRequests.addCell(infoStartDate);

                        PdfPCell infoEndDate = new PdfPCell(new Phrase(request.getHoliday().getEndDate().format(formatter)));
                        infoEndDate.setBackgroundColor(new BaseColor(231,230,230));
                        tableRequests.addCell(infoEndDate);

                        HolidayEntity holiday = request.getHoliday();
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
                        PdfPCell infoHolidayType = new PdfPCell(new Phrase(type));
                        infoHolidayType.setBackgroundColor(new BaseColor(231,230,230));
                        tableRequests.addCell(infoHolidayType);

                        PdfPCell infoStatus = new PdfPCell(new Phrase(request.getStatus().toString()));
                        infoStatus.setBackgroundColor(new BaseColor(231,230,230));
                        tableRequests.addCell(infoStatus);
                    }

                    requestsTableParagraph.add(tableRequests);
                    ListItem newItem = new ListItem(requestsTableParagraph);
                    newItem.add(oneEmptyLiner);

                    ordered.add(newItem);

                }

            }
            for (UserEntity teamMember : team.getTeamMembers()) {
                if( this.requestRepository.findByUserIdAndStatusNot(teamMember.getId(), Status.DECLINED).isEmpty()) {
                    ListItem newItem = new ListItem(" " + teamMember.getFirstName() + " "+teamMember.getLastName() + " – Does not have any requests.");
                    newItem.add(oneEmptyLiner);
                    ordered.add(newItem);
                }
            }
            document.add(ordered);

            document.close();

            ReportDto reportDto = new ReportDto();
            reportDto.setReport(byteArrayOutputStream.toByteArray());

            return reportDto;
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }

    private DocumentDto generateUnpaidLeaveDocument(UserEntity userEntity, RequestEntity requestEntity) throws InvalidFormatException, IOException, DocumentException {


        Document document = new Document();
        document.setMargins(70, 70, 30, 30);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


        try {
            PdfWriter.getInstance(document,byteArrayOutputStream);
            document.open();

            Font helveticaFont = FontFactory.getFont("src/main/resources/fonts/Helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            //helveticaFont.setSize(11.5F);

            Font underlineHelveticaFont = FontFactory.getFont("src/main/resources/fonts/Helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            underlineHelveticaFont.setStyle(Font.UNDERLINE);

            Font fontBold = FontFactory.getFont("src/main/resources/fonts/Helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            fontBold.setStyle(Font.BOLD);
            fontBold.setSize(11.5F);

            String blankSpace="     ";

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("dd.MM.yyyy");

            Paragraph oneEmptyLiner = new Paragraph();
            addEmptyLine(oneEmptyLiner,1);

            Paragraph twoEmptyLiner = new Paragraph();
            addEmptyLine(twoEmptyLiner,2);

            Paragraph threeEmptyLiner = new Paragraph();
            addEmptyLine(threeEmptyLiner,3);


            Image imgSoc = Image.getInstance("classpath:Img/logo.jpg");
            imgSoc.scalePercent(15,15);
            imgSoc.setAbsolutePosition(425, 730);
            document.add(imgSoc);


            document.add(threeEmptyLiner);
            document.add(twoEmptyLiner);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(0f);
            table.setSpacingAfter(0f);

            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBorder(Rectangle. NO_BORDER);
            emptyCell.setFixedHeight(27);

            PdfPCell cell1 = new PdfPCell();
            cell1.setBorder(Rectangle. NO_BORDER);
            cell1.setFixedHeight(27);

            PdfPCell cell2 = new PdfPCell();
            cell2.setBorder(Rectangle. NO_BORDER);
            cell2.setFixedHeight(27);
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell2.setPaddingRight(0f);

            cell1.setPhrase(new Phrase("MHP Consulting Romania SRL",helveticaFont));
            table.addCell(cell1);

            table.addCell(emptyCell);

            cell1.setPhrase(new Phrase("Strada Onisifor Ghibu, Nr. 20A",helveticaFont));
            table.addCell(cell1);

            table.addCell(emptyCell);

            cell1.setPhrase(new Phrase("Jud. Cluj",helveticaFont));
            table.addCell(cell1);

            cell2.setPhrase(new Phrase("Se aprobă",helveticaFont));
            table.addCell(cell2);

            cell1.setPhrase(new Phrase("Nr. întreg...../........",helveticaFont));
            table.addCell(cell1);

            cell2.setPhrase(new Phrase("Nume/Prenume",helveticaFont));
            table.addCell(cell2);
            //scrii numele dupa

            table.addCell(emptyCell);

            cell1.setPhrase(new Phrase(blankSpace + userEntity.getTeam().getTeamLead().getFirstName() + " " +userEntity.getTeam().getTeamLead().getLastName() + blankSpace));
            table.addCell(cell1);

            table.addCell(emptyCell);

            cell2.setPhrase(new Phrase("Semnătura...............................",helveticaFont));
            table.addCell(cell2);


            Paragraph tablePara = new Paragraph();
            tablePara.add(table);
            document.add(tablePara);

            document.add(threeEmptyLiner);
            document.add(twoEmptyLiner);

            Paragraph title1 = new Paragraph();
            title1.setFont(helveticaFont);
            title1.setAlignment(Element.ALIGN_CENTER);
            title1.add("Către conducerea MHP CONSULTING ROMÂNIA SRL");
            document.add(title1);

            document.add(twoEmptyLiner);

            Paragraph firstPara = new Paragraph();
            firstPara.setFont(helveticaFont);
            firstPara.setMultipliedLeading(1.55f);
            firstPara.setAlignment(Element.ALIGN_JUSTIFIED);
            firstPara.setFont(helveticaFont);
            Chunk firstParaChunk1=new Chunk("Subsemnatul");
            firstParaChunk1.setFont(helveticaFont);
            Chunk firstParaChunk2=new Chunk(blankSpace+userEntity.getFirstName()+" "+userEntity.getLastName()+blankSpace);
            firstParaChunk2.setUnderline(0.5F,-1);
            Chunk firstParaChunk3=new Chunk(", angajat a MHP Consulting Romania SRL,în functia de");
            firstParaChunk3.setFont(helveticaFont);
            Chunk firstParaChunk4=new Chunk(blankSpace+userEntity.getRole().toString()+blankSpace);
            firstParaChunk4.setUnderline(0.5F,-1);
            Chunk firstParaChunk5 = new Chunk("vă rog să îmi aprobati cererea de concediu fără plata pentru studii/ scop personal, în perioada");
            firstParaChunk5.setFont(helveticaFont);
            Chunk firstParaChunk6 = new Chunk(blankSpace+requestEntity.getHoliday().getStartDate().format(formatter)+" - "+requestEntity.getHoliday().getEndDate().format(formatter)+blankSpace);
            firstParaChunk6.setUnderline(0.5F,-1);
            firstPara.add(firstParaChunk1);
            firstPara.add(firstParaChunk2);
            firstPara.add(firstParaChunk3);
            firstPara.add(firstParaChunk4);
            firstPara.add(firstParaChunk5);
            firstPara.add(firstParaChunk6);
            document.add(firstPara);


            Paragraph secondPara = new Paragraph();
            secondPara.setMultipliedLeading(1.55f);
            secondPara.setFont(fontBold);


            secondPara.add("Declar pe proprie răspundere că managerul de proiect a fost informat despre intentia de a pleca în concediu.");
            document.add(secondPara);

            document.add(threeEmptyLiner);

            PdfPTable tableBottom = new PdfPTable(2);
            tableBottom.setWidthPercentage(100);
            tableBottom.setSpacingBefore(0f);
            tableBottom.setSpacingAfter(0f);

            Chunk dataPhraseChunk = new Chunk(blankSpace+ LocalDate.now().format(formatter)+blankSpace);
            cell1.setPhrase(new Phrase("Data:"+dataPhraseChunk,helveticaFont));
            tableBottom.addCell(cell1);

            cell2.setPhrase(new Phrase("Angajat",helveticaFont));
            tableBottom.addCell(cell2);

            tableBottom.addCell(emptyCell);

            cell2.setPhrase(new Phrase("Nume si prenume",helveticaFont));
            tableBottom.addCell(cell2);

            tableBottom.addCell(emptyCell);

            cell2.setPhrase(new Phrase(blankSpace+userEntity.getFirstName()+" "+userEntity.getLastName()+blankSpace,helveticaFont));
            tableBottom.addCell(cell2);

            tableBottom.addCell(emptyCell);


            Paragraph tableBottomParagraph = new Paragraph();
            tableBottomParagraph.add(tableBottom);
            document.add(tableBottomParagraph);


            document.close();
            DocumentDto documentDto = new DocumentDto();
            documentDto.setDocument(byteArrayOutputStream.toByteArray());
            return documentDto;

        }
        catch (DocumentException e) {
            throw new DocumentException();

        }
    }


    public DocumentDto generateRestLeaveDocument(UserEntity userEntity, RequestEntity requestEntity) throws InvalidFormatException, IOException {

        Document document = new Document();
        document.setMargins(70, 70, 30, 30);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document,byteArrayOutputStream);
            document.open();

            Font helveticaFont = FontFactory.getFont("src/main/resources/fonts/Helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font underlineHelveticaFont = FontFactory.getFont("src/main/resources/fonts/Helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            underlineHelveticaFont.setStyle(Font.UNDERLINE);

            Font boldHelveticaFont = FontFactory.getFont("src/main/resources/fonts/Helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            boldHelveticaFont.setStyle(Font.BOLD);

            String blankSpace="     ";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("dd.MM.yyyy");
            Font fontBold = new Font();
            fontBold.setStyle(Font.BOLD);
            fontBold.setSize(11.5F);

            Paragraph oneEmptyLiner = new Paragraph();
            addEmptyLine(oneEmptyLiner, 1);

            Paragraph twoEmptyLiner = new Paragraph();
            addEmptyLine(twoEmptyLiner, 2);

            Paragraph threeEmptyLiner = new Paragraph();
            addEmptyLine(threeEmptyLiner, 3);
            Image imgSoc = Image.getInstance("classpath:Img/logo.jpg");
            imgSoc.scalePercent(15, 15);
            imgSoc.setAbsolutePosition(425, 730);
            document.add(imgSoc);


            document.add(threeEmptyLiner);
            document.add(twoEmptyLiner);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(0f);
            table.setSpacingAfter(0f);

            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBorder(Rectangle.NO_BORDER);
            emptyCell.setMinimumHeight(40f);


            PdfPCell cell1 = new PdfPCell();
            cell1.setBorder(Rectangle.NO_BORDER);
            cell1.setMinimumHeight(40);
            cell1.setCalculatedHeight(40);

            PdfPCell cell2 = new PdfPCell();
            cell2.setBorder(Rectangle.NO_BORDER);
            cell2.setMinimumHeight(40);
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell2.setPaddingRight(0f);

            PdfPCell emptyCell2 = new PdfPCell(new Phrase(""));
            emptyCell2.setBorder(Rectangle.NO_BORDER);
            emptyCell2.setFixedHeight(20f);
            emptyCell2.setBorder(Rectangle.NO_BORDER);


            document.add(threeEmptyLiner);
            document.add(twoEmptyLiner);

            Paragraph title1 = new Paragraph();
            title1.setAlignment(Element.ALIGN_CENTER);
            title1.setFont(boldHelveticaFont);
            title1.add("Cerere concediu de odihnă / Urlaubsantrag");

            document.add(title1);

            document.add(twoEmptyLiner);

            Paragraph firstPara = new Paragraph();
            firstPara.setMultipliedLeading(1.55f);
            firstPara.setAlignment(Element.ALIGN_LEFT);
            firstPara.setFont(helveticaFont);
            Chunk firstParaChunk1=new Chunk("Dl/Dna");
            firstParaChunk1.setFont(helveticaFont);

            Chunk firstParaChunk2=new Chunk(blankSpace+userEntity.getFirstName()+" "+userEntity.getLastName()+blankSpace);
            firstParaChunk2.setUnderline(0.5F,-1);
            Chunk firstParaChunk3=new Chunk(", angajat in functia de");
            firstParaChunk3.setFont(helveticaFont);
            Chunk firstParaChunk4=new Chunk(blankSpace+userEntity.getRole().toString()+blankSpace);
            firstParaChunk4.setUnderline(0.5F,-1);
            Chunk firstParaChunk5 = new Chunk("solicit plecarea in concediu pe anul "+ requestEntity.getHoliday().getStartDate().getYear() +" de la data de");
            firstParaChunk5.setFont(helveticaFont);
            Chunk firstParaChunk6 = new Chunk(blankSpace+requestEntity.getHoliday().getStartDate().format(formatter) + blankSpace);
            firstParaChunk6.setUnderline(0.5F,-1);
            Chunk firstParaChunk7 = new Chunk("pană la data de");
            firstParaChunk7.setFont(helveticaFont);
            Chunk firstParaChunk8 = new Chunk(blankSpace+requestEntity.getHoliday().getEndDate().format(formatter) + blankSpace);
            firstParaChunk8.setUnderline(0.5F,-1);
            Chunk firstParaChunk9 = new Chunk("adică");
            firstParaChunk9.setFont(helveticaFont);
            int duration = (int) Duration.between(requestEntity.getHoliday().getStartDate(), requestEntity.getHoliday().getEndDate()).toDays() + 1;
            Chunk firstParaChunk10 = new Chunk(blankSpace+  duration + blankSpace);
            firstParaChunk10.setUnderline(0.5F,-1);
            Chunk firstParaChunk11 = new Chunk("zile");
            firstParaChunk11.setFont(helveticaFont);

            firstPara.add(firstParaChunk1);
            firstPara.add(firstParaChunk2);
            firstPara.add(firstParaChunk3);
            firstPara.add(firstParaChunk4);
            firstPara.add(firstParaChunk5);
            firstPara.add(firstParaChunk6);
            firstPara.add(firstParaChunk7);
            firstPara.add(firstParaChunk8);
            firstPara.add(firstParaChunk9);
            firstPara.add(firstParaChunk10);
            firstPara.add(firstParaChunk11);
            document.add(firstPara);


            document.add(oneEmptyLiner);

            document.add(oneEmptyLiner);

            Paragraph secondPara = new Paragraph();
            secondPara.setMultipliedLeading(1.55f);
            secondPara.setFont(boldHelveticaFont);
            secondPara.add("Declar pe proprie răspundere că managerul de proiect a fost informat despre intentia de a pleca în concediu.");
            document.add(secondPara);

            document.add(oneEmptyLiner);



            Paragraph thirdPara = new Paragraph();
            thirdPara.setFont(fontBold);

            Chunk thirdParaChunk1 = new Chunk("Asa cum a fost agreat impreună cu Supervizorul meu, pe durata concediului " +
            "voi fi inlocuit pe proiecte de catre");
            thirdParaChunk1.setFont(boldHelveticaFont);
            Chunk thirdParaChunk2 = new Chunk(blankSpace + requestEntity.getHoliday().getRestHoliday().getSubstituteName() + blankSpace);
            thirdParaChunk2.setUnderline(0.5F,-1);
            thirdParaChunk2.setFont(boldHelveticaFont);
            Chunk thirdParaChunk3 = new Chunk(".");
            thirdParaChunk2.setFont(boldHelveticaFont);

            thirdPara.add(thirdParaChunk1);
            thirdPara.add(thirdParaChunk2);
            thirdPara.add(thirdParaChunk3);

            document.add(thirdPara);

            document.add(oneEmptyLiner);

            Paragraph multuPara = new Paragraph();
            multuPara.setFont(helveticaFont);
            multuPara.add("Va multumesc!");
            document.add(multuPara);

            document.add(threeEmptyLiner);

            PdfPTable tableBottom = new PdfPTable(2);
            tableBottom.setWidthPercentage(100);
            tableBottom.setSpacingBefore(0f);
            tableBottom.setSpacingAfter(0f);


            cell1.setPhrase(new Phrase("Nume si prenume", helveticaFont));
            tableBottom.addCell(cell1);

            tableBottom.addCell(emptyCell);

            tableBottom.addCell(emptyCell2);
            tableBottom.addCell(emptyCell2);

            Chunk tableBottomChunk1 = new Chunk(blankSpace + userEntity.getFirstName() + " " + userEntity.getLastName()+ blankSpace);
            cell1.setPhrase(new Phrase(tableBottomChunk1));
            tableBottom.addCell(cell1);

            tableBottom.addCell(emptyCell);

            tableBottom.addCell(emptyCell);

            cell2.setPhrase(new Phrase("Se aprobă / Genehmigt,", helveticaFont));
            tableBottom.addCell(cell2);

            tableBottom.addCell(emptyCell);

            cell2.setPhrase(new Phrase("Nume, Prenume,\nSemnătura", helveticaFont));
            tableBottom.addCell(cell2);

            tableBottom.addCell(emptyCell);

            cell2.setPhrase(new Phrase(blankSpace + userEntity.getTeam().getTeamLead().getFirstName() + " " +userEntity.getTeam().getTeamLead().getLastName() + blankSpace));
            tableBottom.addCell(cell2);

            tableBottom.addCell(emptyCell2);
            tableBottom.addCell(emptyCell2);





            Paragraph tableBottomParagraph = new Paragraph();
            tableBottomParagraph.add(tableBottom);
            document.add(tableBottomParagraph);

            document.close();
            DocumentDto documentDto = new DocumentDto();
            documentDto.setDocument(byteArrayOutputStream.toByteArray());
            return documentDto;

        } catch (DocumentException | FileNotFoundException e) {
            throw new RuntimeException(e);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public DocumentDto generateSpecialLeaveDocument(UserEntity userEntity, RequestEntity requestEntity) throws InvalidFormatException, IOException, DocumentException {

        Document document = new Document();
        document.setMargins(70, 70, 30, 30);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document,byteArrayOutputStream);
            document.open();

            Font fontBold = FontFactory.getFont("src/main/resources/fonts/Helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);;
            fontBold.setStyle(Font.BOLD);
            fontBold.setSize(11.5F);

            Font helveticaFont = FontFactory.getFont("src/main/resources/fonts/Helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font underlineHelveticaFont = FontFactory.getFont("src/main/resources/fonts/Helvetica.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            underlineHelveticaFont.setStyle(Font.UNDERLINE);

            String blankSpace="     ";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern ("dd.MM.yyyy");



            Paragraph oneEmptyLiner = new Paragraph();
            addEmptyLine(oneEmptyLiner,1);

            Paragraph twoEmptyLiner = new Paragraph();
            addEmptyLine(twoEmptyLiner,2);

            Paragraph threeEmptyLiner = new Paragraph();
            addEmptyLine(threeEmptyLiner,3);

            Image imgSoc = Image.getInstance("classpath:Img/logo.jpg");
            imgSoc.scalePercent(15,15);
            imgSoc.setAbsolutePosition(425, 730);
            document.add(imgSoc);


            document.add(threeEmptyLiner);
            document.add(twoEmptyLiner);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(0f);
            table.setSpacingAfter(0f);

            PdfPCell emptyCell = new PdfPCell(new Phrase(""));
            emptyCell.setBorder(Rectangle. NO_BORDER);
            emptyCell.setFixedHeight(35f);

            PdfPCell emptyCell2 = new PdfPCell(new Phrase(""));
            emptyCell2.setBorder(Rectangle.NO_BORDER);
            emptyCell2.setFixedHeight(20f);
            emptyCell2.setBorder(Rectangle.NO_BORDER);

            PdfPCell cell1 = new PdfPCell();
            cell1.setBorder(Rectangle. NO_BORDER);
            cell1.setFixedHeight(35f);

            PdfPCell cell2 = new PdfPCell();
            cell2.setBorder(Rectangle. NO_BORDER);
            cell2.setFixedHeight(35f);
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell2.setPaddingRight(0f);

            document.add(threeEmptyLiner);
            document.add(twoEmptyLiner);

            Paragraph title1 = new Paragraph();
            title1.setAlignment(Element.ALIGN_CENTER);
            title1.add("Cerere de acordare a concediului\n-evenimente speciale -");
            document.add(title1);

            document.add(twoEmptyLiner);

            Paragraph firstPara = new Paragraph();
            firstPara.setMultipliedLeading(1.55f);
            firstPara.setAlignment(Element.ALIGN_LEFT);
            Chunk firstParaChunkName = new Chunk(blankSpace+userEntity.getFirstName()+" "+userEntity.getLastName()+blankSpace);
            firstParaChunkName.setUnderline(0.5F,-1);
            Chunk firstParaChunkDepartment = new Chunk(blankSpace+userEntity.getDepartment()+blankSpace);
            firstParaChunkDepartment.setUnderline(0.5F,-1);
            Chunk firstParaChunkRole = new Chunk(blankSpace+userEntity.getRole().toString()+blankSpace);
            firstParaChunkRole.setUnderline(0.5F,-1);
            Chunk firstParaChunkNoDays = new Chunk(blankSpace+ChronoUnit.DAYS.between(requestEntity.getHoliday().getStartDate(),requestEntity.getHoliday().getEndDate())+blankSpace);
            firstParaChunkNoDays.setUnderline(0.5F,-1);
            Chunk firstParaChunkPeriod = new Chunk(blankSpace+requestEntity.getHoliday().getStartDate().format(formatter)+" - "+requestEntity.getHoliday().getEndDate().format(formatter)+blankSpace);
            firstParaChunkPeriod.setUnderline(0.5F,-1);
            Chunk firstParaChunkHardText1=new Chunk("Subsemnatul(a) ");
            firstParaChunkHardText1.setFont(helveticaFont);
            Chunk firstParaChunkHardText2=new Chunk(" ,angajat(a) la societatea MHP CONSULTING ROMANIA, în functia de");
            firstParaChunkHardText2.setFont(helveticaFont);
            Chunk firstParaChunkHardText3=new Chunk(" ,department ");
            firstParaChunkHardText3.setFont(helveticaFont);
            Chunk firstParaChunkHardText4=new Chunk(" va rog sa-mi aprobati efectuarea a ");
            firstParaChunkHardText4.setFont(helveticaFont);
            Chunk firstParaChunkHardText5=new Chunk(" zi/zile libere platite în perioada: ");
            firstParaChunkHardText5.setFont(helveticaFont);
            firstPara.add(firstParaChunkHardText1);
            firstPara.add(firstParaChunkName);
            firstPara.add(firstParaChunkHardText2);
            firstPara.add(firstParaChunkRole);
            firstPara.add(firstParaChunkHardText3);
            firstPara.add(firstParaChunkDepartment);
            firstPara.add(firstParaChunkHardText4);
            firstPara.add(firstParaChunkNoDays);
            firstPara.add(firstParaChunkHardText5);
            firstPara.add(firstParaChunkPeriod);
            document.add(firstPara);


            Paragraph extraPara = new Paragraph();
            Chunk extraParaChunkHardText= new Chunk("În sprijinul cererii mele, atasez o copie a certificatului ");
            Chunk extraParaChunkDocumentName = new Chunk(blankSpace+requestEntity.getHoliday().getSpecialHoliday().getDocumentName()+blankSpace);
            extraParaChunkDocumentName.setUnderline(0.5F,-1);

            extraPara.add(extraParaChunkHardText);
            extraPara.add(extraParaChunkDocumentName);

            document.add(extraPara);

            document.add(oneEmptyLiner);

            Paragraph secondPara = new Paragraph();
            secondPara.setMultipliedLeading(1.55f);
            secondPara.setFont(fontBold);


            secondPara.add("Declar pe proprie răspundere ca managerul de proiect a fost informat despre intentia de a pleca în concediu.");
            document.add(secondPara);

            document.add(oneEmptyLiner);
            Paragraph thirdPara = new Paragraph();
            thirdPara.setFont(fontBold);

            Chunk thirdParaChunkHardText= new Chunk("Asa cum a fost agreat impreuna cu Supervizorul meu, pe durata concediului " +
                    "voi fi inlocuit pe proiecte de catre ");
            Chunk thirdParaChunkSubstituteName = new Chunk(blankSpace+requestEntity.getHoliday().getSpecialHoliday().getSubstituteName()+blankSpace);
            thirdParaChunkSubstituteName.setUnderline(0.5F,-1);
            thirdPara.add(thirdParaChunkHardText);
            thirdPara.add(thirdParaChunkSubstituteName);
            document.add(thirdPara);

            document.add(oneEmptyLiner);

            Paragraph multuPara = new Paragraph();
            multuPara.add("Va multumesc!");
            document.add(multuPara);

            document.add(threeEmptyLiner);

            PdfPTable tableBottom = new PdfPTable(2);
            tableBottom.setWidthPercentage(100);
            tableBottom.setSpacingBefore(0f);
            tableBottom.setSpacingAfter(0f);


            cell1.setPhrase(new Phrase("Nume si prenume"));
            tableBottom.addCell(cell1);

            tableBottom.addCell(emptyCell);

            tableBottom.addCell(emptyCell2);
            tableBottom.addCell(emptyCell2);

            cell1.setPhrase(new Phrase(blankSpace+userEntity.getFirstName()+" "+userEntity.getLastName()+blankSpace,helveticaFont));
            tableBottom.addCell(cell1);

            tableBottom.addCell(emptyCell);

            tableBottom.addCell(emptyCell);

            cell2.setPhrase(new Phrase("Se aproba, Nume, Prenume, \nSemnatura"));
            tableBottom.addCell(cell2);

            tableBottom.addCell(emptyCell2);
            tableBottom.addCell(emptyCell2);

            tableBottom.addCell(emptyCell);
            cell2.setPhrase(new Phrase(blankSpace + userEntity.getTeam().getTeamLead().getFirstName() + " " +userEntity.getTeam().getTeamLead().getLastName() + blankSpace));


            Paragraph tableBottomParagraph = new Paragraph();
            tableBottomParagraph.add(tableBottom);
            document.add(tableBottomParagraph);

            document.close();
            DocumentDto documentDto = new DocumentDto();
            documentDto.setDocument(byteArrayOutputStream.toByteArray());
            return documentDto;

        }
        catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
            throw  new DocumentException();

        }
    }

    @Transactional
    @Override
    public DocumentDto autocompletePdfDocument(Long userId, Long holidayId) throws DocumentException, IOException, InvalidFormatException
    {
        RequestEntity requestEntity = requestRepository.findByUserIdAndHolidayId(userId, holidayId);
        UserEntity userEntity = userRepository.getById(userId);
        HolidayEntity holiday = requestEntity.getHoliday();

        if (holiday.getRestHoliday() != null) {

            return generateRestLeaveDocument(userEntity, requestEntity);
        } else if (holiday.getSpecialHoliday() != null) {

            return generateSpecialLeaveDocument(userEntity, requestEntity);
        } else{

            return generateUnpaidLeaveDocument(userEntity, requestEntity);
        }
    }

    @Override
    public SendRawEmailResult sendSimpleEmail(String requestingUserEmail, Long userId, Long holidayId) throws MessagingException, DocumentException, IOException, InvalidFormatException {

        String SUBJECT = "Holiday Request";

        String SENDER = userRepository.getById(userId).getEmail();

        String RECIPIENT = requestingUserEmail;

        String BODY_HTML = "<html>"
                + "<head></head>"
                + "<body>"
                + "<h1>Hello!</h1>"
                + "<p>Please see the attached file for a "
                + "list of customers to contact.</p>"
                + "</body>"
                + "</html>";

        Session session = Session.getDefaultInstance(new Properties());

        MimeMessage message = new MimeMessage(session);

        // Add subject, from and to lines.
        message.setSubject(SUBJECT, "UTF-8");
        message.setFrom(new InternetAddress(SENDER));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECIPIENT));

        // Create a multipart/alternative child container.
        MimeMultipart msg_body = new MimeMultipart("alternative");

        // Create a wrapper for the HTML and text parts.
        MimeBodyPart wrap = new MimeBodyPart();

        // Define the HTML part.
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(BODY_HTML,"text/html; charset=UTF-8");

        msg_body.addBodyPart(htmlPart);

        // Add the child container to the wrapper object.
        wrap.setContent(msg_body);

        // Create a multipart/mixed parent container.
        MimeMultipart msg = new MimeMultipart("mixed");

        // Add the parent container to the message.
        message.setContent(msg);

        // Add the multipart/alternative part to the message.
        msg.addBodyPart(wrap);

        // Define the attachment
        MimeBodyPart att = new MimeBodyPart();

        File file = new File("tmp1");
        DataSource source1 = new FileDataSource(file);

        byte[] sourceBytes = autocompletePdfDocument(userId, holidayId).getDocument();

        OutputStream sourceOS = source1.getOutputStream();
        sourceOS.write(sourceBytes);

        sourceOS.close();

        RequestEntity requestEntity = requestRepository.findByUserIdAndHolidayId(userId, holidayId);

        att.setDataHandler(new DataHandler(source1));
        att.setFileName("Holiday Request Document.pdf");

        // Add the attachment to the message.
        msg.addBodyPart(att);

        File file2 = null;

        if (requestEntity.getHoliday().getSpecialHoliday() != null) {

            MimeBodyPart att2 = new MimeBodyPart();

            file2 = new File("tmp2");
            DataSource source2 = new FileDataSource(file2);

            sourceBytes = requestEntity.getHoliday().getSpecialHoliday().getDocument();

            OutputStream sourceOS2 = source2.getOutputStream();
            sourceOS2.write(sourceBytes);

            sourceOS2.close();

            att2.setDataHandler(new DataHandler(source2));
            att2.setFileName(requestEntity.getHoliday().getSpecialHoliday().getDocumentName());

            msg.addBodyPart(att2);
        }

        SendRawEmailResult result = null;

        // Try to send the email.
        try {

            System.out.println("Attempting to send an email through Amazon SES "
                    +"using the AWS SDK for Java...");

            // Print the raw email content on the console
            PrintStream out = System.out;
            message.writeTo(out);

            // Send the email.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            RawMessage rawMessage =
                    new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

            SendRawEmailRequest rawEmailRequest =
                    new SendRawEmailRequest(rawMessage)
                            .withConfigurationSetName("");

            result = amazonSimpleEmailService.sendRawEmail(rawEmailRequest);
            System.out.println("Email sent!");

            if (file.delete()) {

                System.out.println("Temp file deleted");
            }

            if (file2 != null && file2.delete()) {

                System.out.println("Temp file deleted");
            }

        }
        catch (Exception ex) {
            System.out.println("Email Failed");
            System.err.println("Error message: " + ex.getMessage());
            ex.printStackTrace();
        }

        return result;
    }
}
