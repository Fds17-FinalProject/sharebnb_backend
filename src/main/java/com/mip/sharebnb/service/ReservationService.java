package com.mip.sharebnb.service;

import com.mip.sharebnb.dto.AccommodationDto;
import com.mip.sharebnb.dto.ReservationDto;
import com.mip.sharebnb.model.Accommodation;
import com.mip.sharebnb.model.BookedDate;
import com.mip.sharebnb.model.Member;
import com.mip.sharebnb.model.Reservation;
import com.mip.sharebnb.repository.AccommodationRepository;
import com.mip.sharebnb.repository.MemberRepository;
import com.mip.sharebnb.repository.BookedDateRepository;
import com.mip.sharebnb.repository.ReservationRepository;

import com.mip.sharebnb.repository.dynamic.DynamicReservationRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.spec.PSource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final DynamicReservationRepository dynamicReservationRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final AccommodationRepository accommodationRepository;
    private final BookedDateRepository bookedDateRepository;

    private Long code = 100000000L;
    private LocalDate saveDate;

    public List<ReservationDto> getReservations(Long memberId) {
        if (memberId == null) {
            return new ArrayList<>();
        }
        List<Reservation> reservations = reservationRepository.findReservationByMemberId(memberId);

        List<ReservationDto> reservationDtoList = new ArrayList<>();

        for (Reservation reservation : reservations) {

            ReservationDto reservationDto = new ReservationDto();

            reservationDto.setReservationId(reservation.getId());
            reservationDto.setCheckInDate(reservation.getCheckInDate());
            reservationDto.setCheckoutDate(reservation.getCheckoutDate());
            reservationDto.setAccommodationDto(mappingAccommodationDto(reservation));

            reservationDtoList.add(reservationDto);
        }
        return reservationDtoList;
    }

    @Transactional
    public Reservation insertReservation(ReservationDto reservationDto) {
        System.out.println("memeberId " + reservationDto.getMemberId() + " accommodationId" + reservationDto.getAccommodationId());
        Optional<Member> optionalMember = Optional.of(memberRepository.findById(reservationDto.getMemberId()).orElse(new Member()));
        Member member = optionalMember.get();

        Optional<Accommodation> optionalAccommodation = Optional.of(accommodationRepository.findById(reservationDto.getAccommodationId()).orElse(new Accommodation()));
        Accommodation accommodation = optionalAccommodation.get();

        List<BookedDate> bookedDates = new ArrayList<>();

        for (LocalDate date = reservationDto.getCheckInDate(); date.isBefore(reservationDto.getCheckoutDate()); date = date.plusDays(1)) {
            bookedDates.add(saveBookedDate(date, accommodation));
        }

        List<BookedDate> checkDuplicateDate = dynamicReservationRepository.findByReservationIdAndDate(reservationDto.getAccommodationId(), reservationDto.getCheckInDate(), reservationDto.getCheckoutDate());
        System.out.println(" >>> " + checkDuplicateDate.isEmpty());

        if (checkDuplicateDate.isEmpty()) {
            Reservation buildReservation = Reservation.builder()
                    .checkInDate(reservationDto.getCheckInDate())
                    .checkoutDate(reservationDto.getCheckoutDate())
                    .guestNum(reservationDto.getGuestNum())
                    .totalPrice(reservationDto.getTotalPrice())
                    .isCanceled(false)
                    .paymentDate(LocalDate.now())
                    .member(member)
                    .accommodation(accommodation)
                    .bookedDates(bookedDates)
                    .reservationCode(setReservationCode())
                    .build();

            System.out.println(setReservationCode());
            return reservationRepository.save(buildReservation);
        }

        return new Reservation();
    }

    @Transactional
    public Reservation updateReservation(Long reservationId, ReservationDto reservationDto) {

        List<BookedDate> findBookedDates = bookedDateRepository.findBookedDatesByReservationId(reservationId);

        if (findBookedDates.isEmpty()) {
            // 예외처리
            return new Reservation();
        }

        bookedDateRepository.deleteBookedDateByReservationId(reservationId);

        Optional<Reservation> findReservation = Optional.of(reservationRepository.findById(reservationId).orElse(new Reservation()));

        Reservation reservation = findReservation.get();

        List<BookedDate> reservations = dynamicReservationRepository.findByReservationIdAndDate(reservation.getAccommodation().getId(), reservationDto.getCheckInDate(), reservationDto.getCheckoutDate());

        if (reservations.isEmpty()) {
            reservation.setCheckInDate(reservationDto.getCheckInDate());
            reservation.setCheckoutDate(reservationDto.getCheckoutDate());
            reservation.setGuestNum(reservationDto.getGuestNum());
            reservation.setTotalPrice(reservationDto.getTotalPrice());

            return reservationRepository.save(reservation);

        } else {

            for (BookedDate findBookedDate : findBookedDates) {
                bookedDateRepository.save(findBookedDate);
            }

            return new Reservation();
        }
    }


    private AccommodationDto mappingAccommodationDto(Reservation reservation) {

        AccommodationDto accommodationDto = new AccommodationDto();

        accommodationDto.setCity(reservation.getAccommodation().getCity());
        accommodationDto.setGu(reservation.getAccommodation().getGu());
        accommodationDto.setAccommodationPictures(reservation.getAccommodation().getAccommodationPictures());

        return accommodationDto;
    }

    private BookedDate saveBookedDate(LocalDate date, Accommodation accommodation) {
        BookedDate bookedDate = BookedDate.builder()
                .date(date)
                .accommodation(accommodation)
                .build();
        bookedDateRepository.save(bookedDate);

        return bookedDate;
    }

    private String setReservationCode(){
        LocalDate today = LocalDate.now();

        if (saveDate.equals(today)){
            code += 1;
        } else {
            saveDate = today;
            code = 10000000L;
        }

        String nowDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        StringBuilder sb = new StringBuilder();
        sb.append("Num").append(nowDate).append(String.valueOf(code));

        return sb.toString();

    }
}
