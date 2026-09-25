package com.prj.beehouse;

import com.prj.beehouse.entity.Authority;
import com.prj.beehouse.entity.Instalment;
import com.prj.beehouse.entity.InstalmentTemplate;
import com.prj.beehouse.entity.User;
import com.prj.beehouse.entity.enumerated.AuthorityName;
import com.prj.beehouse.repository.AuthorityRepository;
import com.prj.beehouse.repository.InstalmentRepository;
import com.prj.beehouse.repository.InstalmentTemplateRepository;
import com.prj.beehouse.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootApplication
@EnableScheduling
public class BeeHouseApplication {

	private static final BigDecimal DEFAULT_INSTALMENT_AMOUNT = BigDecimal.valueOf(300);

	public static void main(String[] args) {
		SpringApplication.run(BeeHouseApplication.class, args);
	}

	@Bean
	public CommandLineRunner seedBaseData(
			AuthorityRepository authorityRepository,
			UserRepository userRepository,
			InstalmentTemplateRepository instalmentTemplateRepository,
			InstalmentRepository instalmentRepository,
			PasswordEncoder passwordEncoder
	) {
		return args -> {
			Authority userAuthority = authorityRepository.findById(1)
					.orElseGet(() -> Authority.builder()
							.id(1)
							.authorityName(AuthorityName.USER)
							.build());
			userAuthority.setAuthorityName(AuthorityName.USER);
			userAuthority.setAuthorityDefault(true);
			userAuthority = authorityRepository.save(userAuthority);

			Authority administratorAuthority = authorityRepository.findById(2)
					.orElseGet(() -> Authority.builder()
							.id(2)
							.authorityName(AuthorityName.ADMINISTRATOR)
							.build());
			administratorAuthority.setAuthorityName(AuthorityName.ADMINISTRATOR);
			administratorAuthority.setAuthorityDefault(false);
			administratorAuthority = authorityRepository.save(administratorAuthority);

			if (!userRepository.existsByUsername("administrator")) {
				User administrator = User.builder()
						.username("administrator")
						.name("Administrator")
						.lastname("BeeHouse")
						.email("administrator@beehouse.local")
						.password(passwordEncoder.encode("administrator"))
						.enable(true)
						.authority(administratorAuthority)
						.build();
				userRepository.save(administrator);
			}

			InstalmentTemplate instalmentTemplate = instalmentTemplateRepository.findFirstByOrderByIdAsc()
					.orElseGet(() -> InstalmentTemplate.builder()
							.cost(DEFAULT_INSTALMENT_AMOUNT)
							.build());
			instalmentTemplate.setCost(DEFAULT_INSTALMENT_AMOUNT);
			instalmentTemplate = instalmentTemplateRepository.save(instalmentTemplate);

			LocalDate today = LocalDate.now();
			if (!instalmentRepository.existsByYearAndMonth(today.getYear(), today.getMonthValue())) {
				Instalment instalment = Instalment.builder()
						.year(today.getYear())
						.month(today.getMonthValue())
						.initialAmount(DEFAULT_INSTALMENT_AMOUNT)
						.currentBalance(DEFAULT_INSTALMENT_AMOUNT)
						.instalmentTemplate(instalmentTemplate)
						.build();
				instalmentRepository.save(instalment);
			}
		};
	}
}
