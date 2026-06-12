package com.example.base_spring_boot.seeder;

import com.example.base_spring_boot.models.entities.Court;
import com.example.base_spring_boot.models.entities.CourtType;
import com.example.base_spring_boot.models.entities.Slot;
import com.example.base_spring_boot.models.repositories.ICourtRepository;
import com.example.base_spring_boot.models.repositories.ICourtTypeRepository;
import com.example.base_spring_boot.models.repositories.ISlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class MasterDataSeeder implements CommandLineRunner {
    private final ICourtTypeRepository courtTypeRepository;
    private final ICourtRepository courtRepository;
    private final ISlotRepository slotRepository;

    @Override
    public void run(String... args) throws Exception {
        if (courtTypeRepository.count() == 0) {
            CourtType tham = courtTypeRepository.save(CourtType.builder().name("Sân thảm").description("Sân thảm chất lượng cao").build());
            CourtType go = courtTypeRepository.save(CourtType.builder().name("Sân gỗ").description("Sân gỗ tiêu chuẩn").build());

            if (courtRepository.count() == 0) {
                courtRepository.save(Court.builder().courtName("Sân số 1").courtType(tham).pricePerHour(100000.0).status("AVAILABLE").build());
                courtRepository.save(Court.builder().courtName("Sân số 2").courtType(tham).pricePerHour(100000.0).status("AVAILABLE").build());
                courtRepository.save(Court.builder().courtName("Sân số 3").courtType(go).pricePerHour(80000.0).status("AVAILABLE").build());
            }
        }

        if (slotRepository.count() == 0) {
            slotRepository.save(Slot.builder().startTime(LocalTime.of(5, 0)).endTime(LocalTime.of(6, 0)).build());
            slotRepository.save(Slot.builder().startTime(LocalTime.of(6, 0)).endTime(LocalTime.of(7, 0)).build());
            slotRepository.save(Slot.builder().startTime(LocalTime.of(7, 0)).endTime(LocalTime.of(8, 0)).build());
            slotRepository.save(Slot.builder().startTime(LocalTime.of(17, 0)).endTime(LocalTime.of(18, 0)).build());
            slotRepository.save(Slot.builder().startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(19, 0)).build());
            slotRepository.save(Slot.builder().startTime(LocalTime.of(19, 0)).endTime(LocalTime.of(20, 0)).build());
            slotRepository.save(Slot.builder().startTime(LocalTime.of(20, 0)).endTime(LocalTime.of(21, 0)).build());
        }
    }
}
