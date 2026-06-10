package com.example.base_spring_boot.seeder;

import com.example.base_spring_boot.models.constants.RoleName;
import com.example.base_spring_boot.models.entities.Role;
import com.example.base_spring_boot.models.repositories.IRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class RoleDataSeeder implements CommandLineRunner {
    private final IRoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem database đã có dữ liệu Role chưa để tránh insert trùng lặp
        if (roleRepository.count() == 0) {

            Role adminRole = new Role();
            adminRole.setRoleName(RoleName.ROLE_ADMIN);

            Role managerRole = new Role();
            managerRole.setRoleName(RoleName.ROLE_MANAGER);

            Role customerRole = new Role();
            customerRole.setRoleName(RoleName.ROLE_CUSTOMER);

            // Lưu tất cả các role vào database
            roleRepository.saveAll(Arrays.asList(adminRole, managerRole, customerRole));

            System.out.println("Đã seed dữ liệu Role thành công!");
        } else {
            System.out.println("Dữ liệu Role đã tồn tại, bỏ qua bước seeder.");
        }
    }
}
